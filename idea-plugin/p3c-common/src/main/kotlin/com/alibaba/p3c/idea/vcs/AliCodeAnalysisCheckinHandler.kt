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
package com.alibaba.p3c.idea.vcs

import com.alibaba.p3c.idea.action.AliInspectionAction
import com.alibaba.p3c.idea.compatible.inspection.Inspections
import com.alibaba.p3c.idea.config.P3cConfig
import com.alibaba.p3c.idea.inspection.AliBaseInspection
import com.alibaba.smartfox.idea.common.util.BalloonNotifications
import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.InspectionManagerEx
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.VcsBundle
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerUtil
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.ui.NonFocusableCheckBox
import com.intellij.util.ExceptionUtil
import com.intellij.util.PairConsumer
import java.awt.BorderLayout
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JComponent
import javax.swing.JPanel

/**
 *
 * @author yaohui.wyh
 * @date 2017/03/21
 * @author caikang
 * @date 2017/05/04
 */
class AliCodeAnalysisCheckinHandler(
    private val myProject: Project,
    private val myCheckinPanel: CheckinProjectPanel
) : CheckinHandler() {
    private val dialogTitle = "Alibaba Code Analyze"
    private val cancelText = "&Cancel"
    private val commitText = "&Commit Anyway"
    private val waitingText = "Wait"

    val log = Logger.getInstance(javaClass)

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent? {
        val checkBox = NonFocusableCheckBox("Alibaba Code Guidelines")
        return object : RefreshableOnComponent {
            override fun getComponent(): JComponent {
                val panel = JPanel(BorderLayout())
                panel.add(checkBox)
                val dumb = DumbService.isDumb(myProject)
                checkBox.isEnabled = !dumb
                checkBox.toolTipText = if (dumb) {
                    "Code analysis is impossible until indices are up-to-date"
                } else {
                    ""
                }
                return panel
            }

            override fun refresh() {}

            override fun saveState() {
                getSettings().analysisBeforeCheckin = checkBox.isSelected
            }

            override fun restoreState() {
                checkBox.isSelected = getSettings().analysisBeforeCheckin
            }
        }
    }

    private fun getSettings(): P3cConfig {
        return ServiceManager.getService(P3cConfig::class.java)
    }

    override fun beforeCheckin(
        executor: CommitExecutor?,
        additionalDataConsumer: PairConsumer<Any, Any>
    ): CheckinHandler.ReturnResult {
        if (!getSettings().analysisBeforeCheckin) {
            return CheckinHandler.ReturnResult.COMMIT
        }
        if (DumbService.getInstance(myProject).isDumb) {
            if (Messages.showOkCancelDialog(
                    myProject,
                    "Code analysis is impossible until indices are up-to-date", dialogTitle,
                    waitingText, commitText, null
                ) == Messages.OK
            ) {
                return CheckinHandler.ReturnResult.CANCEL
            }
            return CheckinHandler.ReturnResult.COMMIT
        }

        val virtualFiles = CheckinHandlerUtil.filterOutGeneratedAndExcludedFiles(myCheckinPanel.virtualFiles, myProject)
        val hasViolation = hasViolation(virtualFiles, myProject)
        if (!hasViolation) {
            BalloonNotifications.showSuccessNotification(
                "No suspicious code found！",
                myProject, "Analyze Finished"
            )
            return CheckinHandler.ReturnResult.COMMIT
        }
        if (Messages.showOkCancelDialog(
                myProject, "Found suspicious code,continue commit？",
                dialogTitle, commitText, cancelText, null
            ) == Messages.OK
        ) {
            return CheckinHandler.ReturnResult.COMMIT
        } else {
            doAnalysis(myProject, virtualFiles.toTypedArray())
            return CheckinHandler.ReturnResult.CLOSE_WINDOW
        }
    }

    fun doAnalysis(project: Project, virtualFiles: Array<VirtualFile>) {
        val managerEx = InspectionManager.getInstance(project) as InspectionManagerEx
        val analysisScope = AnalysisScope(
            project,
            ArrayList(Arrays.asList(*virtualFiles))
        )
        val tools = Inspections.aliInspections(project) { it.tool is AliBaseInspection }
        AliInspectionAction.createContext(tools, managerEx, null, false, analysisScope)
            .doInspections(analysisScope)
    }

    private fun hasViolation(virtualFiles: List<VirtualFile>, project: Project): Boolean {
        ApplicationManager.getApplication().assertIsDispatchThread()
        PsiDocumentManager.getInstance(myProject).commitAllDocuments()
        if (ApplicationManager.getApplication().isWriteAccessAllowed) throw RuntimeException(
            "Must not run under write action"
        )
        val result = AtomicBoolean(false)
        val exception = Ref.create<Exception>()
        ProgressManager.getInstance().run(
            object : Task.Modal(myProject, VcsBundle.message("checking.code.smells.progress.title"), true) {
                override fun run(progress: ProgressIndicator) {
                    try {
                        val tools = Inspections.aliInspections(project) { it.tool is AliBaseInspection }
                        val inspectionManager = InspectionManager.getInstance(project)
                        val psiManager = PsiManager.getInstance(project)
                        val count = AtomicInteger(0)
                        val hasViolation = virtualFiles.asSequence().any { file ->
                            ApplicationManager.getApplication().runReadAction(Computable {
                                val psiFile = psiManager.findFile(file) ?: return@Computable false
                                val curCount = count.incrementAndGet()
                                progress.text = file.canonicalPath
                                progress.fraction = curCount.toDouble() / virtualFiles.size.toDouble()
                                return@Computable tools.any {
                                    progress.checkCanceled()
                                    val tool = it.tool as LocalInspectionTool
                                    val aliTool = tool as AliBaseInspection
                                    progress.text2 = aliTool.ruleName()
                                    val problems = tool.processFile(psiFile, inspectionManager)
                                    problems.size > 0
                                }
                            })

                        }
                        result.set(hasViolation)
                    } catch (e: ProcessCanceledException) {
                        result.set(false)
                    } catch (e: Exception) {
                        log.error(e)
                        exception.set(e)
                    }
                }
            })
        if (!exception.isNull) {
            val t = exception.get()
            ExceptionUtil.rethrowAllAsUnchecked(t)
        }

        return result.get()
    }
}
