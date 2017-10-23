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
package com.alibaba.p3c.idea.util

import com.alibaba.p3c.pmd.lang.java.rule.comment.AvoidCommentBehindStatementRule
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.PsiKeyword
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.ElementType

/**
 *
 *
 * @author caikang
 * @date 2017/03/16
6
 */
object ProblemsUtils {
    private val highlightLineRules = setOf(AvoidCommentBehindStatementRule::class.java.simpleName)

    fun createProblemDescriptorForPmdRule(psiFile: PsiFile, manager: InspectionManager, isOnTheFly: Boolean,
            ruleName: String, desc: String, start: Int, end: Int,
            checkLine: Int = 0,
            quickFix: (PsiElement) -> LocalQuickFix? = {
                QuickFixes.getQuickFix(ruleName, isOnTheFly)
            }): ProblemDescriptor? {
        val document = FileDocumentManager.getInstance().getDocument(psiFile.virtualFile) ?: return null
        if (highlightLineRules.contains(ruleName) && checkLine <= document.lineCount) {
            val lineNumber = if (start >= document.textLength) {
                document.lineCount - 1
            } else {
                document.getLineNumber(start)
            }
            val textRange = TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber))
            return createTextRangeProblem(manager, textRange, isOnTheFly, psiFile, ruleName, desc)
        }
        if (psiFile.virtualFile.canonicalPath!!.endsWith(".vm")) {
            return createTextRangeProblem(manager, TextRange(start, end), isOnTheFly, psiFile, ruleName, desc)
        }
        var psiElement = psiFile.findElementAt(start) ?: return null

        psiElement = transform(psiElement) ?: return null
        var endElement = if (start == end) psiElement else getEndElement(psiFile, psiElement, end)
        if (psiElement != endElement && endElement.parent is PsiField) {
            psiElement = endElement
        }
        if (endElement is PsiWhiteSpace) {
            endElement = psiElement
        }
        if (psiElement is PsiWhiteSpace) {
            val textRange = TextRange(start, end)
            return createTextRangeProblem(manager, textRange, isOnTheFly, psiFile, ruleName, desc)
        }

        if (psiElement.textRange.startOffset >= endElement.textRange.endOffset) {
            if (!(psiElement is PsiFile && endElement is PsiFile)) {
                return null
            }
            endElement = psiElement
        }
        return manager.createProblemDescriptor(psiElement, endElement,
                desc, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly,
                quickFix(psiElement))
    }

    private fun getEndElement(psiFile: PsiFile, psiElement: PsiElement, endOffset: Int): PsiElement {
        var endElement = psiFile.findElementAt(endOffset)
        if (endElement is PsiJavaToken && endElement.tokenType === ElementType.SEMICOLON) {
            endElement = psiFile.findElementAt(endOffset - 1)
        }
        if (endElement is PsiIdentifier) {
            return endElement
        }
        if (psiElement is PsiIdentifier) {
            return psiElement
        }
        if (endElement == null || endElement is PsiWhiteSpace
                || psiElement.textRange.startOffset >= endElement.textRange.endOffset) {
            endElement = psiElement
        }
        return endElement
    }

    private fun transform(element: PsiElement): PsiElement? {
        var psiElement: PsiElement? = element
        while (psiElement is PsiWhiteSpace) {
            psiElement = psiElement.getNextSibling()
        }
        if (psiElement == null) {
            return null
        }
        if (psiElement is PsiKeyword && psiElement.text != null && (ObjectConstants.CLASS_LITERAL == psiElement.text
                || ObjectConstants.INTERFACE_LITERAL == psiElement.text
                || ObjectConstants.ENUM_LITERAL == psiElement.text) && psiElement.parent is PsiClass) {
            val parent = psiElement.parent as PsiClass
            val identifier = parent.nameIdentifier
            return identifier ?: psiElement
        }
        return psiElement
    }

    private fun createTextRangeProblem(manager: InspectionManager, textRange: TextRange, isOnTheFly: Boolean,
            psiFile: PsiFile, ruleName: String, desc: String,
            quickFix: () -> LocalQuickFix? = {
                QuickFixes.getQuickFix(ruleName, isOnTheFly)
            }): ProblemDescriptor {

        return manager.createProblemDescriptor(psiFile, textRange,
                desc, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly, quickFix())
    }
}
