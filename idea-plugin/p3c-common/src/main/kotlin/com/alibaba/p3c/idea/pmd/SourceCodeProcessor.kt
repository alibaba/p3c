/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package com.alibaba.p3c.idea.pmd

import com.alibaba.p3c.idea.component.AliProjectComponent.FileContext
import com.alibaba.p3c.idea.config.P3cConfig
import com.alibaba.p3c.idea.util.withLockNotInline
import com.alibaba.p3c.idea.util.withTryLock
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
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
import java.io.Reader
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

class SourceCodeProcessor(
    private val configuration: PMDConfiguration,
    private val document: Document,
    private val fileContext: FileContext,
    private val isOnTheFly: Boolean
) {

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

    private fun getRootNode(sourceCode: Reader, ruleSets: RuleSets, ctx: RuleContext): Node? {
        if (!smartFoxConfig.astCacheEnable) {
            return parseNode(ctx, ruleSets, sourceCode)
        }
        val node = getNode(ctx.sourceCodeFilename, isOnTheFly)
        if (node != null) {
            return node
        }
        if (document.lineCount > 3000 && isOnTheFly) {
            return null
        }
        val lock = fileContext.lock
        val readLock = lock.readLock()
        val writeLock = lock.writeLock()
        val ruleName = ruleSets.allRules.joinToString(",") { it.name }
        val fileName = ctx.sourceCodeFilename
        val readAction = {
            getNode(ctx.sourceCodeFilename, isOnTheFly)
        }
        val cacheNode = if (isOnTheFly) {
            readLock.withTryLock(50, MILLISECONDS, readAction)
        } else {
            val start = System.currentTimeMillis()
            LOG.info("rule:$ruleName,file:$fileName require read lock")
            readLock.withLockNotInline(readAction).also {
                LOG.info("rule:$ruleName,file:$fileName get result $it with read lock ,elapsed ${System.currentTimeMillis() - start}")
            }
        }
        if (cacheNode != null) {
            return cacheNode
        }
        val writeAction = {
            val finalNode = getNode(ctx.sourceCodeFilename, isOnTheFly)
            if (finalNode == null) {
                val start = System.currentTimeMillis()
                if (!isOnTheFly) {
                    LOG.info("rule:$ruleName,file:$fileName parse with write lock")
                }
                parseNode(ctx, ruleSets, sourceCode).also {
                    if (!isOnTheFly) {
                        LOG.info("rule:$ruleName,file:$fileName get result $it parse with write lock ,elapsed ${System.currentTimeMillis() - start}")
                    }
                }
            } else {
                finalNode
            }
        }
        return if (isOnTheFly) {
            writeLock.withTryLock(50, MILLISECONDS, writeAction)!!
        } else {
            writeLock.withLockNotInline(
                writeAction
            )!!
        }
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
        onlyTheFlyCache.put(ctx.sourceCodeFilename, rootNode)
        userTriggerNodeCache.put(ctx.sourceCodeFilename, rootNode)
        return rootNode
    }

    private fun symbolFacade(rootNode: Node, languageVersionHandler: LanguageVersionHandler) {
        TimeTracker.startOperation(TimedOperationCategory.SYMBOL_TABLE)
            .use { languageVersionHandler.getSymbolFacade(configuration.classLoader).start(rootNode) }
    }

    private fun resolveQualifiedNames(rootNode: Node, handler: LanguageVersionHandler) {
        TimeTracker.startOperation(TimedOperationCategory.QUALIFIED_NAME_RESOLUTION)
            .use { handler.getQualifiedNameResolutionFacade(configuration.classLoader).start(rootNode) }
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

    private fun usesTypeResolution(
        languageVersion: LanguageVersion, rootNode: Node, ruleSets: RuleSets,
        language: Language
    ) {

        if (ruleSets.usesTypeResolution(language)) {
            TimeTracker.startOperation(TimedOperationCategory.TYPE_RESOLUTION).use { to ->
                languageVersion.languageVersionHandler.getTypeResolutionFacade(configuration.classLoader)
                    .start(rootNode)
            }
        }
    }

    private fun usesMultifile(
        rootNode: Node, languageVersionHandler: LanguageVersionHandler, ruleSets: RuleSets,
        language: Language
    ) {

        if (ruleSets.usesMultifile(language)) {
            TimeTracker.startOperation(TimedOperationCategory.MULTIFILE_ANALYSIS)
                .use { languageVersionHandler.multifileFacade.start(rootNode) }
        }
    }

    private fun processSource(sourceCode: Reader, ruleSets: RuleSets, ctx: RuleContext) {
        val languageVersion = ctx.languageVersion
        val languageVersionHandler = languageVersion.languageVersionHandler

        val rootNode = getRootNode(sourceCode, ruleSets, ctx) ?: return
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
        private lateinit var onlyTheFlyCache: Cache<String, Node>
        private lateinit var userTriggerNodeCache: Cache<String, Node>
        private val LOG = Logger.getInstance(SourceCodeProcessor::class.java)

        init {
            reInitNodeCache(smartFoxConfig.astCacheTime)
        }

        fun reInitNodeCache(expireTime: Long) {
            onlyTheFlyCache = CacheBuilder.newBuilder().concurrencyLevel(16)
                .expireAfterWrite(expireTime, TimeUnit.MILLISECONDS)
                .maximumSize(300)
                .build<String, Node>()!!

            userTriggerNodeCache = CacheBuilder.newBuilder().concurrencyLevel(16)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(300)
                .build<String, Node>()!!
        }

        fun invalidateCache(file: String) {
            onlyTheFlyCache.invalidate(file)
            userTriggerNodeCache.invalidate(file)
        }

        fun invalidUserTrigger(file: String) {
            userTriggerNodeCache.invalidate(file)
        }

        fun invalidateAll() {
            onlyTheFlyCache.invalidateAll()
            userTriggerNodeCache.invalidateAll()
        }

        fun getNode(file: String, isOnTheFly: Boolean): Node? {
            return if (isOnTheFly) onlyTheFlyCache.getIfPresent(file) else userTriggerNodeCache.getIfPresent(file)
        }
    }
}
