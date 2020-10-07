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

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.Nls

/**
 *
 * @author caikang
 * @date 2017/07/19
 */
class DelegateLocalInspectionTool : LocalInspectionTool(), AliBaseInspection {

    private val forJavassist: LocalInspectionTool? = null

    private val localInspectionTool: LocalInspectionTool

    init {
        localInspectionTool = forJavassist ?: throw IllegalStateException()
    }

    override fun runForWholeFile(): Boolean {
        return localInspectionTool.runForWholeFile()
    }

    override fun checkFile(
        file: PsiFile, manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return localInspectionTool.checkFile(file, manager, isOnTheFly)
    }

    override fun getStaticDescription(): String? {
        return localInspectionTool.staticDescription
    }

    override fun ruleName(): String {
        return (localInspectionTool as AliBaseInspection).ruleName()
    }

    @Nls
    override fun getDisplayName(): String {
        return localInspectionTool.displayName
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return localInspectionTool.defaultLevel
    }

    @Nls
    override fun getGroupDisplayName(): String {
        return AliBaseInspection.GROUP_NAME
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun getShortName(): String {
        return localInspectionTool.shortName
    }

    override fun isSuppressedFor(element: PsiElement): Boolean {
        return localInspectionTool.isSuppressedFor(element)
    }

    override fun buildVisitor(
        holder: ProblemsHolder, isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        if (!AliLocalInspectionToolProvider.javaShouldInspectChecker.shouldInspect(holder.file)) {
            return PsiElementVisitor.EMPTY_VISITOR
        }
        return localInspectionTool.buildVisitor(holder, isOnTheFly, session)
    }
}