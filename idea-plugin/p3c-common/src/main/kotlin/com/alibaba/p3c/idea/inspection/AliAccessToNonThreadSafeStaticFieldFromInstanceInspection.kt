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

import com.alibaba.p3c.idea.i18n.P3cBundle
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.siyeh.ig.threading.AccessToNonThreadSafeStaticFieldFromInstanceInspection

/**
 * @author caikang
 * @date 2016/12/08
 */
class AliAccessToNonThreadSafeStaticFieldFromInstanceInspection
    : AccessToNonThreadSafeStaticFieldFromInstanceInspection,
        AliBaseInspection {
    constructor()
    /**
     * For Javassist
     */
    constructor(any: Any?) : this()

    init {
        nonThreadSafeClasses.clear()
        nonThreadSafeClasses.add("java.text.SimpleDateFormat")
    }

    override fun ruleName(): String {
        return "AvoidCallStaticSimpleDateFormatRule"
    }

    override fun getDisplayName(): String {
        return RuleInspectionUtils.getRuleMessage(ruleName())
    }

    override fun buildErrorString(vararg infos: Any): String {
        return P3cBundle.getMessage("com.alibaba.p3c.idea.inspection.rule.AvoidCallStaticSimpleDateFormatRule.errMsg")
    }

    override fun getStaticDescription(): String? {
        return RuleInspectionUtils.getRuleStaticDescription(ruleName())
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return RuleInspectionUtils.getHighlightDisplayLevel(ruleName())
    }
}
