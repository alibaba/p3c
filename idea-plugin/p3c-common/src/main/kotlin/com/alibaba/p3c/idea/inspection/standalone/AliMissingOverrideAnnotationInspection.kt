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
import com.alibaba.p3c.idea.quickfix.DecorateInspectionGadgetsFix
import com.alibaba.p3c.idea.util.HighlightDisplayLevels
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.util.InheritanceUtil
import com.siyeh.ig.BaseInspectionVisitor
import com.siyeh.ig.InspectionGadgetsFix
import com.siyeh.ig.inheritance.MissingOverrideAnnotationInspection
import com.siyeh.ig.psiutils.MethodUtils
import javax.swing.JComponent

/**
 * Batch QuickFix Supported
 * @author caikang
 * @date 2016/12/08
 */
class AliMissingOverrideAnnotationInspection : MissingOverrideAnnotationInspection, AliBaseInspection {
    private val messageKey = "com.alibaba.p3c.idea.inspection.standalone.AliMissingOverrideAnnotationInspection"

    constructor()
    /**
     * For Javassist
     */
    constructor(any: Any?) : this()

    init {
        ignoreAnonymousClassMethods = false
        ignoreObjectMethods = false
    }

    override fun getDisplayName(): String = P3cBundle.getMessage("$messageKey.message")

    override fun getStaticDescription(): String? = P3cBundle.getMessage("$messageKey.desc")

    override fun ruleName(): String = "MissingOverrideAnnotationRule"

    override fun buildErrorString(vararg infos: Any): String = P3cBundle.getMessage("$messageKey.errMsg")

    override fun createOptionsPanel(): JComponent? = null

    override fun buildFix(vararg infos: Any): InspectionGadgetsFix? {
        val fix = super.buildFix(*infos) ?: return null
        return DecorateInspectionGadgetsFix(fix,
            P3cBundle.getMessage("com.alibaba.p3c.idea.quickfix.standalone.AliMissingOverrideAnnotationInspection"))
    }

    override fun manualBuildFix(psiElement: PsiElement, isOnTheFly: Boolean): LocalQuickFix? = buildFix(psiElement)

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevels.BLOCKER

    override fun buildVisitor(): BaseInspectionVisitor = MissingOverrideAnnotationVisitor()

    private inner class MissingOverrideAnnotationVisitor : BaseInspectionVisitor() {

        override fun visitMethod(method: PsiMethod) {
            if (method.nameIdentifier == null) {
                return
            }
            if (method.isConstructor) {
                return
            }
            if (method.hasModifierProperty(PsiModifier.PRIVATE) || method.hasModifierProperty(PsiModifier.STATIC)) {
                return
            }
            val methodClass = method.containingClass ?: return
            if (ignoreAnonymousClassMethods && methodClass is PsiAnonymousClass) {
                return
            }
            if (hasOverrideAnnotation(method)) {
                return
            }
            if (!isJdk6Override(method, methodClass) && !isJdk5Override(method, methodClass)) {
                return
            }
            if (ignoreObjectMethods && (MethodUtils.isHashCode(method) ||
                    MethodUtils.isEquals(method) ||
                    MethodUtils.isToString(method))) {
                return
            }
            registerMethodError(method)
        }

        private fun hasOverrideAnnotation(element: PsiModifierListOwner): Boolean {
            val modifierList = element.modifierList
            return modifierList?.findAnnotation(CommonClassNames.JAVA_LANG_OVERRIDE) != null
        }

        private fun isJdk6Override(method: PsiMethod, methodClass: PsiClass): Boolean {
            val superMethods = method.findSuperMethods()
            var hasSupers = false
            for (superMethod in superMethods) {
                val superClass = superMethod.containingClass
                if (!InheritanceUtil.isInheritorOrSelf(methodClass, superClass, true)) {
                    continue
                }
                hasSupers = true
                if (!superMethod.hasModifierProperty(PsiModifier.PROTECTED)) {
                    return true
                }
            }
            // is override except if this is an interface method
            // overriding a protected method in java.lang.Object
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6501053
            return hasSupers && !methodClass.isInterface
        }

        private fun isJdk5Override(method: PsiMethod, methodClass: PsiClass): Boolean {
            val superMethods = method.findSuperMethods()
            for (superMethod in superMethods) {
                val superClass = superMethod.containingClass
                if (superClass == null || !InheritanceUtil.isInheritorOrSelf(methodClass, superClass, true)) {
                    continue
                }
                if (superClass.isInterface) {
                    continue
                }
                if (methodClass.isInterface && superMethod.hasModifierProperty(PsiModifier.PROTECTED)) {
                    // only true for J2SE java.lang.Object.clone(), but might
                    // be different on other/newer java platforms
                    continue
                }
                return true
            }
            return false
        }
    }
}
