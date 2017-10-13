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
package com.alibaba.smartfox.eclipse.handler

import com.alibaba.smartfox.eclipse.SmartfoxActivator
import com.alibaba.smartfox.eclipse.message.P3cBundle
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.commands.IElementUpdater
import org.eclipse.ui.menus.UIElement

/**
 *
 *
 * @author caikang
 * @date 2017/06/21
 */
class SwitchLanguageHandler : AbstractHandler(), IElementUpdater {
    val handlerKey = "com.alibaba.smartfox.eclipse.handler.SwitchLanguageHandler"
    val textKey = "$handlerKey.text.cur_"

    override fun execute(executionEvent: ExecutionEvent): Any? {
        SmartfoxActivator.instance.toggleLocale()
        if (!MessageDialog.openConfirm(null, "Tips",
                P3cBundle.getMessage("$handlerKey.success.${SmartfoxActivator.instance.locale}"))) {
            return null
        }
        PlatformUI.getWorkbench().restart()
        return null
    }

    override fun updateElement(element: UIElement, parameters: MutableMap<Any?, Any?>?) {
        val text = P3cBundle.getMessage("$textKey${SmartfoxActivator.instance.locale}")
        element.setText(text)
        element.setTooltip(text)
    }
}