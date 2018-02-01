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

import net.sourceforge.pmd.RuleContext
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.ClassInstanceCreation
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.FieldAccess
import org.eclipse.jdt.core.dom.IVariableBinding
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.Modifier
import org.eclipse.jdt.core.dom.QualifiedName
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.jdt.core.dom.VariableDeclarationFragment

/**
 * @author caikang
 * @date 2016/12/27
 */
class AvoidAccessStaticViaInstanceRule : AbstractEclipseRule() {
    override fun getVisitor(ast: CompilationUnit, ruleContext: RuleContext): ASTVisitor {
        return object : ASTVisitor() {
            override fun visit(node: QualifiedName?): Boolean {
                val parent = node!!.parent
                if (parent !is MethodInvocation && parent !is VariableDeclarationFragment) {
                    return false
                }
                val name = node.name
                val binding = name.resolveBinding()
                if (binding !is IVariableBinding || binding.getModifiers() and Modifier.STATIC == 0) {
                    return true
                }
                val qualifier = node.qualifier
                val typeBinding = qualifier.resolveTypeBinding()
                if (qualifier.isSimpleName) {
                    when (parent) {
                        is MethodInvocation -> {
                            val methodBinding = parent.resolveMethodBinding()
                            val methodTypeBinding = methodBinding.declaringClass
                            if (methodBinding.modifiers and Modifier.STATIC != 0
                                    && qualifier.fullyQualifiedName != methodTypeBinding.name
                                    && methodTypeBinding == typeBinding && binding.name != typeBinding.name) {
                                violation(ruleContext, parent, ast)
                                return false
                            }
                        }
                        else -> {
                        }
                    }
                    if (typeBinding.name != qualifier.fullyQualifiedName) {
                        violation(ruleContext, node, ast)
                        return false
                    }
                }
                if (qualifier.isQualifiedName) {
                    val qualifiedName = qualifier as QualifiedName
                    if (typeBinding.name != qualifiedName.name.identifier) {
                        violation(ruleContext, node, ast)
                        return false
                    }
                }
                return true
            }

            override fun visit(node: FieldAccess): Boolean {
                val variableBinding = node.resolveFieldBinding() ?: return false
                if (variableBinding.modifiers and Modifier.STATIC == 0) {
                    return true
                }
                if (node.expression is ClassInstanceCreation) {
                    violation(ruleContext, node, ast)
                    return true
                }
                return true
            }

            override fun visit(node: MethodInvocation?): Boolean {
                val methodBinding = node?.resolveMethodBinding() ?: return false
                if (methodBinding.modifiers and Modifier.STATIC == 0) {
                    return true
                }
                if (node.expression is ClassInstanceCreation) {
                    violation(ruleContext, node, ast)
                    return true
                }
                val expression = node.expression
                if (expression is SimpleName && expression.identifier != expression.resolveTypeBinding().name) {
                    violation(ruleContext, node, ast)
                    return true
                }
                return true
            }
        }
    }

}
