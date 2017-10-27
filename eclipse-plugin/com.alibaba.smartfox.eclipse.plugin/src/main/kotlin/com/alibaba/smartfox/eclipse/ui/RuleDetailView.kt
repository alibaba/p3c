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
import net.sourceforge.pmd.Rule
import org.eclipse.jface.action.Action
import org.eclipse.jface.action.Separator
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.part.ViewPart


/**
 *
 *
 * @author caikang
 * @date 2017/06/12
 */
open class RuleDetailView : ViewPart() {

    private lateinit var ruleDetailComposite: RuleDetailComposite

    override fun setFocus() {
    }

    override fun createPartControl(parent: Composite) {
        ruleDetailComposite = RuleDetailComposite(parent)
        initToolBar()
    }

    fun refresh(rule: Rule) {
        ruleDetailComposite.refresh(rule)
    }


    private fun initToolBar() {
        val bars = viewSite.actionBars
        val tm = bars.toolBarManager

        val rulesAction = object : Action("Show Rules") {
            override fun run() {
                val shell = Shell(Display.getDefault())
                shell.text = "All Rules"
                shell.layout = FillLayout()

                val sc = ScrolledComposite(shell, SWT.V_SCROLL or SWT.H_SCROLL)

                val rulesView = AllRulesView(sc)

                sc.expandHorizontal = true
                sc.expandVertical = true

                sc.content = rulesView.content

                shell.open()
            }
        }

        rulesAction.imageDescriptor = SmartfoxActivator.getImageDescriptor("icons/actions/rules.png")

        tm.add(Separator("Markers"))
        tm.add(rulesAction)
    }

    companion object {
        fun showAndGetView(): RuleDetailView {
            return PlatformUI.getWorkbench().activeWorkbenchWindow.activePage.showView(
                    "com.alibaba.smartfox.eclipse.ui.RuleDetailView", null,
                    IWorkbenchPage.VIEW_VISIBLE) as RuleDetailView
        }
    }
}
