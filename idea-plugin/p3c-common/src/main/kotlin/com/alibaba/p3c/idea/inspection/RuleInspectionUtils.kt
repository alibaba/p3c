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
package com.alibaba.p3c.idea.inspection

import com.alibaba.p3c.idea.config.P3cConfig
import com.alibaba.p3c.idea.util.HighlightDisplayLevels
import com.alibaba.p3c.idea.util.NumberConstants
import com.alibaba.p3c.pmd.I18nResources
import com.alibaba.smartfox.idea.common.util.getService
import com.google.common.base.Joiner
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.URLUtil
import freemarker.template.Configuration
import freemarker.template.TemplateException
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RulePriority
import net.sourceforge.pmd.RuleSetFactory
import net.sourceforge.pmd.RuleSetNotFoundException
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException
import java.io.StringWriter
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * @author caikang
 * @date 2016/12/16
 */
object RuleInspectionUtils {

    private val logger = Logger.getInstance(RuleInspectionUtils::class.java)
    private val ruleSetFilePattern = Pattern.compile("(java|vm)/ali-.*?\\.xml")
    private val staticDescriptionTemplate = run {
        val cfg = Configuration(Configuration.VERSION_2_3_25)
        cfg.setClassForTemplateLoading(RuleInspectionUtils::class.java, "/tpl")
        cfg.defaultEncoding = "UTF-8"
        cfg.getTemplate("StaticDescriptionTemplate.ftl")
    }

    private val ruleSetsPrefix = "rulesets/"

    private val ruleStaticDescriptions: Map<String, String>
    private val ruleMessages: Map<String, String>
    private val displayLevelMap: Map<String, HighlightDisplayLevel>

    init {
        I18nResources.changeLanguage(P3cConfig::class.java.getService().locale)
        val builder = ImmutableMap.builder<String, String>()
        val messageBuilder = ImmutableMap.builder<String, String>()
        val displayLevelBuilder = ImmutableMap.builder<String, HighlightDisplayLevel>()
        val rules = loadAllAlibabaRule()
        for (rule in rules) {
            builder.put(rule.name, parseStaticDescription(rule))
            messageBuilder.put(rule.name, rule.message)
            displayLevelBuilder.put(rule.name, getHighlightDisplayLevel(rule.priority))
        }
        ruleStaticDescriptions = builder.build()
        ruleMessages = messageBuilder.build()
        displayLevelMap = displayLevelBuilder.build()
    }

    fun getRuleStaticDescription(ruleName: String): String {
        return ruleStaticDescriptions[ruleName]!!
    }

    fun getHighlightDisplayLevel(ruleName: String): HighlightDisplayLevel {
        val level = displayLevelMap[ruleName]

        return level ?: HighlightDisplayLevel.WEAK_WARNING
    }

    fun getHighlightDisplayLevel(rulePriority: RulePriority): HighlightDisplayLevel {
        when (rulePriority) {
            RulePriority.HIGH -> return HighlightDisplayLevels.BLOCKER
            RulePriority.MEDIUM_HIGH -> return HighlightDisplayLevels.CRITICAL
            else -> return HighlightDisplayLevels.MAJOR
        }
    }

    fun getRuleMessage(ruleName: String): String {
        return ruleMessages[ruleName]!!
    }

    private fun parseStaticDescription(rule: Rule): String {
        val writer = StringWriter()
        try {
            val map = Maps.newHashMap<String, Any>()
            map.put("message", StringUtils.trimToEmpty(rule.message))
            map.put("description", StringUtils.trimToEmpty(rule.description))
            val examples = rule.examples.map {
                it?.trim {
                    c ->
                    c == '\n'
                }
            }
            map.put("examples", examples)
            staticDescriptionTemplate.process(map, writer)
        } catch (e: TemplateException) {
            logger.error(e)
        } catch (e: IOException) {
            logger.error(e)
        }
        return writer.toString()
    }

    private fun loadAllAlibabaRule(): List<Rule> {
        try {
            Thread.currentThread().contextClassLoader = RuleInspectionUtils::class.java.classLoader
            val ruleSetConfigs = findRuleSetConfigs()
            val ruleSetFactory = RuleSetFactory()
            val ruleSets = ruleSetFactory.createRuleSets(
                    Joiner.on(",").join(ruleSetConfigs).replace("/".toRegex(), "-"))
            val map = Maps.newHashMap<String, Rule>()
            ruleSets.allRuleSets
                    .asSequence()
                    .flatMap { it.rules.asSequence() }
                    .forEach { map.put(it.name, it) }
            return Lists.newArrayList(map.values)
        } catch (e: IOException) {
            logger.warn("no available alibaba rules")
            return emptyList()
        } catch (e: RuleSetNotFoundException) {
            logger.error("rule sets not found", e)
            return emptyList()
        }

    }

    @Throws(IOException::class)
    private fun findRuleSetConfigs(): List<String> {
        val ruleSets = Lists.newArrayList<String>()
        val enumeration = RuleInspectionUtils::class.java.classLoader.getResources(ruleSetsPrefix)
        while (enumeration.hasMoreElements()) {
            val url = enumeration.nextElement()
            if (URLUtil.JAR_PROTOCOL == url.protocol) {
                findRuleSetsFromJar(ruleSets, url)
            } else if (URLUtil.FILE_PROTOCOL == url.protocol) {
                findRuleSetsFromDirectory(ruleSets, url)
            }
        }
        return ruleSets
    }

    @Throws(IOException::class)
    private fun findRuleSetsFromDirectory(ruleSets: MutableList<String>, url: URL) {
        val file = File(url.path)
        if (file.exists() && file.isDirectory) {
            val files = Lists.newArrayList<File>()
            FileUtil.collectMatchedFiles(file, ruleSetFilePattern, files)
            files.mapTo(ruleSets) { it.canonicalPath.replace(url.path, "").replace(".xml", "") }
        }
    }

    @Throws(IOException::class)
    private fun findRuleSetsFromJar(ruleSets: MutableList<String>, url: URL) {
        logger.info("start to find rule sets from jar " + url)
        var path = URLDecoder.decode(url.path, StandardCharsets.UTF_8.name())
        val index = path.lastIndexOf(URLUtil.JAR_SEPARATOR)
        if (index > NumberConstants.INDEX_0) {
            path = path.substring("file:".length, index)
        }
        val jarFile = JarFile(path)
        logger.info("create jarFile for path " + path)
        val jarEntries = jarFile.entries()
        while (jarEntries.hasMoreElements()) {
            val jarEntry = jarEntries.nextElement()
            val subPath = jarEntry.name.replace(ruleSetsPrefix, "")
            if (ruleSetFilePattern.matcher(subPath).find()) {
                val resultPath = subPath.replace(".xml", "")
                logger.info("get result rule set " + resultPath)
                ruleSets.add(resultPath)
            }
        }
        logger.info("find rule sets from jar $url finished")
    }
}
