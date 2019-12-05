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
package com.alibaba.smartfox.eclipse.job

import com.alibaba.p3c.pmd.lang.java.util.GeneratedCodeUtils
import com.alibaba.smartfox.eclipse.SmartfoxActivator
import com.alibaba.smartfox.eclipse.handler.CodeAnalysisHandler
import com.alibaba.smartfox.eclipse.handler.FileCollectVisitor
import com.alibaba.smartfox.eclipse.ui.InspectionResultView
import com.alibaba.smartfox.eclipse.ui.InspectionResults
import com.alibaba.smartfox.eclipse.ui.MarkerViolation
import com.alibaba.smartfox.eclipse.util.MarkerUtil
import com.google.common.io.Files
import net.sourceforge.pmd.PMDConfiguration
import net.sourceforge.pmd.PMDException
import net.sourceforge.pmd.Report
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.SourceCodeProcessor
import org.apache.log4j.Logger
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import java.io.IOException
import java.io.StringReader
import java.nio.charset.Charset

/**
 *
 *
 * @author caikang
 * @date 2017/06/14
 */
object CodeAnalysis {
    private val log = Logger.getLogger(CodeAnalysisHandler::class.java)

    fun processResources(resources: Set<IResource>) {
        InspectionResultView.activeViews()
        val job = object : Job("P3C Code Analysis") {
            override fun run(monitor: IProgressMonitor): IStatus {
                val fileVisitor = FileCollectVisitor()
                monitor.setTaskName("Collect files")
                resources.forEach {
                    if (monitor.isCanceled) {
                        return@run Status.CANCEL_STATUS
                    }
                    if (it.isAccessible) {
                        it.accept(fileVisitor)
                    }
                }
                if (monitor.isCanceled) {
                    return Status.CANCEL_STATUS
                }
                val subMonitor = SubMonitor.convert(monitor, "Analysis files", fileVisitor.fileSet.size)
                monitor.setTaskName("Analysis files")
                fileVisitor.fileSet.forEach { iFile ->
                    if (monitor.isCanceled) {
                        return@run Status.CANCEL_STATUS
                    }
                    MarkerUtil.removeAllMarkers(iFile)
                    monitor.subTask(iFile.fullPath.toPortableString())
                    val markers = processFileToMakers(iFile, monitor)
                    subMonitor.newChild(1)
                    InspectionResults.updateFileViolations(iFile, markers)
                }
                return Status.OK_STATUS
            }
        }
        job.apply {
            isUser = true
            isSystem = false
            priority = Job.INTERACTIVE
            rule = P3cMutex
            schedule()
        }
    }

    fun processFileToMakers(file: IFile, monitor: IProgressMonitor): List<MarkerViolation> {
        file.refreshLocal(IResource.DEPTH_ZERO, monitor)
        val ruleViolations = processFile(file)

        MarkerUtil.removeAllMarkers(file)
        return ruleViolations.map {
            MarkerViolation(MarkerUtil.addMarker(file, it), it)
        }
    }

    private fun processFile(file: IFile): List<RuleViolation> {
        val configuration = PMDConfiguration()
        configuration.setSourceEncoding(file.charset ?: Charsets.UTF_8.name())
        configuration.inputPaths = file.fullPath.toPortableString()
        val ctx = RuleContext()
        ctx.setAttribute("eclipseFile", file)
        val niceFileName = configuration.inputPaths
        val report = Report.createReport(ctx, niceFileName)
        SmartfoxActivator.instance.ruleSets.start(ctx)
        val processor = SourceCodeProcessor(configuration)
        try {
            ctx.languageVersion = null
            val content = Files.toString(file.rawLocation.toFile(), Charset.forName(file.charset))
            if (!GeneratedCodeUtils.isGenerated(content)) {
                processor.processSourceCode(StringReader(content), SmartfoxActivator.instance.ruleSets, ctx)
            }
        } catch (pmde: PMDException) {
            log.debug("Error while processing file: " + niceFileName, pmde.cause)
            report.addError(Report.ProcessingError(pmde, niceFileName))
        } catch (ioe: IOException) {
            log.error("Unable to read source file: " + niceFileName, ioe)
        } catch (re: RuntimeException) {
            log.error("RuntimeException while processing file: " + niceFileName, re)
        } finally {
            SmartfoxActivator.instance.ruleSets.end(ctx)
        }
        return report.toList()
    }
}
