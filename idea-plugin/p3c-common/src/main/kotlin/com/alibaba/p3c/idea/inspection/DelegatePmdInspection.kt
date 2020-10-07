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
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

import org.jetbrains.annotations.Nls

/**
 * @author caikang
 * @date 2017/02/28
 */
class DelegatePmdInspection : LocalInspectionTool(), AliBaseInspection, PmdRuleInspectionIdentify {

    private val ruleName: String? = null

    private val aliPmdInspection: AliPmdInspection

    init {
        aliPmdInspection = AliPmdInspection(ruleName!!)
    }

    override fun runForWholeFile(): Boolean {
        return aliPmdInspection.runForWholeFile()
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager,
            isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return aliPmdInspection.checkFile(file, manager, isOnTheFly)
    }

    override fun getStaticDescription(): String? {
        return aliPmdInspection.staticDescription
    }

    override fun ruleName(): String {
        return ruleName!!
    }

    @Nls
    override fun getDisplayName(): String {
        return aliPmdInspection.displayName
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return aliPmdInspection.defaultLevel
    }

    @Nls
    override fun getGroupDisplayName(): String {
        return aliPmdInspection.groupDisplayName
    }

    override fun isEnabledByDefault(): Boolean {
        return aliPmdInspection.isEnabledByDefault
    }

    override fun getShortName(): String {
        return aliPmdInspection.shortName
    }

    override fun isSuppressedFor(element: PsiElement): Boolean {
        return aliPmdInspection.isSuppressedFor(element)
    }
}
