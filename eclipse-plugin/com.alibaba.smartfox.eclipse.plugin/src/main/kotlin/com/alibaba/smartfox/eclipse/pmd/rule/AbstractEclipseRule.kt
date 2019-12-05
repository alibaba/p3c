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
package com.alibaba.smartfox.eclipse.pmd.rule

import com.alibaba.smartfox.eclipse.message.P3cBundle
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import org.eclipse.core.resources.IFile
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IProblemRequestor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.JavaModelException
import org.eclipse.jdt.core.WorkingCopyOwner
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.CompilationUnit
import java.util.MissingResourceException

/**
 * @author caikang
 * @date 2016/12/26
 */
abstract class AbstractEclipseRule : AbstractJavaRule() {

    open fun getErrorMessage(): String {
        return message
    }

    override fun visit(node: ASTCompilationUnit, data: Any): Any? {
        val result = super.visit(node, data)
        val ruleContext = data as RuleContext
        val file = ruleContext.getAttribute("eclipseFile") as? IFile ?: return result
        val compilationUnit = JavaCore.createCompilationUnitFrom(file) ?: return data
        try {
            val requestor = object : IProblemRequestor {
                override fun acceptProblem(problem: IProblem) {}

                override fun beginReporting() {}

                override fun endReporting() {}

                override fun isActive(): Boolean {
                    return true
                }
            }
            val workingCopy = compilationUnit.getWorkingCopy(null)
            val ast = workingCopy.reconcile(JLS8, ICompilationUnit.FORCE_PROBLEM_DETECTION
                    or ICompilationUnit.ENABLE_BINDINGS_RECOVERY or ICompilationUnit.ENABLE_STATEMENTS_RECOVERY,
                    object : WorkingCopyOwner() {
                        override fun getProblemRequestor(workingCopy: ICompilationUnit?): IProblemRequestor {
                            return requestor
                        }
                    }, NullProgressMonitor()) ?: return data
            ast.accept(getVisitor(ast, ruleContext))

        } catch (e: JavaModelException) {
            throw RuntimeException(e)
        }

        return data
    }

    override fun setDescription(description: String?) {
        try {
            super.setDescription(P3cBundle.getMessage(description ?: ""))
        } catch (e: MissingResourceException) {
            super.setMessage(description)
        }

    }

    override fun setMessage(message: String) {
        try {
            super.setMessage(P3cBundle.getMessage(message))
        } catch (e: MissingResourceException) {
            super.setMessage(message)
        }

    }


    protected abstract fun getVisitor(ast: CompilationUnit, ruleContext: RuleContext): ASTVisitor

    internal fun addRuleViolation(ruleContext: RuleContext, ast: CompilationUnit, nodeInfo: NodeInfo) {
        val rule = this
        val ruleViolation = object : RuleViolation {
            override fun getRule(): Rule {
                return rule
            }

            override fun getDescription(): String {
                return getErrorMessage().trim { it <= ' ' }
            }

            override fun isSuppressed(): Boolean {
                return false
            }

            override fun getFilename(): String {
                return ruleContext.sourceCodeFilename
            }

            override fun getBeginLine(): Int {
                return ast.getLineNumber(nodeInfo.startPosition)
            }

            override fun getBeginColumn(): Int {
                return ast.getColumnNumber(nodeInfo.startPosition)
            }

            override fun getEndLine(): Int {
                return ast.getLineNumber(nodeInfo.endPosition)
            }

            override fun getEndColumn(): Int {
                return ast.getColumnNumber(nodeInfo.endPosition)
            }

            override fun getPackageName(): String? {
                return nodeInfo.packageName
            }

            override fun getClassName(): String? {
                return nodeInfo.className
            }

            override fun getMethodName(): String? {
                return nodeInfo.methodName
            }

            override fun getVariableName(): String? {
                return nodeInfo.variableName
            }

            override fun toString(): String {
                return rule.toString()
            }


        }

        ruleContext.report.addRuleViolation(ruleViolation)
    }

    protected fun violation(rc: RuleContext, node: ASTNode, ast: CompilationUnit) {
        val nodeInfo = NodeInfo()
        nodeInfo.className = ast.javaElement.elementName
        nodeInfo.packageName = ast.`package`.name.fullyQualifiedName
        nodeInfo.startPosition = node.startPosition
        nodeInfo.endPosition = node.startPosition + node.length

        addRuleViolation(rc, ast, nodeInfo)
    }

    inner class NodeInfo {
        internal var packageName: String? = null
        internal var className: String? = null
        internal var methodName: String? = null
        internal var variableName: String? = null
        internal var startPosition: Int = 0
        internal var endPosition: Int = 0
    }

    companion object {
        val JLS8 = 8
    }
}
