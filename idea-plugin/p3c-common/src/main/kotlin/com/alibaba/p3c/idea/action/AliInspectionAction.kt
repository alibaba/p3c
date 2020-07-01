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
import com.alibaba.p3c.idea.i18n.P3cBundle
import com.alibaba.p3c.idea.inspection.AliBaseInspection
import com.alibaba.p3c.idea.util.NumberConstants
import com.google.common.collect.Lists
import com.intellij.analysis.AnalysisScope
import com.intellij.analysis.AnalysisUIOptions
import com.intellij.analysis.BaseAnalysisActionDialog
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.InspectionsBundle
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.codeInspection.ex.InspectionManagerEx
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import java.awt.event.KeyEvent

/**
 * @author caikang
 * @date 2016/12/11
 */
class AliInspectionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val analysisUIOptions = ServiceManager.getService(project, AnalysisUIOptions::class.java)!!
        analysisUIOptions.GROUP_BY_SEVERITY = true

        val managerEx = InspectionManager.getInstance(project) as InspectionManagerEx
        val toolWrappers = Inspections.aliInspections(project) {
            it.tool is AliBaseInspection
        }
        val psiElement = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        val psiFile = e.getData<PsiFile>(CommonDataKeys.PSI_FILE)
        val virtualFile = e.getData<VirtualFile>(CommonDataKeys.VIRTUAL_FILE)
        val virtualFiles = e.getData<Array<VirtualFile>>(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        var analysisScope: AnalysisScope? = null
        var projectDir = false
        if (psiFile != null) {
            analysisScope = AnalysisScope(psiFile)
            projectDir = isBaseDir(psiFile.virtualFile, project)
        } else if (virtualFiles != null && virtualFiles.size > NumberConstants.INTEGER_SIZE_OR_LENGTH_0) {
            analysisScope = AnalysisScope(project, Lists.newArrayList<VirtualFile>(*virtualFiles))
            projectDir = virtualFiles.any {
                isBaseDir(it, project)
            }
        } else {
            if (virtualFile != null && virtualFile.isDirectory) {
                val psiDirectory = PsiManager.getInstance(project).findDirectory(virtualFile)
                if (psiDirectory != null) {
                    analysisScope = AnalysisScope(psiDirectory)
                    projectDir = isBaseDir(virtualFile, project)
                }
            }
            if (analysisScope == null && virtualFile != null) {
                analysisScope = AnalysisScope(project, listOf(virtualFile))
                projectDir = isBaseDir(virtualFile, project)
            }
            if (analysisScope == null) {
                projectDir = true
                analysisScope = AnalysisScope(project)
            }
        }
        if (e.inputEvent is KeyEvent) {
            inspectForKeyEvent(project, managerEx, toolWrappers, psiElement, psiFile, virtualFile, analysisScope)
            return
        }
        val element = psiFile ?: psiElement
        analysisScope.isIncludeTestSource = false
        analysisScope.setSearchInLibraries(true)
        createContext(
            toolWrappers, managerEx, element,
            projectDir, analysisScope
        ).doInspections(analysisScope)
    }

    private fun isBaseDir(file: VirtualFile, project: Project): Boolean {
        if (file.canonicalPath == null || project.basePath == null) {
            return false
        }
        return project.basePath == file.canonicalPath
    }

    private fun inspectForKeyEvent(
        project: Project, managerEx: InspectionManagerEx,
        toolWrappers: List<InspectionToolWrapper<*, *>>, psiElement: PsiElement?, psiFile: PsiFile?,
        virtualFile: VirtualFile?, analysisScope: AnalysisScope
    ) {
        var module: Module? = null
        if (virtualFile != null && project.baseDir != virtualFile) {
            module = ModuleUtilCore.findModuleForFile(virtualFile, project)
        }

        val uiOptions = AnalysisUIOptions.getInstance(project)
        uiOptions.ANALYZE_TEST_SOURCES = false
        val dialog = BaseAnalysisActionDialog(
            "Select Analyze Scope", "Analyze Scope", project, analysisScope,
            module?.name, true, uiOptions, psiElement
        )

        if (!dialog.showAndGet()) {
            return
        }
        val scope = dialog.getScope(uiOptions, analysisScope, project, module)
        scope.setSearchInLibraries(true)
        val element = psiFile ?: psiElement
        createContext(
            toolWrappers, managerEx, element,
            dialog.isProjectScopeSelected, scope
        ).doInspections(scope)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.text = P3cBundle.getMessage("com.alibaba.p3c.idea.action.AliInspectionAction.text")
    }

    companion object {
        private val logger = Logger.getInstance(AliInspectionAction::class.java)

        private fun getTitle(element: PsiElement?, isProjectScopeSelected: Boolean): String? {
            if (element == null) {
                return null
            }
            if (isProjectScopeSelected) {
                return "Project"
            }
            if (element is PsiFileSystemItem) {
                return VfsUtilCore.getRelativePath(element.virtualFile, element.project.baseDir)
            }
            return null
        }

        fun createContext(
            toolWrapperList: List<InspectionToolWrapper<*, *>>,
            managerEx: InspectionManagerEx, psiElement: PsiElement?,
            projectScopeSelected: Boolean, scope: AnalysisScope
        ):
            GlobalInspectionContextImpl {
            // remove last same scope content
            val project = managerEx.project
            val title = getTitle(psiElement, projectScopeSelected)
            val model = InspectionProfileService.createSimpleProfile(toolWrapperList, managerEx, psiElement)
            title?.let {
                model.name = it
            }

            val inspectionContext = createNewGlobalContext(
                managerEx, projectScopeSelected
            )
            InspectionProfileService.setExternalProfile(model, inspectionContext)

            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.INSPECTION)

            if (toolWindow != null) {
                val contentManager = toolWindow.contentManager
                val contentTitle = title?.let {
                    InspectionsBundle.message("inspection.results.for.profile.toolwindow.title", it, scope.shortenName)
                }
                val content = contentManager.contents.firstOrNull {
                    contentTitle != null && (it.tabName == contentTitle || it.tabName.endsWith(contentTitle))
                }
                content?.let {
                    contentManager.removeContent(content, true)
                }
            }

            return inspectionContext
        }

        private fun createNewGlobalContext(
            managerEx: InspectionManagerEx,
            projectScopeSelected: Boolean
        ): GlobalInspectionContextImpl {
            return PmdGlobalInspectionContextImpl(managerEx.project, managerEx.contentManager, projectScopeSelected)
        }
    }
}
