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
package com.alibaba.p3c.idea.component

import com.alibaba.p3c.idea.compatible.inspection.Inspections
import com.alibaba.p3c.idea.config.P3cConfig
import com.alibaba.p3c.idea.inspection.AliPmdInspectionInvoker
import com.alibaba.p3c.idea.pmd.SourceCodeProcessor
import com.alibaba.smartfox.idea.common.component.AliBaseProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileAdapter
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager

/**
 * @author caikang
 * @date 2016/12/13
 */
class AliProjectComponent(
        private val project: Project,
        val p3cConfig: P3cConfig
) : AliBaseProjectComponent {
    private val listener: VirtualFileListener
    private val javaExtension = ".java"
    private val velocityExtension = ".vm"

    init {
        listener = object : VirtualFileAdapter() {
            override fun contentsChanged(event: VirtualFileEvent) {
                val path = event.file.canonicalPath
                if (path == null || !(path.endsWith(javaExtension) || path.endsWith(velocityExtension))) {
                    return
                }
                PsiManager.getInstance(project).findFile(event.file) ?: return
                if (!p3cConfig.ruleCacheEnable) {
                    AliPmdInspectionInvoker.refreshFileViolationsCache(event.file)
                }
                if (!p3cConfig.astCacheEnable) {
                    SourceCodeProcessor.invalidateCache(path)
                }

            }
        }
    }

    override fun projectOpened() {
        Inspections.addCustomTag(project, "date")
        VirtualFileManager.getInstance().addVirtualFileListener(listener)
    }

    override fun projectClosed() {
        VirtualFileManager.getInstance().removeVirtualFileListener(listener)
    }
}
