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
package com.alibaba.p3c.idea.action

import com.alibaba.p3c.idea.compatible.inspection.InspectionProfileService
import com.alibaba.p3c.idea.compatible.inspection.Inspections
import com.alibaba.p3c.idea.config.SmartFoxProjectConfig
import com.alibaba.p3c.idea.i18n.P3cBundle
import com.alibaba.p3c.idea.inspection.AliBaseInspection
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import icons.P3cIcons

/**
 *
 * Open or close inspections
 * @author caikang
 * @date 2017/03/14
4
 */
class ToggleProjectInspectionAction : AnAction() {
    val textKey = "com.alibaba.p3c.idea.action.ToggleProjectInspectionAction.text"

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val smartFoxConfig = ServiceManager.getService(project, SmartFoxProjectConfig::class.java)
        val tools = Inspections.aliInspections(project) {
            it.tool is AliBaseInspection
        }
        InspectionProfileService.toggleInspection(project, tools, smartFoxConfig.projectInspectionClosed)
        smartFoxConfig.projectInspectionClosed = !smartFoxConfig.projectInspectionClosed
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val smartFoxConfig = ServiceManager.getService(project, SmartFoxProjectConfig::class.java)
        e.presentation.text = if (smartFoxConfig.projectInspectionClosed) {
            e.presentation.icon = P3cIcons.PROJECT_INSPECTION_ON
            P3cBundle.getMessage("$textKey.open")
        } else {
            e.presentation.icon = P3cIcons.PROJECT_INSPECTION_OFF
            P3cBundle.getMessage("$textKey.close")
        }
    }
}
