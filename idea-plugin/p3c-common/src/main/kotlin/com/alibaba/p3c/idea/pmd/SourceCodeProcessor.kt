/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package com.alibaba.p3c.idea.pmd

import com.alibaba.p3c.idea.config.P3cConfig
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.openapi.components.ServiceManager
import net.sourceforge.pmd.PMD
import net.sourceforge.pmd.PMDConfiguration
import net.sourceforge.pmd.PMDException
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.RuleSets
import net.sourceforge.pmd.benchmark.TimeTracker
import net.sourceforge.pmd.benchmark.TimedOperationCategory
import net.sourceforge.pmd.lang.Language
import net.sourceforge.pmd.lang.LanguageVersion
import net.sourceforge.pmd.lang.LanguageVersionHandler
import net.sourceforge.pmd.lang.Parser
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.ParseException
import net.sourceforge.pmd.lang.xpath.Initializer
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.concurrent.TimeUnit

class SourceCodeProcessor(private val configuration: PMDConfiguration) {

    /**
     * Processes the input stream against a rule set using the given input
     * encoding.
     *
     * @param sourceCode
     * The InputStream to analyze.
     * @param ruleSets
     * The collection of rules to process against the file.
     * @param ctx
     * The context in which PMD is operating.
     * @throws PMDException
     * if the input encoding is unsupported, the input stream could
     * not be parsed, or other error is encountered.
     * @see .processSourceCode
     */
    @Throws(PMDException::class)
    fun processSourceCode(sourceCode: InputStream, ruleSets: RuleSets, ctx: RuleContext) {
        try {
            InputStreamReader(sourceCode, configuration.sourceEncoding).use { streamReader -> processSourceCode(streamReader, ruleSets, ctx) }
        } catch (e: IOException) {
            throw PMDException("IO exception: " + e.message, e)
        }

    }

    /**
     * Processes the input stream against a rule set using the given input
     * encoding. If the LanguageVersion is `null` on the RuleContext,
     * it will be automatically determined. Any code which wishes to process
     * files for different Languages, will need to be sure to either properly
     * set the Language on the RuleContext, or set it to `null`
     * first.
     *
     * @see RuleContext.setLanguageVersion
     * @see PMDConfiguration.getLanguageVersionOfFile
     * @param sourceCode
     * The Reader to analyze.
     * @param ruleSets
     * The collection of rules to process against the file.
     * @param ctx
     * The context in which PMD is operating.
     * @throws PMDException
     * if the input encoding is unsupported, the input stream could
     * not be parsed, or other error is encountered.
     */
    @Throws(PMDException::class)
    fun processSourceCode(sourceCode: Reader, ruleSets: RuleSets, ctx: RuleContext) {
        determineLanguage(ctx)

        // make sure custom XPath functions are initialized
        Initializer.initialize()

        try {
            ruleSets.start(ctx)
            processSource(sourceCode, ruleSets, ctx)
        } catch (pe: ParseException) {
            configuration.analysisCache.analysisFailed(ctx.sourceCodeFile)
            throw PMDException("Error while parsing " + ctx.sourceCodeFilename, pe)
        } catch (e: Exception) {
            configuration.analysisCache.analysisFailed(ctx.sourceCodeFile)
            throw PMDException("Error while processing " + ctx.sourceCodeFilename, e)
        } finally {
            ruleSets.end(ctx)
        }
    }

    private fun parse(ctx: RuleContext, sourceCode: Reader, parser: Parser): Node {
        TimeTracker.startOperation(TimedOperationCategory.PARSER).use {
            val rootNode = parser.parse(ctx.sourceCodeFilename, sourceCode)
            ctx.report.suppress(parser.suppressMap)
            return rootNode
        }
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
        val language = languageVersion.language
        usesDFA(languageVersion, rootNode, ruleSets, language)
        usesTypeResolution(languageVersion, rootNode, ruleSets, language)
        nodeCache.put(ctx.sourceCodeFilename, rootNode)
        return rootNode
    }

    private fun symbolFacade(rootNode: Node, languageVersionHandler: LanguageVersionHandler) {
        TimeTracker.startOperation(TimedOperationCategory.SYMBOL_TABLE).use { to -> languageVersionHandler.getSymbolFacade(configuration.classLoader).start(rootNode) }
    }

    private fun resolveQualifiedNames(rootNode: Node, handler: LanguageVersionHandler) {
        TimeTracker.startOperation(TimedOperationCategory.QUALIFIED_NAME_RESOLUTION).use { to -> handler.getQualifiedNameResolutionFacade(configuration.classLoader).start(rootNode) }
    }

    // private ParserOptions getParserOptions(final LanguageVersionHandler
    // languageVersionHandler) {
    // // TODO Handle Rules having different parser options.
    // ParserOptions parserOptions =
    // languageVersionHandler.getDefaultParserOptions();
    // parserOptions.setSuppressMarker(configuration.getSuppressMarker());
    // return parserOptions;
    // }

    private fun usesDFA(languageVersion: LanguageVersion, rootNode: Node, ruleSets: RuleSets, language: Language) {
        if (ruleSets.usesDFA(language)) {
            TimeTracker.startOperation(TimedOperationCategory.DFA).use { to ->
                val dataFlowFacade = languageVersion.languageVersionHandler.dataFlowFacade
                dataFlowFacade.start(rootNode)
            }
        }
    }

    private fun usesTypeResolution(languageVersion: LanguageVersion, rootNode: Node, ruleSets: RuleSets,
        language: Language) {

        if (ruleSets.usesTypeResolution(language)) {
            TimeTracker.startOperation(TimedOperationCategory.TYPE_RESOLUTION).use { to ->
                languageVersion.languageVersionHandler.getTypeResolutionFacade(configuration.classLoader)
                    .start(rootNode)
            }
        }
    }


    private fun usesMultifile(rootNode: Node, languageVersionHandler: LanguageVersionHandler, ruleSets: RuleSets,
        language: Language) {

        if (ruleSets.usesMultifile(language)) {
            TimeTracker.startOperation(TimedOperationCategory.MULTIFILE_ANALYSIS).use { to -> languageVersionHandler.multifileFacade.start(rootNode) }
        }
    }


    private fun processSource(sourceCode: Reader, ruleSets: RuleSets, ctx: RuleContext) {
        val languageVersion = ctx.languageVersion
        val languageVersionHandler = languageVersion.languageVersionHandler

        val rootNode = getRootNode(sourceCode, ruleSets, ctx)
        resolveQualifiedNames(rootNode, languageVersionHandler)
        symbolFacade(rootNode, languageVersionHandler)
        val language = languageVersion.language
        usesDFA(languageVersion, rootNode, ruleSets, language)
        usesTypeResolution(languageVersion, rootNode, ruleSets, language)
        usesMultifile(rootNode, languageVersionHandler, ruleSets, language)

        val acus = listOf(rootNode)
        ruleSets.apply(acus, ctx, language)
    }

    private fun determineLanguage(ctx: RuleContext) {
        // If LanguageVersion of the source file is not known, make a
        // determination
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
