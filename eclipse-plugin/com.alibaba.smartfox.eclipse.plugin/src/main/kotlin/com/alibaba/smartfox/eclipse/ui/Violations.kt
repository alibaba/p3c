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

import com.alibaba.smartfox.eclipse.util.MarkerUtil
import net.sourceforge.pmd.RuleViolation
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IMarker

/**
 *
 *
 * @author caikang
 * @date 2017/06/13
 */
data class LevelViolations(var level: String, var rules: List<RuleViolations>,
        var count: Int = rules.sumBy { it.count }) {
    fun removeMarkers() {
        rules.forEach {
            it.removeMarkers()
        }
    }
}

data class RuleViolations(var rule: String, var files: List<FileMarkers>,
        var count: Int = files.sumBy { it.markers.size }) {
    fun removeMarkers() {
        files.forEach {
            it.removeMarkers()
        }
    }
}

data class FileMarkers(var file: IFile, var markers: List<MarkerViolation>) {
    fun removeMarkers() {
        MarkerUtil.removeAllMarkers(file)
    }
}

data class MarkerViolation(val marker: IMarker, val violation: RuleViolation)