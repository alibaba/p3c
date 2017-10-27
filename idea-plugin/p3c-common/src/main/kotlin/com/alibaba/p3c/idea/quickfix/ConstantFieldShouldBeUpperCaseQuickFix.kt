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
package com.alibaba.p3c.idea.quickfix

import com.alibaba.p3c.idea.i18n.P3cBundle
import com.google.common.base.Splitter
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiIdentifier

/**
 *
 *
 * @author caikang
 * @date 2017/02/28
 */
object ConstantFieldShouldBeUpperCaseQuickFix : AliQuickFix {
    val separator = '_'

    override fun getName(): String {
        return P3cBundle.getMessage("com.alibaba.p3c.idea.quickfix.field.to.upperCaseWithUnderscore")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val psiIdentifier = descriptor.psiElement as? PsiIdentifier ?: return
        val identifier = psiIdentifier.text
        val list = Splitter.on(separator).trimResults().omitEmptyStrings().splitToList(identifier)

        val resultName = list.joinToString(separator.toString()) {
            separateCamelCase(it).toUpperCase()
        }

        AliQuickFix.doQuickFix(resultName, project, psiIdentifier)
    }

    private fun separateCamelCase(name: String): String {
        val translation = StringBuilder()


        for (i in 0 until name.length - 1) {
            val character = name[i]
            val next = name[i + 1]
            if (Character.isUpperCase(character) && !next.isUpperCase() && translation.isNotEmpty()) {
                translation.append(separator)
            }
            if (character != separator) {
                translation.append(character)
            }
        }
        val last = name.last()
        if (last != separator) {
            translation.append(last)
        }
        return translation.toString()
    }

    override val ruleName: String
        get() = "ConstantFieldShouldBeUpperCaseRule"
    override val onlyOnThFly: Boolean
        get() = true

}
