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

import com.alibaba.smartfox.eclipse.pmd.RulePriority
import com.alibaba.smartfox.eclipse.ui.pmd.ContentBuilder
import com.alibaba.smartfox.eclipse.ui.pmd.StringArranger
import net.sourceforge.pmd.Rule
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite

/**
 *
 * @author caikang
 * @date 2017/09/01
 */
class RuleDetailComposite(parent: Composite, style: Int = SWT.NONE) {
    private val viewField: StyledText
    private val contentBuilder = ContentBuilder()
    private val arranger = StringArranger("   ")
    private val panel = Composite(parent, style)

    init {
        panel.layout = FillLayout()

        viewField = StyledText(panel, SWT.BORDER or SWT.H_SCROLL or SWT.V_SCROLL)
        viewField.wordWrap = true
        viewField.tabs = 20
        viewField.text = "Select a result item to show rule detail."
        viewField.editable = false
    }

    fun refresh(rule: Rule) {
        contentBuilder.clear()
        viewField.text = ""
        contentBuilder.addHeading("Name")
        contentBuilder.addText(rule.name)
        contentBuilder.addHeading("Severity")
        contentBuilder.addText(RulePriority.valueOf(rule.priority.priority).title)
        contentBuilder.addHeading("Message")
        contentBuilder.addText(rule.message)
        if (!rule.description.isNullOrBlank()) {
            contentBuilder.addHeading("Description")
            contentBuilder.addRawText(arranger.format(rule.description).toString())
        }

        val examples = rule.examples
        if (examples.isEmpty()) {
            contentBuilder.showOn(viewField)
            return
        }

        contentBuilder.setLanguage(rule.language)

        contentBuilder.addHeading("Examples")
        contentBuilder.addText("")
        for (example in rule.examples) {
            contentBuilder.addCode(example.trim { it <= ' ' })
            contentBuilder.addText("")
        }
        contentBuilder.showOn(viewField)

        if (contentBuilder.hasLinks()) {
            contentBuilder.addLinkHandler(viewField)
        }
        viewField.update()
    }
}