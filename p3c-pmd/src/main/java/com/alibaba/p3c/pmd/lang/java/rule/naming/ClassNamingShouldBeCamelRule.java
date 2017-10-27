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

import java.util.List;
import java.util.regex.Pattern;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;

/**
 * [Mandatory] Class names should be nouns in UpperCamelCase except domain models: DO, BO, DTO, VO, etc.
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class ClassNamingShouldBeCamelRule extends AbstractAliRule {

    private static final Pattern PATTERN
        = Pattern.compile("^I?([A-Z][a-z0-9]+)+(([A-Z])|(DO|DTO|VO|DAO|BO|DAOImpl|YunOS|AO|PO))?$");

    private static final List<String> CLASS_NAMING_WHITE_LIST = NameListConfig.NAME_LIST_SERVICE.getNameList(
        ClassNamingShouldBeCamelRule.class.getSimpleName(), "CLASS_NAMING_WHITE_LIST");

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        for (String s : CLASS_NAMING_WHITE_LIST) {
            if (node.getImage().contains(s)) {
                return super.visit(node, data);
            }
        }
        if (PATTERN.matcher(node.getImage()).matches()) {
            return super.visit(node, data);
        }
        ViolationUtils.addViolationWithPrecisePosition(this, node, data,
            I18nResources.getMessage("java.naming.ClassNamingShouldBeCamelRule.violation.msg",
                node.getImage()));

        return super.visit(node, data);
    }
}
