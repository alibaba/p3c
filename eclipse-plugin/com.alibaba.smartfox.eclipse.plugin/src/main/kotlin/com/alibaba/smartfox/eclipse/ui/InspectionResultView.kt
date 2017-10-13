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
import com.alibaba.smartfox.eclipse.job.P3CMutex
import com.alibaba.smartfox.eclipse.util.MarkerUtil
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IMarker
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jface.action.Action
import org.eclipse.jface.action.Separator
import org.eclipse.jface.util.OpenStrategy
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.ITreeSelection
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.OpenAndLinkWithEditorHelper
import org.eclipse.ui.PartInitException
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.ide.IDE
import org.eclipse.ui.ide.ResourceUtil
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities
import org.eclipse.ui.part.ViewPart
import org.eclipse.ui.texteditor.ITextEditor
import java.util.HashSet

/**
 *
 *
 * @author caikang
 * @date 2017/06/08
 */
class InspectionResultView : ViewPart() {
    lateinit var treeViewer: TreeViewer

    private val quickFixAction = QuickFixAction(this)

    override fun setFocus() {
        treeViewer.control.setFocus()
    }

    override fun createPartControl(parent: Composite) {
        parent.layout = FillLayout()
        treeViewer = TreeViewer(parent, SWT.MULTI or SWT.H_SCROLL or SWT.V_SCROLL)
        treeViewer.setUseHashlookup(true)
        treeViewer.contentProvider = InspectionResultTreeContentProvider
        treeViewer.labelProvider = InspectionResultTreeLabelProvider
        treeViewer.input = InspectionResults

        InspectionResults.view = this

        addDoubleClickListener()

        addSelectionChangedListener()

        initToolBar()

        addLinkWithEditorSupport()
        site.selectionProvider = treeViewer
    }

    fun clear() {
        InspectionResults.clear()
        refreshView(InspectionResults)
    }

    fun refreshView(input: InspectionResults) {
        Display.getDefault().asyncExec {
            treeViewer.refresh(input, true)
            contentDescription = input.contentDescription
        }
    }

    private fun addSelectionChangedListener() {
        treeViewer.addSelectionChangedListener inner@ {
            val selection = it.selection as? IStructuredSelection ?: return@inner
            val item = selection.firstElement ?: return@inner
            val ruleDetailView = RuleDetailView.showAndGetView()
            when (item) {
                is MarkerViolation -> {
                    ruleDetailView.refresh(item.violation.rule)
                    quickFixAction.updateFileMarkers(listOf(FileMarkers(item.marker.resource as IFile, listOf(item))))
                }
                is FileMarkers -> {
                    ruleDetailView.refresh(item.markers.first().violation.rule)
                    quickFixAction.updateFileMarkers(listOf(item))
                }
                is RuleViolations -> {
                    ruleDetailView.refresh(SmartfoxActivator.instance.getRule(item.rule))
                    quickFixAction.updateFileMarkers(item.files)
                }
                else -> {
                    quickFixAction.updateFileMarkers(emptyList())
                }
            }
        }
    }

    private fun initToolBar() {
        val bars = viewSite.actionBars
        val tm = bars.toolBarManager

        val clearAction = object : Action("Clear Markers") {
            override fun run() {
                val job = object : Job("Clear Markers") {
                    override fun run(monitor: IProgressMonitor): IStatus {
                        if (monitor.isCanceled) {
                            Status.CANCEL_STATUS
                        }
                        clear()
                        return Status.OK_STATUS
                    }
                }
                job.rule = P3CMutex
                job.schedule()
            }
        }

        clearAction.imageDescriptor = SmartfoxActivator.getImageDescriptor("icons/actions/clear.png")

        tm.add(Separator("Markers"))
        tm.add(clearAction)

        tm.add(Separator("FilterGroup"))
        tm.add(quickFixAction)
    }

    private fun addLinkWithEditorSupport() {
        object : OpenAndLinkWithEditorHelper(treeViewer) {
            override fun activate(selection: ISelection) {
                val currentMode = OpenStrategy.getOpenMethod()
                try {
                    OpenStrategy.setOpenMethod(OpenStrategy.DOUBLE_CLICK)
                    openSelectedMarkers()
                } finally {
                    OpenStrategy.setOpenMethod(currentMode)
                }
            }

            override fun linkToEditor(selection: ISelection?) {
            }

            override fun open(selection: ISelection, activate: Boolean) {
                val structured = selection as ITreeSelection
                val element = structured.firstElement as? MarkerViolation ?: return
                val page = site.page
                if (element.marker.exists()) {
                    openMarkerInEditor(element.marker, page)
                    return
                }
                val file = element.marker.resource as IFile
                val editor = IDE.openEditor(page, file) as ITextEditor
                editor.selectAndReveal(MarkerUtil.getAbsoluteRange(file, element.violation).start, 0)
            }
        }
    }

    internal fun openSelectedMarkers() {
        val markers = getOpenableMarkers()
        for (marker in markers) {
            val page = site.page
            openMarkerInEditor(marker, page)
        }
    }

    private fun getOpenableMarkers(): Array<IMarker> {
        val structured = treeViewer.selection as ITreeSelection
        val elements = structured.iterator()
        val result = HashSet<IMarker>()

        while (elements.hasNext()) {
            val marker = elements.next() as? IMarker ?: return emptyArray()
            result.add(marker)
        }
        return result.toTypedArray()
    }


    fun openMarkerInEditor(marker: IMarker?, page: IWorkbenchPage) {
        val editor = page.activeEditor
        if (editor != null) {
            val input = editor.editorInput
            val file = ResourceUtil.getFile(input)
            if (file != null) {
                if (marker!!.resource == file && OpenStrategy.activateOnOpen()) {
                    page.activate(editor)
                }
            }
        }

        if (marker != null && marker.resource is IFile) {
            try {
                IDE.openEditor(page, marker, OpenStrategy.activateOnOpen())
            } catch (e: PartInitException) {
                MarkerSupportInternalUtilities.showViewError(e)
            }

        }
    }

    private fun addDoubleClickListener() {
        treeViewer.addDoubleClickListener({ event ->
            val selection = event.selection
            if (selection !is ITreeSelection || selection.size() != 1) {
                return@addDoubleClickListener
            }
            val obj = selection.firstElement
            if (treeViewer.isExpandable(obj)) {
                treeViewer.setExpandedState(obj, !treeViewer.getExpandedState(obj))
            }
        })
    }

    companion object {
        val viewId = "com.alibaba.smartfox.eclipse.ui.InspectionResultView"

        fun activeViews() {
            PlatformUI.getWorkbench().activeWorkbenchWindow.activePage.showView(viewId)
            RuleDetailView.showAndGetView()
        }

        fun getView(): InspectionResultView {
            return PlatformUI.getWorkbench().activeWorkbenchWindow.activePage.findView(viewId) as InspectionResultView
        }
    }
}

