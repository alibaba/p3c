/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package com.alibaba.p3c.idea.pmd

import com.alibaba.p3c.idea.config.P3cConfig
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import net.sourceforge.pmd.PMD
import net.sourceforge.pmd.PMDConfiguration
import net.sourceforge.pmd.PMDException
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.RuleSets
import net.sourceforge.pmd.benchmark.Benchmark
import net.sourceforge.pmd.benchmark.Benchmarker
import net.sourceforge.pmd.lang.LanguageVersion
import net.sourceforge.pmd.lang.LanguageVersionHandler
import net.sourceforge.pmd.lang.Parser
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.ParseException
import net.sourceforge.pmd.lang.xpath.Initializer
import org.apache.commons.io.IOUtils
import java.io.Reader
import java.util.concurrent.TimeUnit

class SourceCodeProcessor(private val configuration: PMDConfiguration) {
    val logger = Logger.getInstance(javaClass)

    /**
     * Processes the input stream against a rule set using the given input encoding.
     * If the LanguageVersion is `null`  on the RuleContext, it will
     * be automatically determined.  Any code which wishes to process files for
     * different Languages, will need to be sure to either properly set the
     * Language on the RuleContext, or set it to `null` first.

     * @see RuleContext.setLanguageVersion
     * @see PMDConfiguration.getLanguageVersionOfFile
     * @param sourceCode The Reader to analyze.
     * @param ruleSets The collection of rules to process against the file.
     * @param ctx The context in which PMD is operating.
     * @throws PMDException if the input encoding is unsupported, the input stream could
     *                      not be parsed, or other error is encountered.
     */
    @Throws(PMDException::class)
    fun processSourceCode(sourceCode: Reader, ruleSets: RuleSets, ctx: RuleContext) {
        determineLanguage(ctx)

        // make sure custom XPath functions are initialized
        Initializer.initialize()
        // Coarse check to see if any RuleSet applies to file, will need to do a finer RuleSet specific check later
        try {
            processSource(sourceCode, ruleSets, ctx)

        } catch (pe: ParseException) {
            throw PMDException("Error while parsing " + ctx.sourceCodeFilename, pe)
        } catch (e: Exception) {
            throw PMDException("Error while processing " + ctx.sourceCodeFilename, e)
        } catch (error: Error) {
            throw PMDException("Error while processing ${ctx.sourceCodeFilename} ${error.message}")
        } finally {
            IOUtils.closeQuietly(sourceCode)
        }
    }


    private fun parse(ctx: RuleContext, sourceCode: Reader, parser: Parser): Node {
        val start = System.nanoTime()
        val rootNode = parser.parse(ctx.sourceCodeFilename, sourceCode)
        ctx.report.suppress(parser.suppressMap)
        val end = System.nanoTime()
        Benchmarker.mark(Benchmark.Parser, end - start, 0)
        return rootNode
    }

    private fun symbolFacade(rootNode: Node, languageVersionHandler: LanguageVersionHandler) {
        val start = System.nanoTime()
        languageVersionHandler.getSymbolFacade(configuration.classLoader).start(rootNode)
        val end = System.nanoTime()
        Benchmarker.mark(Benchmark.SymbolTable, end - start, 0)
    }

    private fun usesDFA(languageVersion: LanguageVersion, rootNode: Node, ruleSets: RuleSets) {
        val start = System.nanoTime()
        val dataFlowFacade = languageVersion.languageVersionHandler.dataFlowFacade
        dataFlowFacade.start(rootNode)
        val end = System.nanoTime()
        Benchmarker.mark(Benchmark.DFA, end - start, 0)
    }

    private fun usesTypeResolution(languageVersion: LanguageVersion, rootNode: Node, ruleSets: RuleSets) {
        if (ruleSets.usesTypeResolution(languageVersion.language)) {
            val start = System.nanoTime()
            languageVersion.languageVersionHandler.getTypeResolutionFacade(configuration.classLoader).start(rootNode)
            val end = System.nanoTime()
            Benchmarker.mark(Benchmark.TypeResolution, end - start, 0)
        }
    }

    private fun processSource(sourceCode: Reader, ruleSets: RuleSets, ctx: RuleContext) {
        val start = System.currentTimeMillis()
        val acus = listOf(getRootNode(sourceCode, ruleSets, ctx))
        logger.debug("elapsed ${System.currentTimeMillis() - start}ms to" +
                " parse ast tree for file ${ctx.sourceCodeFilename}")
        ruleSets.apply(acus, ctx, ctx.languageVersion.language)
    }

    private fun getRootNode(sourceCode: Reader, ruleSets: RuleSets, ctx: RuleContext): Node {
        if (!smartFoxConfig.astCacheEnable) {
            return parseNode(ctx, ruleSets, sourceCode)
        }
        val node = nodeCache.getIfPresent(ctx.sourceCodeFilename)
        if (node != null) {
            return node
        }
        return parseNode(ctx, ruleSets, sourceCode)
    }

    private fun parseNode(ctx: RuleContext, ruleSets: RuleSets, sourceCode: Reader): Node {
        val languageVersion = ctx.languageVersion
        val languageVersionHandler = languageVersion.languageVersionHandler
        val parser = PMD.parserFor(languageVersion, configuration)
        val rootNode = parse(ctx, sourceCode, parser)
        symbolFacade(rootNode, languageVersionHandler)
        usesDFA(languageVersion, rootNode, ruleSets)
        usesTypeResolution(languageVersion, rootNode, ruleSets)
        nodeCache.put(ctx.sourceCodeFilename, rootNode)
        return rootNode
    }

    private fun determineLanguage(ctx: RuleContext) {
        // If LanguageVersion of the source file is not known, make a determination
        if (ctx.languageVersion == null) {
            val languageVersion = configuration.getLanguageVersionOfFile(ctx.sourceCodeFilename)
            ctx.languageVersion = languageVersion
        }
    }

    companion object {
        val smartFoxConfig = ServiceManager.getService(P3cConfig::class.java)!!
        private lateinit var nodeCache: Cache<String, Node>

        init {
            reInitNodeCache(smartFoxConfig.astCacheTime)
        }

        fun reInitNodeCache(expireTime: Long) {
            nodeCache = CacheBuilder.newBuilder().concurrencyLevel(16)
                    .expireAfterWrite(expireTime, TimeUnit.MILLISECONDS)
                    .maximumSize(100)
                    .build<String, Node>()!!
        }

        fun invalidateCache(file: String) {
            nodeCache.invalidate(file)
        }
    }
}
