// =====================================================================
//
// Copyright (C) 2012 - 2016, Philip Graf
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// =====================================================================
package com.alibaba.smartfox.eclipse.util

import com.alibaba.smartfox.eclipse.QuickFixGenerator
import com.alibaba.smartfox.eclipse.SmartfoxActivator
import com.google.common.io.Files
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RulePriority
import net.sourceforge.pmd.RuleViolation
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.CoreException
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.Document
import org.eclipse.ui.IMarkerResolution
import java.nio.charset.Charset

/**
 * @author caikang
 * @date 2017/06/08
 */
object MarkerUtil {

    private val PMD_TAB_SIZE = 8

    private val MARKER_TYPE = "${SmartfoxActivator.PLUGIN_ID}.p3cMarker"

    @Throws(CoreException::class)
    fun removeAllMarkers(file: IFile) {
        try {
            if (file.exists()) {
                file.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_ZERO)
            }
        } catch (e: Exception) {
            SmartfoxActivator.instance.logError(e.message ?: "", e)
        }
    }

    @Throws(CoreException::class)
    fun removeAllMarkers(project: IProject) {
        project.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE)
    }

    @Throws(CoreException::class)
    fun addMarker(file: IFile, violation: RuleViolation): IMarker {

        val marker = file.createMarker(MARKER_TYPE)
        marker.setAttribute(IMarker.MESSAGE, violation.description)
        val severity = when (violation.rule.priority) {
            RulePriority.HIGH -> IMarker.SEVERITY_ERROR
            RulePriority.MEDIUM_HIGH -> IMarker.SEVERITY_WARNING
            else -> IMarker.SEVERITY_INFO
        }
        marker.setRule(violation.rule.name)
        marker.setAttribute(IMarker.SEVERITY, severity)
        marker.setAttribute(IMarker.LINE_NUMBER, Math.max(violation.beginLine, 0))
        val range = getAbsoluteRange(file, violation)
        val start = Math.max(range.start, 0)
        marker.setAttribute(IMarker.CHAR_START, start)
        val end = Math.max(range.end, 0)
        marker.setAttribute(IMarker.CHAR_END, end)
        return marker
    }


    fun getAbsoluteRange(file: IFile, violation: RuleViolation): Range {
        val content = Files.toString(file.rawLocation.toFile(), Charset.forName(file.charset))
        try {
            return calculateAbsoluteRange(content, violation)
        } catch (e: BadLocationException) {
            return Range(0, 0)
        }
    }

    @Throws(BadLocationException::class) private fun calculateAbsoluteRange(content: String,
            violation: RuleViolation): Range {
        val document = Document(content)

        // violation line and column start at one, the marker's start and end positions at zero
        val start = getAbsolutePosition(content, document.getLineOffset(violation.beginLine - 1), violation.beginColumn)
        val end = getAbsolutePosition(content, document.getLineOffset(violation.endLine - 1), violation.endColumn)

        // for some rules PMD creates violations with the end position before the start position
        val range = if (start <= end) {
            Range(start - 1, end)
        } else {
            Range(end - 1, start)
        }

        return range
    }

    private fun getAbsolutePosition(content: String, lineOffset: Int, pmdCharOffset: Int): Int {
        var pmdCharCounter = 0
        var absoluteOffset = lineOffset
        while (pmdCharCounter < pmdCharOffset) {
            if (absoluteOffset < content.length) {
                val c = content[absoluteOffset]
                if (c == '\t') {
                    pmdCharCounter = (pmdCharCounter / PMD_TAB_SIZE + 1) * PMD_TAB_SIZE
                } else {
                    pmdCharCounter++
                }
            } else {
                break
            }
            absoluteOffset++
        }
        return absoluteOffset
    }
}

fun IMarker.setRule(rule: String) {
    this.setAttribute("rule", rule)
}

fun IMarker.getRule(): Rule {
    return SmartfoxActivator.instance.getRule(this.getAttribute("rule") as String)
}

fun IMarker.getResolution(): IMarkerResolution? {
    return QuickFixGenerator.quickFixes[getRule().name]
}

data class Range(val start: Int, val end: Int)
