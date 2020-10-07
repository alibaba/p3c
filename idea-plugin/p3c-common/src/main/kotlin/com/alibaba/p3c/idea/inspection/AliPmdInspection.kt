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
package com.alibaba.p3c.idea.inspection

import com.alibaba.p3c.idea.inspection.AliLocalInspectionToolProvider.ShouldInspectChecker
import com.alibaba.p3c.idea.util.NumberConstants
import com.alibaba.p3c.idea.util.QuickFixes
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import net.sourceforge.pmd.Rule
import org.jetbrains.annotations.Nls

/**
 * @author caikang
 * @date 2016/12/16
 */
class AliPmdInspection(private val ruleName: String) : LocalInspectionTool(),
    AliBaseInspection,
    PmdRuleInspectionIdentify {
    override fun manualBuildFix(psiElement: PsiElement, isOnTheFly: Boolean): LocalQuickFix? {
        return QuickFixes.getQuickFix(ruleName, isOnTheFly)
    }

    private val staticDescription: String = RuleInspectionUtils.getRuleStaticDescription(ruleName)

    private val displayName: String

    private val shouldInspectChecker: ShouldInspectChecker

    private val defaultLevel: HighlightDisplayLevel

    private val rule: Rule

    init {
        val ruleInfo = AliLocalInspectionToolProvider.ruleInfoMap[ruleName]!!
        shouldInspectChecker = ruleInfo.shouldInspectChecker
        rule = ruleInfo.rule
        displayName = rule.message
        defaultLevel = RuleInspectionUtils.getHighlightDisplayLevel(rule.priority)
    }

    override fun runForWholeFile(): Boolean {
        return true
    }

    override fun checkFile(
        file: PsiFile, manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (!shouldInspectChecker.shouldInspect(file)) {
            return null
        }
        return AliPmdInspectionInvoker.invokeInspection(file, manager, rule, isOnTheFly)
    }

    override fun getStaticDescription(): String? {
        return staticDescription
    }

    override fun ruleName(): String {
        return ruleName
    }

    @Nls
    override fun getDisplayName(): String {
        return displayName
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return defaultLevel
    }

    @Nls
    override fun getGroupDisplayName(): String {
        return AliBaseInspection.GROUP_NAME
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun getShortName(): String {

        var shortName = "Alibaba" + ruleName
        val index = shortName.lastIndexOf("Rule")
        if (index > NumberConstants.INDEX_0) {
            shortName = shortName.substring(NumberConstants.INDEX_0, index)
        }
        return shortName
    }
}
