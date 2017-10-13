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
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import com.siyeh.HardcodedMethodConstants
import com.siyeh.ig.BaseInspectionVisitor
import com.siyeh.ig.InspectionGadgetsFix
import com.siyeh.ig.psiutils.TypeUtils
import com.siyeh.ig.style.LiteralAsArgToStringEqualsInspection
import org.jetbrains.annotations.NonNls

/**
 *
 * Batch QuickFix Supported
 * @author caikang
 * @date 2017/02/27
 */
class AliEqualsAvoidNullInspection : LiteralAsArgToStringEqualsInspection, AliBaseInspection {
    constructor()
    /**
     * For Javassist
     */
    constructor(any: Any?) : this()

    override fun ruleName(): String {
        return "EqualsAvoidNullRule"
    }

    override fun getDisplayName(): String {
        return RuleInspectionUtils.getRuleMessage(ruleName())
    }

    override fun buildErrorString(vararg infos: Any?): String {
        val methodName = infos[0] as String
        return String.format(P3cBundle.getMessage("com.alibaba.p3c.idea.inspection.rule.AliEqualsAvoidNull.errMsg"),
                methodName)
    }

    override fun getShortName(): String {
        return "AliEqualsAvoidNull"
    }

    override fun getStaticDescription(): String? {
        return RuleInspectionUtils.getRuleStaticDescription(ruleName())
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return RuleInspectionUtils.getHighlightDisplayLevel(ruleName())
    }

    override fun buildVisitor(): BaseInspectionVisitor {
        return LiteralAsArgToEqualsVisitor()
    }

    private class LiteralAsArgToEqualsVisitor : BaseInspectionVisitor() {

        override fun visitMethodCallExpression(
                expression: PsiMethodCallExpression) {
            super.visitMethodCallExpression(expression)
            val methodExpression = expression.methodExpression
            @NonNls val methodName = methodExpression.referenceName
            if (HardcodedMethodConstants.EQUALS != methodName && HardcodedMethodConstants.EQUALS_IGNORE_CASE != methodName) {
                return
            }
            val argList = expression.argumentList
            val args = argList.expressions
            if (args.size != 1) {
                return
            }
            val argument = args[0]
            val argumentType = argument.type ?: return
            if (argument !is PsiLiteralExpression && !isConstantField(argument)) {
                return
            }
            if (!TypeUtils.isJavaLangString(argumentType)) {
                return
            }
            val target = methodExpression.qualifierExpression
            if (target is PsiLiteralExpression || isConstantField(argument)) {
                return
            }
            registerError(argument, methodName)
        }

        private fun isConstantField(argument: PsiExpression): Boolean {
            if (argument !is PsiReferenceExpression) {
                return false
            }
            val psiField = argument.resolve() as? PsiField ?: return false
            val modifierList = psiField.modifierList ?: return false
            return modifierList.hasModifierProperty("final") && modifierList.hasModifierProperty("static")
        }
    }

    override fun buildFix(vararg infos: Any): InspectionGadgetsFix? {
        val fix = super.buildFix(*infos) ?: return null
        return DecorateInspectionGadgetsFix(fix,
                P3cBundle.getMessage("com.alibaba.p3c.idea.quickfix.AliEqualsAvoidNull"))
    }

    override fun manualBuildFix(psiElement: PsiElement, isOnTheFly: Boolean): LocalQuickFix? {
        return buildFix(psiElement)
    }

    override fun manualParsePsiElement(psiFile: PsiFile, manager: InspectionManager, start: Int, end: Int): PsiElement {
        return psiFile.findElementAt(start)!!.parent.parent
    }
}
