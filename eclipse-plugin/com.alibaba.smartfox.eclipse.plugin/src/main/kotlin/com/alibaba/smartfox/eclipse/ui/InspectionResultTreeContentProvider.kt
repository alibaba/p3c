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

import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.Viewer

/**
 *
 *
 * @author caikang
 * @date 2017/06/08
 */
object InspectionResultTreeContentProvider : ITreeContentProvider {
    private lateinit var input: InspectionResults

    override fun getParent(element: Any?): Any {
        return input
    }

    override fun hasChildren(element: Any?): Boolean {
        return element is InspectionResults || element is LevelViolations || element is RuleViolations
                || element is FileMarkers
    }

    override fun getChildren(parentElement: Any?): Array<Any> {
        if (parentElement is InspectionResults) {
            return parentElement.errors.toTypedArray()
        }
        if (parentElement is LevelViolations) {
            return parentElement.rules.toTypedArray()
        }
        if (parentElement is RuleViolations) {
            return parentElement.files.toTypedArray()
        }
        if (parentElement is FileMarkers) {
            return parentElement.markers.toTypedArray()
        }
        return emptyArray()
    }

    override fun getElements(inputElement: Any?): Array<Any> {
        return input.errors.toTypedArray()
    }

    override fun inputChanged(viewer: Viewer?, oldInput: Any?, newInput: Any?) {
        if (newInput == null) {
            return
        }
        this.input = newInput as InspectionResults
    }

    override fun dispose() {
    }
}