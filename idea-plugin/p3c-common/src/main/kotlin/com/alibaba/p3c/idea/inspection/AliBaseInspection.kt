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
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * @author caikang
 * @date 2016/12/08
 */
interface AliBaseInspection {

    /**
     * ruleName

     * @return ruleName
     */
    fun ruleName(): String

    /**
     * display info for inspection

     * @return display
     */
    fun getDisplayName(): String

    /**
     * group display info for inspection

     * @return group display
     */
    fun getGroupDisplayName(): String

    /**
     * inspection enable by default

     * @return true -> enable
     */
    fun isEnabledByDefault(): Boolean

    /**
     * default inspection level

     * @return level
     */
    fun getDefaultLevel(): HighlightDisplayLevel

    /**
     * inspection short name

     * @return shor name
     */
    fun getShortName(): String

    fun manualBuildFix(psiElement: PsiElement, isOnTheFly: Boolean): LocalQuickFix? = null

    fun manualParsePsiElement(psiFile: PsiFile, manager: InspectionManager,
            start: Int, end: Int): PsiElement {
        return psiFile.findElementAt(start)!!
    }

    companion object {
        val GROUP_NAME = "Ali-Check"
    }
}
