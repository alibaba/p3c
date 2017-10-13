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
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiIdentifier
import org.apache.commons.lang3.StringUtils

/**
 *
 *
 * @author caikang
 * @date 2017/02/28
 */
object AvoidStartWithDollarAndUnderLineNamingQuickFix : AliQuickFix {
    override val ruleName: String
        get() = "AvoidStartWithDollarAndUnderLineNamingRule"
    override val onlyOnThFly: Boolean
        get() = true

    override fun getName(): String {
        return P3cBundle.getMessage("com.alibaba.p3c.idea.quickfix.delete_$")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val psiIdentifier = descriptor.psiElement as? PsiIdentifier ?: return
        val identifier = psiIdentifier.text
        val resultName = StringUtils.replacePattern(identifier, "^[\$_]+", "")
        if (resultName.toLongOrNull() != null) {
            return
        }
        AliQuickFix.doQuickFix(resultName, project, psiIdentifier)
    }
}
