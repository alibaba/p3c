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
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.javadoc.PsiDocToken
import com.siyeh.ig.InspectionGadgetsFix

/**
 *
 *
 * @author caikang
 * @date 2017/02/27
 */
object ClassMustHaveAuthorQuickFix : InspectionGadgetsFix(), AliQuickFix {

    val tag = "@author ${System.getProperty("user.name") ?: System.getenv("USER")}"

    override fun doFix(project: Project?, descriptor: ProblemDescriptor?) {
        descriptor ?: return
        val psiClass = descriptor.psiElement as? PsiClass ?: descriptor.psiElement?.parent as? PsiClass ?: return

        val document = psiClass.docComment
        val psiFacade = JavaPsiFacade.getInstance(project)
        val factory = psiFacade.elementFactory
        if (document == null) {
            val doc = factory.createDocCommentFromText("""
/**
 * $tag
 */
""")
            if (psiClass.isEnum) {
                psiClass.containingFile.addAfter(doc, psiClass.prevSibling)
            } else {
                psiClass.addBefore(doc, psiClass.firstChild)
            }
            return
        }

        val regex = Regex("Created by (.*) on (.*)\\.")
        for (line in document.descriptionElements) {
            if (line is PsiDocToken && line.text.contains(regex)) {
                val groups = regex.find(line.text)?.groups ?: continue
                val author = groups[1]?.value ?: continue
                val date = groups[2]?.value ?: continue
                document.addBefore(factory.createDocTagFromText("@date $date"), line)
                document.addBefore(factory.createDocTagFromText("@author $author"), line)
                line.delete()
                return
            }
        }

        if (document.tags.isNotEmpty()) {
            document.addBefore(factory.createDocTagFromText(tag), document.tags[0])
            return
        }

        document.add(factory.createDocTagFromText(tag))
    }

    override val ruleName: String
        get() = "ClassMustHaveAuthorRule"
    override val onlyOnThFly: Boolean
        get() = true

    override fun getName(): String {
        return P3cBundle.getMessage("com.alibaba.p3c.idea.quickfix.generate.author")
    }

}
