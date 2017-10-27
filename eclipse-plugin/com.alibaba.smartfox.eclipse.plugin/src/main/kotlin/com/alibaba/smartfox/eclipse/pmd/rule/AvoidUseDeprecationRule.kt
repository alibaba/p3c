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
import org.eclipse.jdt.core.dom.ImportDeclaration
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.Modifier
import org.eclipse.jdt.core.dom.QualifiedName
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.VariableDeclarationFragment

/**
 * @author caikang
 * @date 2016/12/27
 */
class AvoidUseDeprecationRule : AbstractEclipseRule() {
    override fun getVisitor(ast: CompilationUnit, ruleContext: RuleContext): ASTVisitor {
        return object : ASTVisitor() {
            override fun visit(node: ImportDeclaration?): Boolean {
                if (node!!.resolveBinding() != null && node.resolveBinding().isDeprecated) {
                    violation(ruleContext, node, ast)
                }
                return true
            }

            override fun visit(node: QualifiedName?): Boolean {
                if (node!!.parent !is MethodInvocation && node.parent !is VariableDeclarationFragment) {
                    return false
                }
                val name = node.name
                val binding = name.resolveBinding()
                if (binding !is IVariableBinding || binding.getModifiers() and Modifier.STATIC == 0) {
                    return true
                }
                if (binding.isDeprecated()) {
                    violation(ruleContext, node, ast)
                    return false
                }
                val qualifier = node.qualifier
                val typeBinding = qualifier.resolveTypeBinding()
                if (typeBinding.isDeprecated) {
                    violation(ruleContext, node, ast)
                    return false
                }
                return true
            }

            override fun visit(node: TypeDeclaration?): Boolean {
                val superClass = node!!.superclassType
                if (superClass != null && superClass.resolveBinding().isDeprecated) {
                    violation(ruleContext, node, ast)
                    return true
                }
                val interfaces = node.resolveBinding().interfaces
                for (tb in interfaces) {
                    if (tb.isDeprecated) {
                        violation(ruleContext, node, ast)
                        return true
                    }
                }
                return true
            }

            override fun visit(node: FieldAccess?): Boolean {
                val variableBinding = node!!.resolveFieldBinding() ?: return false
                if (variableBinding.isDeprecated) {
                    violation(ruleContext, node, ast)
                }
                return true
            }

            override fun visit(node: MethodInvocation): Boolean {
                val methodBinding = node.resolveMethodBinding() ?: return false
                if (methodBinding.isDeprecated) {
                    violation(ruleContext, node, ast)
                }
                return true
            }

            override fun visit(node: ClassInstanceCreation?): Boolean {
                if (node!!.resolveTypeBinding().isDeprecated) {
                    violation(ruleContext, node, ast)
                }
                return true
            }
        }
    }
}
