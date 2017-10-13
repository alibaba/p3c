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
package com.alibaba.p3c.idea.pmd.index

import com.beust.jcommander.internal.Lists
import com.intellij.util.indexing.FileContent
import net.sourceforge.pmd.PMD
import net.sourceforge.pmd.PMDConfiguration
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.RuleSetFactory
import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.RulesetsFactoryUtils
import net.sourceforge.pmd.renderers.Renderer
import net.sourceforge.pmd.util.datasource.DataSource

/**
 * @author caikang
 * @date 2016/12/11
 */
class AliPmdProcessor {
    private val ruleSetFactory: RuleSetFactory
    private val configuration = PMDConfiguration()

    init {
        configuration.ruleSets = "java-ali-pmd,vm-ali-other"
        configuration.threads = 0
        ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration)
    }

    fun processFile(fileContent: FileContent): List<RuleViolation> {
        val renderer = InspectionRenderer()
        val dataSources = Lists.newArrayList<DataSource>()
        dataSources.add(InspectionDataSource(fileContent))
        PMD.processFiles(configuration, ruleSetFactory, dataSources, RuleContext(),
                listOf<Renderer>(renderer))
        return renderer.getViolations()
    }
}
