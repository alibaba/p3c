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

import com.alibaba.p3c.idea.i18n.P3cBundle
import com.alibaba.p3c.idea.quickfix.DecorateInspectionGadgetsFix
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.siyeh.ig.InspectionGadgetsFix
import com.siyeh.ig.style.CStyleArrayDeclarationInspection
import javax.swing.JComponent

/**
 *
 * Batch QuickFix Supported
 * @author caikang
 * @date 2017/02/26
 */
class AliArrayNamingShouldHaveBracketInspection : CStyleArrayDeclarationInspection, AliBaseInspection {
    constructor()
    /**
     * ForJavassist
     */
    constructor(any: Any?) : this()

    override fun ruleName(): String {
        return "ArrayNamingShouldHaveBracketRule"
    }

    override fun getDisplayName(): String {
        return RuleInspectionUtils.getRuleMessage(ruleName())
    }

    override fun getShortName(): String {
        return "AliArrayNamingShouldHaveBracket"
    }

    override fun getStaticDescription(): String? {
        return RuleInspectionUtils.getRuleStaticDescription(ruleName())
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return RuleInspectionUtils.getHighlightDisplayLevel(ruleName())
    }

    override fun createOptionsPanel(): JComponent? {
        return null
    }

    override fun buildErrorString(vararg infos: Any?): String {
        val info = infos[0]
        if (info is PsiMethod) {
            return displayName
        }
        val choice = if (info is PsiField) 1 else if (info is PsiParameter) 2 else 3
        return P3cBundle.message("com.alibaba.p3c.idea.inspection.rule.ArrayNamingShouldHaveBracketRule.errMsg", choice)
    }

    override fun buildFix(vararg infos: Any): InspectionGadgetsFix? {
        val fix = super.buildFix(*infos) ?: return null
        return DecorateInspectionGadgetsFix(fix,
                P3cBundle.getMessage("com.alibaba.p3c.idea.quickfix.ArrayNamingShouldHaveBracketRule"))
    }

    override fun manualBuildFix(psiElement: PsiElement, isOnTheFly: Boolean): LocalQuickFix? {
        return buildFix(psiElement)
    }
}
