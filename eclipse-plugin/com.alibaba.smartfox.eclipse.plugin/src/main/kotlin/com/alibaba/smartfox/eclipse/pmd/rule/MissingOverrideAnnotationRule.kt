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
import net.sourceforge.pmd.RuleContext
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding
import java.util.Arrays
import javax.annotation.Generated

/**
 * @author caikang
 * @date 2016/12/24
 */
class MissingOverrideAnnotationRule : AbstractEclipseRule() {
    override fun getErrorMessage(): String {
        return P3cBundle.getMessage("rule.standalone.MissingOverrideAnnotationRule.error")
    }

    override fun getVisitor(ast: CompilationUnit, ruleContext: RuleContext): ASTVisitor {
        return MissingOverrideVisitor(ast, ruleContext)
    }

    private inner class MissingOverrideVisitor(private val ast: CompilationUnit,
            private val ruleContext: RuleContext) : ASTVisitor() {

        override fun visit(node: MethodDeclaration?): Boolean {
            val methodBinding = node!!.resolveBinding()
            val declaringClass = methodBinding.declaringClass ?: return super.visit(node)
            if (declaringClass.isInterface) {
                return super.visit(node)
            }
            val abs = methodBinding.annotations
            if (abs.any {
                Override::class.java.canonicalName == it.annotationType.binaryName
            }) {
                return super.visit(node)
            }
            try {
                val field = methodBinding.javaClass.getDeclaredField("binding")
                field.isAccessible = true
                val internalBinding = field.get(methodBinding) as MethodBinding
                if (internalBinding.isStatic || !(internalBinding.isImplementing || internalBinding.isOverriding)
                        || isGenerated(internalBinding)) {
                    return super.visit(node)
                }
                violation(ruleContext, node, ast)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return super.visit(node)
        }

        /**
         * skip @Override check for generated code by lombok
         * @param internalBinding
         * @return
         */
        private fun isGenerated(internalBinding: MethodBinding): Boolean {
            val annotationBindings = internalBinding.annotations ?: return false
            return annotationBindings.any {
                it.annotationType != null && Arrays.equals(Generated::class.java.name.toCharArray(),
                        it.annotationType.readableName())
            }
        }
    }
}
