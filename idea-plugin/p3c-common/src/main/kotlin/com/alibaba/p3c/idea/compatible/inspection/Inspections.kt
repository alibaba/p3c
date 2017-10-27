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
package com.alibaba.p3c.idea.compatible.inspection

import com.google.common.base.Splitter
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.ScopeToolState
import com.intellij.codeInspection.javaDoc.JavaDocLocalInspection
import com.intellij.openapi.project.Project

/**
 *
 *
 * @author caikang
 * @date 2017/03/01
 */
object Inspections {
    fun aliInspections(project: Project, filter: (InspectionToolWrapper<*, *>) -> Boolean): List<InspectionToolWrapper<*, *>> {
        val profile = InspectionProfileService.getProjectInspectionProfile(project)
        return getAllTools(project, profile).filter(filter)
    }

    fun addCustomTag(project: Project, tag: String) {
        val profile = InspectionProfileService.getProjectInspectionProfile(project)
        val javaDocLocalInspection = profile.getInspectionTool("JavaDoc", project)?.tool
                as? JavaDocLocalInspection ?: return
        if (javaDocLocalInspection.myAdditionalJavadocTags.isEmpty()) {
            javaDocLocalInspection.myAdditionalJavadocTags = tag
            return
        }

        val tags = Splitter.on(',').splitToList(javaDocLocalInspection.myAdditionalJavadocTags)
        if (tags.contains(tag)) {
            return
        }
        javaDocLocalInspection.myAdditionalJavadocTags += "," + tag
        profile.profileChanged()
        profile.scopesChanged()
    }

    private fun getAllTools(project: Project, profile: InspectionProfileImpl): List<InspectionToolWrapper<*, *>> {
        val method = InspectionProfileImpl::class.java.methods.first {
            it.name == "getAllTools"
        }

        val result = if (method.parameterTypes.isNotEmpty()) {
            method.invoke(profile, project)
        } else {
            method.invoke(profile)
        }
        return (result as List<ScopeToolState>).map { it.tool }
    }
}
