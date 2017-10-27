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
package com.alibaba.smartfox.idea.common.component

import com.alibaba.smartfox.idea.common.util.PluginVersions
import com.intellij.openapi.components.ProjectComponent

/**
 *
 *
 * @author caikang
 * @date 2017/04/28
 */
interface AliBaseProjectComponent : ProjectComponent {
    override fun getComponentName(): String {
        return "${PluginVersions.pluginId.idString}-${javaClass.name}"
    }

    override fun disposeComponent() {
    }

    override fun projectClosed() {
    }

    override fun initComponent() {
    }

    override fun projectOpened() {
    }
}