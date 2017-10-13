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
import com.alibaba.smartfox.eclipse.pmd.RulePriority
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.swt.graphics.Image

/**
 *
 *
 * @author caikang
 * @date 2017/06/08
 */
object InspectionResultTreeLabelProvider : LabelProvider() {
    override fun getImage(element: Any?): Image? {
        if (element is LevelViolations) {
            val imageName = when (element.level) {
                RulePriority.Blocker.title, RulePriority.Critical.title -> "${element.level}.gif".toLowerCase()
                else -> "${element.level}.png".toLowerCase()
            }
            return SmartfoxActivator.instance.getImage("icons/view/$imageName")
        }
        if (element is FileMarkers) {
            if (element.file.fullPath.toPortableString().endsWith("java")) {
                return SmartfoxActivator.instance.getImage("icons/view/class_obj.png")
            }
            return SmartfoxActivator.instance.getImage("icons/view/file_obj.png")
        }
        return null
    }

    override fun getText(element: Any?): String {
        if (element is LevelViolations) {
            return "${element.level} (${element.count} Violations)"
        }
        if (element is RuleViolations) {
            val rule = SmartfoxActivator.instance.getRule(element.rule)
            return "${rule.message} (${element.count} Violations)"
        }
        if (element is FileMarkers) {
            return element.file.fullPath.toPortableString().substringAfterLast("/") +
                    " (${element.markers.size} Violations)"
        }
        if (element is MarkerViolation) {
            val desc = element.violation.description
            return "$desc (at line ${element.violation.beginLine})"
        }
        return element.toString()
    }
}