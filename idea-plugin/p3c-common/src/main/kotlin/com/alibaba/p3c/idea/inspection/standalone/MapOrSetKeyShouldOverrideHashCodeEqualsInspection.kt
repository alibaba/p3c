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
package com.alibaba.p3c.idea.inspection.standalone

import com.alibaba.p3c.idea.i18n.P3cBundle
import com.alibaba.p3c.idea.inspection.AliBaseInspection
import com.alibaba.p3c.idea.util.HighlightDisplayLevels
import com.alibaba.p3c.idea.util.NumberConstants
import com.alibaba.p3c.idea.util.ObjectConstants
import com.google.common.collect.Sets
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.PsiVariable
import com.siyeh.ig.BaseInspection
import com.siyeh.ig.BaseInspectionVisitor
import org.jetbrains.annotations.NonNls

/**
 * @author caikang
 * @date 2017/03/01
 */
class MapOrSetKeyShouldOverrideHashCodeEqualsInspection : BaseInspection, AliBaseInspection {
    val messageKey = "com.alibaba.p3c.idea.inspection.standalone.MapOrSetKeyShouldOverrideHashCodeEqualsInspection"

    constructor()
    /**
     * For Javassist
     */
    constructor(any: Any?) : this()

    override fun getDisplayName(): String {
        return P3cBundle.getMessage("$messageKey.message")
    }

    override fun getStaticDescription(): String? {
        return P3cBundle.getMessage("$messageKey.desc")
    }

    override fun buildErrorString(vararg infos: Any): String {
        val type = infos[0] as PsiClassType
        return String.format(P3cBundle.getMessage("$messageKey.errMsg"), type.className)
    }

    override fun buildVisitor(): BaseInspectionVisitor {
        return MapOrSetKeyVisitor()
    }

    override fun ruleName(): String {
        return "MapOrSetKeyShouldOverrideHashCodeEqualsRule"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevels.CRITICAL
    }

    internal enum class ClassType {
        /**
         * parameter type is Set
         */
        SET,
        MAP, OTHER;

        override fun toString(): String {
            val string = super.toString()
            return string[0] + string.substring(1).toLowerCase()
        }
    }

    private class MapOrSetKeyVisitor : BaseInspectionVisitor() {

        private fun getClassType(aClass: PsiClass?): ClassType {
            return isMapOrSet(aClass, Sets.newHashSet<PsiClass>())
        }

        private fun isMapOrSet(aClass: PsiClass?, visitedClasses: MutableSet<PsiClass>): ClassType {
            if (aClass == null) {
                return ClassType.OTHER
            }
            if (!visitedClasses.add(aClass)) {
                return ClassType.OTHER
            }
            @NonNls
            val className = aClass.qualifiedName
            if (CommonClassNames.JAVA_UTIL_SET == className) {
                return ClassType.SET
            }
            if (CommonClassNames.JAVA_UTIL_MAP == className) {
                return ClassType.MAP
            }
            val supers = aClass.supers
            return supers
                    .map { isMapOrSet(it, visitedClasses) }
                    .firstOrNull { it != ClassType.OTHER }
                    ?: ClassType.OTHER
        }

        override fun visitVariable(variable: PsiVariable) {
            super.visitVariable(variable)
            val typeElement = variable.typeElement ?: return
            val type = typeElement.type as? PsiClassType ?: return
            val referenceElement = typeElement.innermostComponentReferenceElement ?: return
            val aClass = type.resolve()

            val collectionType = getClassType(aClass)
            if (collectionType == ClassType.OTHER) {
                return
            }
            val parameterList = referenceElement.parameterList
            if (parameterList == null || parameterList.typeParameterElements.size == NumberConstants.INTEGER_SIZE_OR_LENGTH_0) {
                return
            }
            val psiType = parameterList.typeArguments[0]
            if (!redefineHashCodeEquals(psiType)) {
                registerError(parameterList.typeParameterElements[0], psiType)
            }
        }

        override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
            val methodExpression = expression.methodExpression
            val qualifierExpression = methodExpression.qualifierExpression ?: return
            val type = qualifierExpression.type as? PsiClassType ?: return
            val aClass = type.resolve()

            val collectionType = getClassType(aClass)
            if (collectionType == ClassType.OTHER) {
                return
            }
            @NonNls
            val methodName = methodExpression.referenceName
            if (collectionType == ClassType.SET && ObjectConstants.METHOD_NAME_ADD != methodName) {
                return
            }
            if (collectionType == ClassType.MAP && ObjectConstants.METHOD_NAME_PUT != methodName) {
                return
            }
            val argumentList = expression.argumentList
            val arguments = argumentList.expressions
            if (collectionType == ClassType.SET && arguments.size != NumberConstants.INTEGER_SIZE_OR_LENGTH_1) {
                return
            }
            if (collectionType == ClassType.MAP && arguments.size != NumberConstants.INTEGER_SIZE_OR_LENGTH_2) {
                return
            }
            val argument = arguments[0]
            val argumentType = argument.type
            if (argumentType == null || redefineHashCodeEquals(argumentType)) {
                return
            }
            registerMethodCallError(expression, argumentType)
        }
    }

    companion object {

        private val skipJdkPackageJava = "java."
        private val skipJdkPackageJavax = "javax."

        private fun redefineHashCodeEquals(psiType: PsiType): Boolean {
            if (psiType !is PsiClassType) {
                return true
            }
            val psiClass = psiType.resolve() ?: return false
            val skip = psiClass.containingFile == null || psiClass is PsiTypeParameter
                    || psiClass.isEnum || psiClass.isInterface
                    || psiClass.containingFile.fileType !is JavaFileType
                    || psiClass.qualifiedName?.startsWith(skipJdkPackageJava) ?: false
                    || psiClass.qualifiedName?.startsWith(skipJdkPackageJavax) ?: false
            if (skip) {
                return true
            }
            val hashCodeMethods = psiClass.findMethodsByName(ObjectConstants.METHOD_NAME_HASHCODE, false)
            if (hashCodeMethods.size == NumberConstants.INTEGER_SIZE_OR_LENGTH_0) {
                return false
            }
            val equalsMethods = psiClass.findMethodsByName(ObjectConstants.METHOD_NAME_EQUALS, false)
            return equalsMethods.size > NumberConstants.INTEGER_SIZE_OR_LENGTH_0
        }
    }
}
