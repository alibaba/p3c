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
package com.alibaba.p3c.idea.ep

import com.alibaba.smartfox.idea.common.util.PluginVersions
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.codeInspection.ui.InspectionResultsView
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

/**
 *
 *
 * @author caikang
 * @date 2017/06/19
 */
interface InspectionActionExtensionPoint {
    fun doOnInspectionFinished(context: GlobalInspectionContextImpl, projectScopeSelected: Boolean) {}
    fun doOnClose(noSuspiciousCodeFound: Boolean, project: Project?) {}
    fun doOnView(view: InspectionResultsView) {}

    companion object {
        val extension = ExtensionPointName.create<InspectionActionExtensionPoint>(
                "${PluginVersions.pluginId.idString}.inspectionAction")!!
    }
}