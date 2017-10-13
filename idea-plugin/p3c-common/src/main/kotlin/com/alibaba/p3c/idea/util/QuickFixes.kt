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

import com.alibaba.p3c.idea.quickfix.AvoidStartWithDollarAndUnderLineNamingQuickFix
import com.alibaba.p3c.idea.quickfix.ClassMustHaveAuthorQuickFix
import com.alibaba.p3c.idea.quickfix.ConstantFieldShouldBeUpperCaseQuickFix
import com.alibaba.p3c.idea.quickfix.LowerCamelCaseVariableNamingQuickFix
import com.alibaba.p3c.idea.quickfix.VmQuietReferenceQuickFix
import com.intellij.codeInspection.LocalQuickFix

/**
 *
 *
 * @author caikang
 * @date 2017/02/06
 */
object QuickFixes {
    val quickFixes = mutableMapOf(VmQuietReferenceQuickFix.ruleName to VmQuietReferenceQuickFix,
            ClassMustHaveAuthorQuickFix.ruleName to ClassMustHaveAuthorQuickFix,
            ConstantFieldShouldBeUpperCaseQuickFix.ruleName to ConstantFieldShouldBeUpperCaseQuickFix,
            AvoidStartWithDollarAndUnderLineNamingQuickFix.ruleName to AvoidStartWithDollarAndUnderLineNamingQuickFix,
            LowerCamelCaseVariableNamingQuickFix.ruleName to LowerCamelCaseVariableNamingQuickFix)

    fun getQuickFix(rule: String, isOnTheFly: Boolean): LocalQuickFix? {
        val quickFix = quickFixes[rule] ?: return null
        if (!quickFix.onlyOnThFly) {
            return quickFix
        }
        if (!isOnTheFly && quickFix.onlyOnThFly) {
            return null
        }
        return quickFix
    }
}
