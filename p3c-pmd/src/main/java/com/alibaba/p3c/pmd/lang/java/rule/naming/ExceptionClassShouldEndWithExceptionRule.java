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
package com.alibaba.p3c.pmd.lang.java.rule.naming;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTExtendsList;
import net.sourceforge.pmd.lang.java.typeresolution.TypeHelper;
import org.apache.commons.lang3.StringUtils;

/**
 * [Mandatory] Exception class names must be ended with Exception.
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class ExceptionClassShouldEndWithExceptionRule extends AbstractAliRule {

    private static final String EXCEPTION_END_SUFFIX = "Exception";

    @Override
    public Object visit(ASTExtendsList node, Object data) {
        ASTClassOrInterfaceType astClassOrInterfaceType = node.getFirstChildOfType(ASTClassOrInterfaceType.class);
        if ((astClassOrInterfaceType == null) || (!(TypeHelper.isA(astClassOrInterfaceType, Throwable.class)))) {
            return super.visit(node, data);
        }

        ASTClassOrInterfaceDeclaration astClassOrInterfaceDeclaration = node.getFirstParentOfType(
            ASTClassOrInterfaceDeclaration.class);
        boolean isExceptionViolation = astClassOrInterfaceDeclaration != null
            && StringUtils.isNotEmpty(astClassOrInterfaceDeclaration.getImage())
            && !astClassOrInterfaceDeclaration.getImage().endsWith(EXCEPTION_END_SUFFIX);
        if (isExceptionViolation) {
            ViolationUtils.addViolationWithPrecisePosition(this, astClassOrInterfaceDeclaration, data,
                I18nResources.getMessage("java.naming.ExceptionClassShouldEndWithExceptionRule.violation.msg",
                    astClassOrInterfaceDeclaration.getImage()));
        }
        return super.visit(node, data);
    }

}
