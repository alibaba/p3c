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
import com.alibaba.p3c.idea.pmd.AliPmdProcessor
import com.alibaba.p3c.idea.util.DocumentUtils.calculateLineStart
import com.alibaba.p3c.idea.util.DocumentUtils.calculateRealOffset
import com.alibaba.p3c.idea.util.ProblemsUtils
import com.alibaba.p3c.pmd.lang.java.rule.comment.RemoveCommentedCodeRule
import com.beust.jcommander.internal.Lists
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RuleViolation
import java.util.concurrent.TimeUnit

/**
 * @author caikang
 * @date 2016/12/13
 */
class AliPmdInspectionInvoker(
        private val psiFile: PsiFile,
        private val manager: InspectionManager,
        private val rule: Rule
) {
    private val logger = Logger.getInstance(javaClass)

    private var violations: List<RuleViolation> = emptyList()

    fun doInvoke() {
        Thread.currentThread().contextClassLoader = javaClass.classLoader
        val processor = AliPmdProcessor(rule)
        val start = System.currentTimeMillis()
        violations = processor.processFile(psiFile)
        logger.debug("elapsed ${System.currentTimeMillis() - start}ms to" +
                " to apply rule ${rule.name} for file ${psiFile.virtualFile.canonicalPath}")
    }

    fun getRuleProblems(isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (violations.isEmpty()) {
            return null
        }
        val problemDescriptors = Lists.newArrayList<ProblemDescriptor>(violations.size)
        for (rv in violations) {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(rv.filename) ?: continue
            val psiFile = PsiManager.getInstance(manager.project).findFile(virtualFile) ?: continue
            val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: continue

            val offsets = if (rv.rule.name == RemoveCommentedCodeRule::class.java.simpleName) {
                Offsets(calculateLineStart(document, rv.beginLine),
                        calculateLineStart(document, rv.endLine + 1) - 1)
            } else {
                Offsets(calculateRealOffset(document, rv.beginLine, rv.beginColumn),
                        calculateRealOffset(document, rv.endLine, rv.endColumn))
            }
            val errorMessage = if (isOnTheFly) {
                rv.description
            } else {
                "${rv.description} (line ${rv.beginLine})"
            }
            val problemDescriptor = ProblemsUtils.createProblemDescriptorForPmdRule(psiFile, manager,
                    isOnTheFly, rv.rule.name, errorMessage, offsets.start, offsets.end, rv.beginLine) ?: continue
            problemDescriptors.add(problemDescriptor)
        }
        return problemDescriptors.toTypedArray()
    }

    companion object {
        private lateinit var invokers: Cache<FileRule, AliPmdInspectionInvoker>

        val smartFoxConfig = ServiceManager.getService(P3cConfig::class.java)!!

        init {
            reInitInvokers(smartFoxConfig.ruleCacheTime)
        }

        fun invokeInspection(psiFile: PsiFile?, manager: InspectionManager, rule: Rule,
                isOnTheFly: Boolean): Array<ProblemDescriptor>? {
            if (psiFile == null) {
                return null
            }
            val virtualFile = psiFile.virtualFile ?: return null
            if (!smartFoxConfig.ruleCacheEnable) {
                val invoker = AliPmdInspectionInvoker(psiFile, manager, rule)
                invoker.doInvoke()
                return invoker.getRuleProblems(isOnTheFly)
            }
            var invoker = invokers.getIfPresent(FileRule(virtualFile.canonicalPath!!, rule.name))
            if (invoker == null) {
                synchronized(virtualFile) {
                    invoker = invokers.getIfPresent(virtualFile.canonicalPath)
                    if (invoker == null) {
                        invoker = AliPmdInspectionInvoker(psiFile, manager, rule)
                        invoker!!.doInvoke()
                        invokers.put(FileRule(virtualFile.canonicalPath!!, rule.name), invoker)
                    }
                }
            }
            return invoker!!.getRuleProblems(isOnTheFly)
        }

        private fun doInvokeIfPresent(filePath: String, rule: String) {
            invokers.getIfPresent(FileRule(filePath, rule))?.doInvoke()
        }

        fun refreshFileViolationsCache(file: VirtualFile) {
            AliLocalInspectionToolProvider.ruleNames.forEach {
                doInvokeIfPresent(file.canonicalPath!!, it)
            }
        }

        fun reInitInvokers(expireTime: Long) {
            invokers = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(expireTime,
                    TimeUnit.MILLISECONDS).build<FileRule, AliPmdInspectionInvoker>()!!
        }
    }
}

data class FileRule(val filePath: String, val ruleName: String)
data class Offsets(val start: Int, val end: Int)
