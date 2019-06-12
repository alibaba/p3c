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
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.deprecation.DeprecationInspection
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

/**
 * @author caikang
 * @date 2016/12/08
 */
class AliDeprecationInspection : DeprecationInspection, AliBaseInspection {
    private val messageKey = "com.alibaba.p3c.idea.inspection.standalone.AliDeprecationInspection"

    constructor()
    /**
     * For Javassist
     */
    constructor(any: Any?) : this()

    init {
        IGNORE_INSIDE_DEPRECATED = true
        IGNORE_ABSTRACT_DEPRECATED_OVERRIDES = false
        IGNORE_IMPORT_STATEMENTS = false
        IGNORE_METHODS_OF_DEPRECATED = false
    }

    override fun getDisplayName(): String {
        return P3cBundle.getMessage("$messageKey.message")
    }

    override fun ruleName(): String {
        return "AvoidUseDeprecationApiRule"
    }

    override fun getShortName(): String {
        return "AliDeprecation"
    }

    override fun getStaticDescription(): String? {
        return P3cBundle.getMessage("$messageKey.desc")
    }

    override fun createOptionsPanel(): JComponent? {
        return null
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val p3cConfig = P3cConfig::class.java.getService()
        return when (p3cConfig.locale) {
            P3cConfig.localeEn -> super.buildVisitor(holder, isOnTheFly)
            else -> super.buildVisitor(DeprecationInspectionProblemsHolder(holder, isOnTheFly), isOnTheFly)
        }
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevels.CRITICAL
    }

    class DeprecationInspectionProblemsHolder(private val holder: ProblemsHolder, onTheFly: Boolean) : ProblemsHolder(
            holder.manager, holder.file, onTheFly) {

        override fun registerProblem(psiElement: PsiElement,
                @Nls descriptionTemplate: String,
                fixes: Array<LocalQuickFix>?) {
            holder.registerProblem(psiElement, getMessage(descriptionTemplate), *(fixes ?: emptyArray()))
        }

        override fun registerProblem(psiElement: PsiElement,
                @Nls descriptionTemplate: String,
                highlightType: ProblemHighlightType, fixes: Array<LocalQuickFix>?) {
            holder.registerProblem(psiElement, getMessage(descriptionTemplate), highlightType, *(fixes ?: emptyArray()))
        }

        override fun registerProblem(reference: PsiReference, descriptionTemplate: String,
                highlightType: ProblemHighlightType) {
            holder.registerProblem(reference, getMessage(descriptionTemplate), highlightType)
        }

        override fun registerProblemForReference(reference: PsiReference,
                highlightType: ProblemHighlightType, descriptionTemplate: String,
                fixes: Array<LocalQuickFix>?) {
            holder.registerProblemForReference(reference, highlightType, getMessage(descriptionTemplate),
                    *(fixes ?: emptyArray()))
        }

        override fun registerProblem(psiElement: PsiElement, rangeInElement: TextRange?,
                message: String, fixes: Array<LocalQuickFix>?) {
            holder.registerProblem(psiElement, rangeInElement, getMessage(message), *(fixes ?: emptyArray()))
        }

        override fun registerProblem(psiElement: PsiElement, message: String,
                highlightType: ProblemHighlightType, rangeInElement: TextRange?,
                fixes: Array<LocalQuickFix>?) {
            holder.registerProblem(psiElement, getMessage(message), highlightType, rangeInElement,
                    *(fixes ?: emptyArray()))
        }

        private fun getMessage(msg: String): String {
            return msg.replace("is deprecated", "已经过时了").replace("Default constructor in", "默认构造函数")
                    .replace("Overrides deprecated method in", "重写了过时的方法") + " #loc"
        }
    }

}
