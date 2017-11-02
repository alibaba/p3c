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
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.ITypeBinding
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.VariableDeclarationStatement

/**
 * @author zenghou.fw
 * @date 2016/12/27
 */
class MapOrSetKeyShouldOverrideHashCodeEqualsRule : AbstractEclipseRule() {

    val methodEquals = "equals"
    val methodHashCode = "hashCode"
    val methodAdd = "add"
    val methodPut = "put"

    private val skipJdkPackageJava = "java."
    private val skipJdkPackageJavax = "javax."

    override fun getVisitor(ast: CompilationUnit, ruleContext: RuleContext): ASTVisitor {
        return object : ASTVisitor() {

            override fun visit(node: VariableDeclarationStatement): Boolean {
                if (!node.type.isParameterizedType) {
                    return true
                }
                val typeBinding = node.type.resolveBinding()
                if (isSet(typeBinding) || isMap(typeBinding)) {
                    val argumentTypes = typeBinding.typeArguments
                    if (argumentTypes != null && argumentTypes.isNotEmpty()) {
                        if (!isOverrideEqualsAndHashCode(argumentTypes[0])) {
                            violation(ruleContext, node, ast)
                            return false
                        }
                    }
                }
                return true
            }

            override fun visit(node: MethodInvocation): Boolean {
                val methodBinding = node.resolveMethodBinding() ?: return false
                val callerType = methodBinding.declaringClass

                if (methodAdd == methodBinding.name) {
                    if (!isSet(callerType)) {
                        return true
                    }
                    val parameterTypes = methodBinding.parameterTypes
                    if (parameterTypes != null && parameterTypes.isNotEmpty()) {
                        if (!isOverrideEqualsAndHashCode(parameterTypes[0])) {
                            violation(ruleContext, node, ast)
                            return false
                        }
                    }
                    return true
                }
                if (methodPut == methodBinding.name) {
                    if (!isMap(callerType)) {
                        return true
                    }
                    val parameterTypes = methodBinding.parameterTypes
                    if (parameterTypes != null && parameterTypes.isNotEmpty()) {
                        if (!isOverrideEqualsAndHashCode(parameterTypes[0])) {
                            violation(ruleContext, node, ast)
                            return false
                        }
                    }
                }
                return true
            }

            private fun isOverrideEqualsAndHashCode(genericType: ITypeBinding): Boolean {
                val skip = genericType.isEnum || genericType.isInterface || genericType.isArray
                        || genericType.isTypeVariable || genericType.isWildcardType
                        || genericType.qualifiedName?.startsWith(skipJdkPackageJava) ?: false
                        || genericType.qualifiedName?.startsWith(skipJdkPackageJavax) ?: false
                // skip
                if (skip) {
                    return true
                }

                val methodBindings = genericType.declaredMethods ?: return false

                val overrideCount = methodBindings.asSequence().filter {
                    //find equals(Object o) and hashCode() with @Override
                    methodEquals == it.name || methodHashCode == it.name
                }.filter {
                    when (it.name) {
                        methodEquals -> {
                            val parameterTypes = it.parameterTypes
                            parameterTypes != null && parameterTypes.isNotEmpty()
                                    && Object::class.java.name == parameterTypes[0].qualifiedName
                        }
                        methodHashCode -> {
                            val parameterTypes = it.parameterTypes
                            parameterTypes == null || parameterTypes.isEmpty()
                        }
                        else -> false
                    }
                }.count()
                return overrideCount == 2
            }

            private fun isSet(typeBinding: ITypeBinding): Boolean {
                return java.util.Set::class.java.name == typeBinding.binaryName
            }

            private fun isMap(typeBinding: ITypeBinding): Boolean {
                return java.util.Map::class.java.name == typeBinding.binaryName
            }
        }
    }
}
