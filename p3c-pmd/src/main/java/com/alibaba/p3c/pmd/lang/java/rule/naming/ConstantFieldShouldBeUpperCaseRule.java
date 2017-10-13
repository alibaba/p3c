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

import java.util.HashSet;
import java.util.Set;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import org.apache.commons.lang3.StringUtils;

/**
 * [Mandatory] Constant variable names should be written in upper characters separated by underscores. These names
 * should be semantically complete and clear.
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class ConstantFieldShouldBeUpperCaseRule extends AbstractAliRule {
    private static final String SERVICE_SUFFIX = "Service";
    private static final Set<String> LOG_VARIABLE_TYPE_SET = new HashSet<>(NameListConfig.NAME_LIST_SERVICE.getNameList(
        "ConstantFieldShouldBeUpperCaseRule", "LOG_VARIABLE_TYPE_SET"));
    private static final Set<String> WHITE_LIST = new HashSet<>(NameListConfig.NAME_LIST_SERVICE.getNameList(
        "ConstantFieldShouldBeUpperCaseRule", "WHITE_LIST"));

    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
        if (!(node.isStatic() && node.isFinal())) {
            return super.visit(node, data);
        }
        //If the variable is of type Log  or Logger,do not check
        ASTClassOrInterfaceType classOrInterfaceType = node.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
        if (classOrInterfaceType != null && LOG_VARIABLE_TYPE_SET.contains(classOrInterfaceType.getImage())) {
            return super.visit(node, data);
        }
        //filter by white list，such as the serialVersionUID
        String constantName = node.jjtGetChild(1).jjtGetChild(0).getImage();
        boolean inWhiteList = StringUtils.isEmpty(constantName) || WHITE_LIST.contains(constantName)
            || constantName.endsWith(SERVICE_SUFFIX);
        if (inWhiteList) {
            return super.visit(node, data);
        }
        //Constant should be upper
        if (!(constantName.equals(constantName.toUpperCase()))) {
            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                I18nResources.getMessage("java.naming.ConstantFieldShouldBeUpperCaseRule.violation.msg",
                    constantName));
        }
        return super.visit(node, data);
    }
}
