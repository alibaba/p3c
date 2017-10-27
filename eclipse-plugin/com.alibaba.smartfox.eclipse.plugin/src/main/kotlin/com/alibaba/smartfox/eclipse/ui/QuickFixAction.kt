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

import com.alibaba.p3c.pmd.lang.java.rule.flowcontrol.NeedBraceRule
import com.alibaba.smartfox.eclipse.RunWithoutViewRefresh
import com.alibaba.smartfox.eclipse.SmartfoxActivator
import com.alibaba.smartfox.eclipse.job.CodeAnalysis
import com.alibaba.smartfox.eclipse.job.P3CMutex
import com.alibaba.smartfox.eclipse.pmd.rule.MissingOverrideAnnotationRule
import com.alibaba.smartfox.eclipse.util.CleanUps
import com.alibaba.smartfox.eclipse.util.getResolution
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jface.action.Action

/**
 *
 *
 * @author caikang
 * @date 2017/06/14
 */
class QuickFixAction(val view: InspectionResultView) : Action("Quick Fix") {
    init {
        imageDescriptor = SmartfoxActivator.getImageDescriptor("icons/actions/quickfixBulb.png")
        isEnabled = false
    }

    var markers = listOf<FileMarkers>()

    fun updateFileMarkers(markers: List<FileMarkers>) {
        this.markers = markers
        isEnabled = enabled()
    }

    override fun run() {
        if (markers.isEmpty()) {
            return
        }
        runJob()
    }

    private fun runJob() {
        val job = object : Job("Perform Quick Fix") {
            override fun run(monitor: IProgressMonitor): IStatus {
                val subMonitor = SubMonitor.convert(monitor, markers.size)
                monitor.setTaskName("Process File")
                markers.forEach {
                    if (monitor.isCanceled) {
                        return@run Status.CANCEL_STATUS
                    }
                    monitor.subTask(it.file.name)
                    val childMonitor = subMonitor.newChild(1)
                    if (useCleanUpRefactoring()) {
                        CleanUps.fix(it.file, childMonitor)
                    } else {
                        it.markers.filter { it.marker.exists() }.forEach {
                            (it.marker.getResolution() as RunWithoutViewRefresh).run(it.marker, true)
                        }
                    }
                    val markers = CodeAnalysis.processFileToMakers(it.file, monitor)
                    InspectionResults.updateFileViolations(it.file, markers)
                }

                return Status.OK_STATUS
            }
        }
        job.rule = P3CMutex
        job.schedule()

    }

    fun enabled(): Boolean {
        if (useCleanUpRefactoring()) {
            return true
        }
        if (markers.isEmpty()) {
            return false
        }
        val marker = markers.first().markers.first().marker
        return marker.exists() && marker.getResolution() != null
    }

    private fun useCleanUpRefactoring(): Boolean {
        if (markers.isEmpty()) {
            return false
        }
        val ruleName = markers.first().markers.first().violation.rule.name
        return ruleName == MissingOverrideAnnotationRule::class.java.simpleName
                || ruleName == NeedBraceRule::class.java.simpleName
    }
}