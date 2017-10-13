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
package com.alibaba.smartfox.eclipse

import com.alibaba.p3c.pmd.lang.java.rule.constant.UpperEllRule
import com.alibaba.p3c.pmd.lang.java.rule.oop.EqualsAvoidNullRule
import com.alibaba.smartfox.eclipse.ui.InspectionResults
import com.alibaba.smartfox.eclipse.util.getRule
import com.google.common.io.Files
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IMarker
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.ltk.core.refactoring.TextFileChange
import org.eclipse.text.edits.ReplaceEdit
import org.eclipse.ui.IMarkerResolution
import org.eclipse.ui.IMarkerResolutionGenerator
import java.nio.charset.Charset

/**
 *
 *
 * @author caikang
 * @date 2017/06/14
 */
class QuickFixGenerator : IMarkerResolutionGenerator {
    override fun getResolutions(marker: IMarker): Array<IMarkerResolution> {
        if (!marker.exists()) {
            return emptyArray()
        }
        val rule = marker.getRule()
        val quickFix = quickFixes[rule.name] ?: return emptyArray()
        return arrayOf(quickFix)
    }

    companion object {
        val quickFixes = mapOf(UpperEllRule::class.java.simpleName to UpperEllQuickFix,
                EqualsAvoidNullRule::class.java.simpleName to EqualsAvoidNullQuickFix)
    }
}

interface RunWithoutViewRefresh : IMarkerResolution {
    fun run(marker: IMarker, refresh: Boolean)

    override fun run(marker: IMarker) {
        run(marker, true)
    }
}

abstract class BaseQuickFix : RunWithoutViewRefresh {
    override fun run(marker: IMarker, refresh: Boolean) {
        if (!marker.exists()) {
            return
        }
        val file = marker.resource as IFile
        doRun(marker, file)
        marker.delete()
        if (refresh) {
            InspectionResults.removeMarker(marker)
        }
    }

    abstract fun doRun(marker: IMarker, file: IFile)
}

object UpperEllQuickFix : BaseQuickFix() {
    override fun doRun(marker: IMarker, file: IFile) {
        val offset = marker.getAttribute(IMarker.CHAR_START, 0)
        val end = marker.getAttribute(IMarker.CHAR_END, 0)
        val content = Files.toString(file.rawLocation.toFile(), Charset.forName(file.charset))
        val replaceString = content.substring(offset, end + 1).replace("l", "L")
        val edit = ReplaceEdit(offset, replaceString.length, replaceString)
        val change = TextFileChange("", file)
        change.edit = edit
        change.perform(NullProgressMonitor())
    }

    override fun getLabel(): String {
        return "Replace 'l' to 'L'."
    }

}

object EqualsAvoidNullQuickFix : BaseQuickFix() {
    val equalsName = ".equals("

    override fun doRun(marker: IMarker, file: IFile) {
        val offset = marker.getAttribute(IMarker.CHAR_START, 0)
        val end = marker.getAttribute(IMarker.CHAR_END, 0)
        val content = Files.toString(file.rawLocation.toFile(), Charset.forName(file.charset))
        val string = content.substring(offset, end)
        val list = string.split(equalsName).filterNotNull()
        if (list.size != 2) {
            return
        }
        val replace = "${list[1].substringBeforeLast(')')}$equalsName${list[0]})"
        val edit = ReplaceEdit(offset, string.length, replace)
        val change = TextFileChange("", file)
        change.edit = edit
        change.perform(NullProgressMonitor())
    }

    override fun getLabel(): String {
        return "Flip equals."
    }

}