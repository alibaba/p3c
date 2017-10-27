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
package com.alibaba.smartfox.idea.common.util

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.cl.PluginClassLoader
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.extensions.PluginId

/**
 * @author caikang
 */
object PluginVersions {
    val baseVersion141 = 141
    val baseVersion143 = 143
    val baseVersion145 = 145
    val baseVersion162 = 162
    val baseVersion163 = 163
    val baseVersion171 = 171

    val pluginId: PluginId = (javaClass.classLoader as PluginClassLoader).pluginId
    val pluginDescriptor: IdeaPluginDescriptor = PluginManager.getPlugin(pluginId)!!

    /**
     * 获取当前安装的 plugin版本
     */
    val pluginVersion: String
        get() = pluginDescriptor.version

    /**
     * 获取当前使用的IDE版本
     */
    val ideVersion: String
        get() {
            val applicationInfo = ApplicationInfo.getInstance()
            return applicationInfo.fullVersion + "_" + applicationInfo.build
        }


    val baseVersion: Int
        get() {
            val applicationInfo = ApplicationInfo.getInstance()
            return applicationInfo.build.baselineVersion
        }
}
