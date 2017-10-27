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
package com.alibaba.p3c.idea.inspection.standalone

import com.alibaba.p3c.idea.config.P3cConfig
import com.alibaba.p3c.idea.i18n.P3cBundle
import com.alibaba.p3c.idea.inspection.AliBaseInspection
import com.alibaba.p3c.idea.util.HighlightDisplayLevels
import com.alibaba.smartfox.idea.common.util.getService
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.impl.analysis.HighlightMessageUtil
import com.intellij.codeInsight.daemon.impl.analysis.HighlightUtil
import com.intellij.codeInsight.daemon.impl.analysis.JavaHighlightUtil
import com.intellij.codeInsight.daemon.impl.quickfix.AccessStaticViaInstanceFix
import com.intellij.codeInsight.daemon.impl.quickfix.RemoveUnusedVariableUtil
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.accessStaticViaInstance.AccessStaticViaInstance
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.JavaResolveResult
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiPackage
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiSubstitutor
import java.util.ArrayList

/**
 * @author caikang
 * @date 2016/12/08
 */
class AliAccessStaticViaInstanceInspection : AccessStaticViaInstance, AliBaseInspection {
    val messageKey = "com.alibaba.p3c.idea.inspection.standalone.AliAccessStaticViaInstanceInspection"

    constructor()
    /**
     * For Javassist
     */
    constructor(any: Any?) : this()

    override fun getDisplayName(): String {
        return P3cBundle.getMessage("$messageKey.message")
    }

    override fun getStaticDescription(): String? {
        return P3cBundle.getMessage("$messageKey.desc")
    }

    override fun ruleName(): String {
        return "AvoidAccessStaticViaInstanceRule"
    }

    override fun getShortName(): String {
        return "AliAccessStaticViaInstance"
    }

    override fun createAccessStaticViaInstanceFix(expr: PsiReferenceExpression,
            onTheFly: Boolean,
            result: JavaResolveResult): AccessStaticViaInstanceFix {
        return object : AccessStaticViaInstanceFix(expr, result, onTheFly) {
            val fixKey = "com.alibaba.p3c.idea.quickfix.standalone.AliAccessStaticViaInstanceInspection"
            internal val text = calcText(result.element as PsiMember, result.substitutor)

            override fun getText(): String {
                return text
            }

            private fun calcText(member: PsiMember, substitutor: PsiSubstitutor): String {
                val aClass = member.containingClass ?: return ""
                val p3cConfig = P3cConfig::class.java.getService()
                return when (p3cConfig.locale) {
                    P3cConfig.localeZh -> String.format(P3cBundle.getMessage(fixKey),
                            HighlightUtil.formatClass(aClass, false),
                            HighlightUtil.formatClass(aClass), HighlightMessageUtil.getSymbolName(member, substitutor))
                    else -> String.format(P3cBundle.getMessage(fixKey), HighlightUtil.formatClass(aClass),
                            HighlightMessageUtil.getSymbolName(member, substitutor),
                            HighlightUtil.formatClass(aClass, false))
                }
            }
        }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitReferenceExpression(expression: PsiReferenceExpression) {
                checkAccessStaticMemberViaInstanceReference(expression, holder, isOnTheFly)
            }
        }
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevels.BLOCKER
    }

    private fun checkAccessStaticMemberViaInstanceReference(expr: PsiReferenceExpression, holder: ProblemsHolder,
            onTheFly: Boolean) {
        val result = expr.advancedResolve(false)
        val resolved = result.element as? PsiMember ?: return

        val qualifierExpression = expr.qualifierExpression ?: return

        if (qualifierExpression is PsiReferenceExpression) {
            val qualifierResolved = qualifierExpression.resolve()
            if (qualifierResolved is PsiClass || qualifierResolved is PsiPackage) {
                return
            }
        }
        if (!resolved.hasModifierProperty(PsiModifier.STATIC)) {
            return
        }

        val description = String.format(P3cBundle.getMessage(
                "$messageKey.errMsg"),
                "${JavaHighlightUtil.formatType(qualifierExpression.type)}.${HighlightMessageUtil.getSymbolName(
                        resolved, result.substitutor)}")
        if (!onTheFly) {
            if (RemoveUnusedVariableUtil.checkSideEffects(qualifierExpression, null, ArrayList<PsiElement>())) {
                holder.registerProblem(expr, description)
                return
            }
        }
        holder.registerProblem(expr, description, createAccessStaticViaInstanceFix(expr, onTheFly, result))
    }

}
