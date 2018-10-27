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

import com.alibaba.smartfox.eclipse.pmd.RulePriority
import com.alibaba.smartfox.eclipse.util.MarkerUtil
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IMarker

/**
 *
 *
 * @author caikang
 * @date 2017/06/13
 */
object InspectionResults {
    private val fileViolations = linkedMapOf<IFile, List<MarkerViolation>>()

    var contentDescription = ""

    val errors: List<LevelViolations>
        get() {
            val result = toLevelViolationList(fileViolations.values.flatten())
            contentDescription = getContentDescription(result)
            return result
        }

    lateinit var view: InspectionResultView


    fun clear() {
        fileViolations.forEach {
            MarkerUtil.removeAllMarkers(it.key)
        }
        fileViolations.clear()
        // update contentDescription
        errors
    }

    private fun toLevelViolationList(markers: Collection<MarkerViolation>): List<LevelViolations> {
        return markers.groupBy {
            it.violation.rule.priority.priority
        }.mapValues {
            it.value.groupBy {
                it.violation.rule.name
            }.mapValues {
                it.value.groupBy {
                    it.marker.resource as IFile
                }.map {
                    FileMarkers(it.key, it.value)
                }
            }.map {
                RuleViolations(it.key, it.value)
            }
        }.toSortedMap().map {
            val level = RulePriority.valueOf(it.key).title
            LevelViolations(level, it.value)
        }
    }

    fun updateFileViolations(file: IFile, markers: List<MarkerViolation>) {
        if (markers.isEmpty()) {
            fileViolations.remove(file)
        } else {
            fileViolations[file] = markers
        }
        view.refreshView(this)
    }

    fun removeMarker(marker: IMarker) {
        val file = marker.resource as IFile
        val list = fileViolations[file] ?: return
        val result = list.filter {
            it.marker != marker
        }
        fileViolations[file] = result
        marker.delete()
        view.refreshView(this)
    }

    fun getContentDescription(errors: List<LevelViolations>): String {
        val map = errors.associateBy {
            it.level
        }

        return "${map[RulePriority.Blocker.title]?.count ?: 0} Blockers," +
                "${map[RulePriority.Critical.title]?.count ?: 0} Criticals," +
                "${map[RulePriority.Major.title]?.count ?: 0} Majors"
    }
}
