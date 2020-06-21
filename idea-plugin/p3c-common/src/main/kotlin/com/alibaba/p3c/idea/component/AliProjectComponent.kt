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
import com.alibaba.p3c.idea.i18n.P3cBundle
import com.alibaba.p3c.idea.inspection.AliPmdInspectionInvoker
import com.alibaba.p3c.idea.pmd.SourceCodeProcessor
import com.alibaba.p3c.idea.util.withLockNotInline
import com.alibaba.p3c.pmd.I18nResources
import com.alibaba.smartfox.idea.common.component.AliBaseProjectComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileAdapter
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileMoveEvent
import com.intellij.psi.PsiManager
import net.sourceforge.pmd.RuleViolation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

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

    private val lock = ReentrantReadWriteLock()
    private val readLock = lock.readLock()
    private val writeLock = lock.writeLock()

    private val fileContexts = ConcurrentHashMap<String, FileContext>()

    init {
        listener = object : VirtualFileAdapter() {
            override fun contentsChanged(event: VirtualFileEvent) {
                val path = getFilePath(event) ?: return
                PsiManager.getInstance(project).findFile(event.file) ?: return
                if (!p3cConfig.ruleCacheEnable) {
                    AliPmdInspectionInvoker.refreshFileViolationsCache(event.file)
                }
                if (!p3cConfig.astCacheEnable) {
                    SourceCodeProcessor.invalidateCache(path)
                }
                SourceCodeProcessor.invalidUserTrigger(path)
                fileContexts[path]?.ruleViolations = null
            }

            override fun fileDeleted(event: VirtualFileEvent) {
                val path = getFilePath(event)
                path?.let {
                    SourceCodeProcessor.invalidateCache(it)
                    removeFileContext(it)
                }
                super.fileDeleted(event)
            }

            override fun fileMoved(event: VirtualFileMoveEvent) {
                val path = getFilePath(event)
                path?.let {
                    SourceCodeProcessor.invalidateCache(it)
                    removeFileContext(it)
                }
                super.fileMoved(event)
            }

            private fun getFilePath(event: VirtualFileEvent): String? {
                val path = event.file.canonicalPath
                if (path == null || !(path.endsWith(javaExtension) || path.endsWith(velocityExtension))) {
                    return null
                }
                return path
            }
        }
    }

    override fun initComponent() {
        I18nResources.changeLanguage(p3cConfig.locale)
        val analyticsGroup = ActionManager.getInstance().getAction(analyticsGroupId)
        analyticsGroup.templatePresentation.text = P3cBundle.getMessage(analyticsGroupText)
    }

    override fun projectOpened() {
        Inspections.addCustomTag(project, "date")
        VirtualFileManager.getInstance().addVirtualFileListener(listener)
    }

    override fun projectClosed() {
        VirtualFileManager.getInstance().removeVirtualFileListener(listener)
    }

    companion object {
        val analyticsGroupId = "com.alibaba.p3c.analytics.action_group"
        val analyticsGroupText = "$analyticsGroupId.text"
    }

    data class FileContext(
        val lock: ReentrantReadWriteLock,
        var ruleViolations: Map<String, List<RuleViolation>>? = null
    )

    fun removeFileContext(path: String) {
        fileContexts.remove(path)
    }

    fun getFileContext(virtualFile: VirtualFile?): FileContext? {
        val file = virtualFile?.canonicalPath ?: return null
        val result = readLock.withLockNotInline {
            fileContexts[file]
        }
        if (result != null) {
            return result
        }
        return writeLock.withLockNotInline {
            val finalContext = fileContexts[file]
            if (finalContext != null) {
                return@withLockNotInline finalContext
            }
            val lock = ReentrantReadWriteLock()
            FileContext(
                lock = lock
            ).also {
                fileContexts[file] = it
            }
        }
    }
}
