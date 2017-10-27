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
package com.alibaba.smartfox.eclipse.ui

import com.alibaba.smartfox.eclipse.SmartfoxActivator
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.List


/**
 *
 * @author caikang
 * @date 2017/09/01
 */
class AllRulesView(parent: Composite) {
    val content = Composite(parent, SWT.NONE)

    private val ruleList = SmartfoxActivator.instance.ruleSets.allRules.toList()

    init {
        content.layout = FillLayout()
        val list = List(content, SWT.BORDER or SWT.SINGLE or SWT.V_SCROLL or SWT.PUSH or SWT.H_SCROLL)
        ruleList.forEach {
            list.add(it.message)
        }
        val ruleDetail = RuleDetailComposite(content, SWT.PUSH)
        list.addListener(SWT.Selection) {
            val index = list.selectionIndex
            val rule = ruleList.getOrNull(index) ?: return@addListener
            ruleDetail.refresh(rule)
        }
        list.select(0)
    }
}