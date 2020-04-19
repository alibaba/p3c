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
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.CommonClassNames
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.util.IncorrectOperationException
import com.siyeh.ig.BaseInspection
import com.siyeh.ig.BaseInspectionVisitor
import com.siyeh.ig.InspectionGadgetsFix
import com.siyeh.ig.PsiReplacementUtil
import com.siyeh.ig.fixes.EqualityToEqualsFix
import com.siyeh.ig.psiutils.ComparisonUtils
import com.siyeh.ig.psiutils.TypeUtils
import org.jetbrains.annotations.NonNls

/**
 *
 * Batch QuickFix Supported
 * @author caikang
 * @date 2017/02/27
 */
class AliWrapperTypeEqualityInspection : BaseInspection, AliBaseInspection {
    constructor()

    /**
     * For Javassist
     */
    constructor(any: Any?) : this()

    val familyName = "$replaceWith equals"

    override fun buildErrorString(vararg infos: Any?): String {
        return P3cBundle.getMessage("com.alibaba.p3c.idea.inspection.rule.WrapperTypeEqualityRule.errMsg")
    }

    override fun buildVisitor(): BaseInspectionVisitor {
        return ObjectComparisonVisitor()
    }

    override fun ruleName(): String {
        return "WrapperTypeEqualityRule"
    }

    override fun getDisplayName(): String {
        return RuleInspectionUtils.getRuleMessage(ruleName())
    }

    override fun getShortName(): String {
        return "AliWrapperTypeEquality"
    }

    override fun getStaticDescription(): String? {
        return RuleInspectionUtils.getRuleStaticDescription(ruleName())
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return RuleInspectionUtils.getHighlightDisplayLevel(ruleName())
    }

    public override fun buildFix(vararg infos: Any): InspectionGadgetsFix? {
        if (infos.isEmpty()) {
            return DecorateInspectionGadgetsFix(EqualityToEqualsFix(), familyName)
        }
        val type = infos[0] as PsiArrayType
        val componentType = type.componentType
        val fix = ArrayEqualityFix(componentType is PsiArrayType, familyName)
        return DecorateInspectionGadgetsFix(fix, fix.name, familyName)
    }

    private inner class ObjectComparisonVisitor : BaseInspectionVisitor() {
        override fun visitBinaryExpression(expression: PsiBinaryExpression) {
            if (!ComparisonUtils.isEqualityComparison(expression)) {
                return
            }
            checkForWrapper(expression)
        }

        private fun checkForWrapper(expression: PsiBinaryExpression) {
            val rhs = expression.rOperand ?: return
            val lhs = expression.lOperand
            if (!isWrapperType(lhs) || !isWrapperType(rhs)) {
                return
            }
            registerError(expression.operationSign)
        }

        private fun isWrapperType(expression: PsiExpression): Boolean {
            if (hasNumberType(expression)) {
                return true
            }
            return TypeUtils.expressionHasTypeOrSubtype(expression, CommonClassNames.JAVA_LANG_BOOLEAN)
                || TypeUtils.expressionHasTypeOrSubtype(expression, CommonClassNames.JAVA_LANG_CHARACTER)
        }

        private fun hasNumberType(expression: PsiExpression): Boolean {
            return TypeUtils.expressionHasTypeOrSubtype(expression, CommonClassNames.JAVA_LANG_NUMBER)
        }
        /**
         * checkForNumber end
         */

    }

    private class ArrayEqualityFix(private val deepEquals: Boolean, private val familyName: String) :
        InspectionGadgetsFix() {

        override fun getName(): String {
            if (deepEquals) {
                return "$replaceWith 'Arrays.deepEquals()'"
            } else {
                return "$replaceWith 'Arrays.equals()'"
            }
        }

        override fun getFamilyName(): String {
            return familyName
        }

        @Throws(IncorrectOperationException::class)
        override fun doFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val parent = element.parent as? PsiBinaryExpression ?: return
            val tokenType = parent.operationTokenType
            @NonNls
            val newExpressionText = StringBuilder()
            if (JavaTokenType.NE == tokenType) {
                newExpressionText.append('!')
            } else if (JavaTokenType.EQEQ != tokenType) {
                return
            }
            if (deepEquals) {
                newExpressionText.append("java.util.Arrays.deepEquals(")
            } else {
                newExpressionText.append("java.util.Arrays.equals(")
            }
            newExpressionText.append(parent.lOperand.text)
            newExpressionText.append(',')
            val rhs = parent.rOperand ?: return
            newExpressionText.append(rhs.text)
            newExpressionText.append(')')
            PsiReplacementUtil.replaceExpressionAndShorten(
                parent,
                newExpressionText.toString()
            )
        }
    }

    override fun manualBuildFix(psiElement: PsiElement, isOnTheFly: Boolean): LocalQuickFix? {
        val expression = psiElement.parent as? PsiBinaryExpression ?: return null
        val rhs = expression.rOperand ?: return null
        val lhs = expression.lOperand
        val lhsType = lhs.type
        if (lhsType !is PsiArrayType || rhs.type !is PsiArrayType) {
            return buildFix()
        }
        return buildFix(lhsType)
    }

    companion object {
        val replaceWith = P3cBundle.getMessage("com.alibaba.p3c.idea.quickfix.replace.with")
    }
}
