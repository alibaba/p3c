/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.p3c.idea.pmd

import com.google.common.base.Throwables
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiFile
import net.sourceforge.pmd.PMDConfiguration
import net.sourceforge.pmd.PMDException
import net.sourceforge.pmd.Report
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.RuleSetFactory
import net.sourceforge.pmd.RuleSets
import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.RulesetsFactoryUtils
import net.sourceforge.pmd.util.ResourceLoader
import java.io.IOException
import java.io.StringReader

/**
 * @author caikang
 * @date 2016/12/11
 */
class AliPmdProcessor(val rule: Rule) {
    private val ruleSetFactory: RuleSetFactory
    private val configuration = PMDConfiguration()

    init {
        ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration, ResourceLoader())
    }

    fun processFile(psiFile: PsiFile): List<RuleViolation> {
        configuration.setSourceEncoding(psiFile.virtualFile.charset.name())
        configuration.inputPaths = psiFile.virtualFile.canonicalPath
        val document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile) ?: return emptyList()
        if (document.lineCount > 10000) {
            return emptyList()
        }
        val ctx = RuleContext()
        val processor = SourceCodeProcessor(configuration)
        val niceFileName = psiFile.virtualFile.canonicalPath!!
        val report = Report.createReport(ctx, niceFileName)
        val ruleSets = RuleSets()

        val ruleSet = ruleSetFactory.createSingleRuleRuleSet(rule)
        ruleSets.addRuleSet(ruleSet)
        LOG.debug("Processing " + ctx.sourceCodeFilename)
        try {
            ctx.languageVersion = null
            processor.processSourceCode(StringReader(document.text), ruleSets, ctx)
        } catch (pmde: PMDException) {
            LOG.debug("Error while processing file: $niceFileName", pmde.cause)
            report.addError(Report.ProcessingError(pmde, niceFileName))
        } catch (ioe: IOException) {
            LOG.error("Unable to read source file: $niceFileName", ioe)
        } catch (re: RuntimeException) {
            val root = Throwables.getRootCause(re)
            if (root !is ApplicationUtil.CannotRunReadActionException) {
                LOG.error("RuntimeException while processing file: $niceFileName", re)
            }
        }
        return ctx.report.toList()
    }

    companion object {
        private val LOG = Logger.getInstance(AliPmdProcessor::class.java)
    }

}
