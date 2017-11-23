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

import com.alibaba.smartfox.eclipse.job.CodeAnalysis.processResources
import com.alibaba.smartfox.eclipse.message.P3cBundle
import com.google.common.collect.Sets
import org.apache.log4j.Logger
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IResourceVisitor
import org.eclipse.core.runtime.IAdaptable
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.IWorkingSet
import org.eclipse.ui.commands.IElementUpdater
import org.eclipse.ui.handlers.HandlerUtil
import org.eclipse.ui.menus.UIElement
import org.eclipse.ui.part.EditorPart
import org.eclipse.ui.part.ViewPart

/**
 * @author caikang
 * @date 2016/12/27
 */
open class CodeAnalysisHandler : AbstractHandler(), IElementUpdater {
    override fun updateElement(element: UIElement, parameters: MutableMap<Any?, Any?>?) {
        val text = P3cBundle.getMessage("com.alibaba.smartfox.eclipse.handler.CodeAnalysisHandler")
        element.setText(text)
        element.setTooltip(text)
    }

    @Throws(ExecutionException::class)
    override fun execute(executionEvent: ExecutionEvent): Any? {
        val selection = HandlerUtil.getCurrentSelectionChecked(executionEvent)
        val part = HandlerUtil.getActivePart(executionEvent)
        if (part is ViewPart) {
            if (selection is IStructuredSelection) {
                processForMutiFiles(selection)
            }
        } else if (part is EditorPart) {
            val editorInput = HandlerUtil.getActiveEditorInput(executionEvent)
            if (editorInput is IFileEditorInput) {
                processResources(setOf(editorInput.file))
            }
        }
        return null
    }

    private fun processForMutiFiles(selection: IStructuredSelection) {
        val resources = getSelectionResources(selection)
        processResources(resources)
    }

    private fun getSelectionResources(selection: IStructuredSelection): MutableSet<IResource> {
        val resources = mutableSetOf<IResource>()
        selection.toList().forEach {
            when (it) {
                is IWorkingSet -> it.elements.mapTo(resources) { it.getAdapter(IResource::class.java) as IResource }
                is IAdaptable -> {
                    val file = it.getAdapter(IResource::class.java) as? IResource ?: return@forEach
                    resources.add(file)
                }
                else -> log.warn("The selected object is not adaptable : ${it.toString()}")
            }
        }
        return resources
    }


    companion object {
        private val log = Logger.getLogger(CodeAnalysisHandler::class.java)
    }
}

class FileCollectVisitor : IResourceVisitor {
    val fileSet = Sets.newLinkedHashSet<IFile>()!!

    override fun visit(resource: IResource?): Boolean {
        if (resource == null) {
            return false
        }
        val file = resource.getAdapter(IFile::class.java) as? IFile ?: return true
        if (file.exists() && (file.fileExtension == "java" || file.fileExtension == "vm")) {
            fileSet.add(file)
        }
        return false
    }
}