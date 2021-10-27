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
package com.xenoamess.p3c.pmd.lang.java.rule.naming;

import com.xenoamess.p3c.pmd.I18nResources;
import com.xenoamess.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.xenoamess.p3c.pmd.lang.java.util.StringAndCharConstants;
import com.xenoamess.p3c.pmd.lang.java.util.ViolationUtils;
import com.xenoamess.p3c.pmd.lang.java.util.namelist.NameListConfig;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotationTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;

import java.util.List;
import java.util.regex.Pattern;

/**
 * [Mandatory] Method names, parameter names, member variable names, and local variable names should be written in
 * lowerCamelCase.
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class LowerCamelCaseVariableNamingRule extends AbstractAliRule {

    private static final String MESSAGE_KEY_PREFIX = "java.naming.LowerCamelCaseVariableNamingRule.violation.msg";
    private final Pattern pattern = Pattern.compile("^[a-z][a-z0-9]*([A-Z][a-z0-9]+)*" +
            "(DO|DTO|VO|DAO|BO|DOList|DTOList|VOList|DAOList|BOList|X|Y|Z|UDF|UDAF|[A-Z])?$");
    private final String STRING_CLASS_NAME_OVERRIDE = "java.lang.Override";

    private static List<String> getWhiteList() {
        return NameListConfig.getNameListService().getNameList(
                LowerCamelCaseVariableNamingRule.class.getSimpleName(),
                "WHITE_LIST"
        );
    }

    @Override
    public Object visit(final ASTVariableDeclaratorId node, Object data) {
        for (String s : getWhiteList()) {
            if (node.getImage().contains(s)) {
                return super.visit(node, data);
            }
        }

        //避免与 AvoidStartWithDollarAndUnderLineNamingRule 重复判断(例: $myTest)
        if (variableNamingStartOrEndWithDollarAndUnderLine(node.getImage())) {
            return super.visit(node, data);
        }
        // Constant named does not apply to this rule
        ASTTypeDeclaration typeDeclaration = node.getFirstParentOfType(ASTTypeDeclaration.class);
        Node jjtGetChild = typeDeclaration.getChild(0);
        if (jjtGetChild instanceof ASTAnnotationTypeDeclaration) {
            return super.visit(node, data);
        }

        ASTFieldDeclaration astFieldDeclaration = node.getFirstParentOfType(ASTFieldDeclaration.class);
        boolean isNotCheck = astFieldDeclaration != null && (astFieldDeclaration.isFinal() || astFieldDeclaration
                .isStatic());
        if (isNotCheck) {
            return super.visit(node, data);
        }

        // variable naming violate lowerCamelCase
        if (!(pattern.matcher(node.getImage()).matches())) {
            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                    I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".variable", node.getImage()));
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTMethodDeclarator node, Object data) {
        for (String s : getWhiteList()) {
            if (node.getImage().contains(s)) {
                return super.visit(node, data);
            }
        }

        {
            // add logic: DO NOT do this analyse to all function with @Override annotation on
            // reason: If we can change its parent class
            // (means the parent class also follows p3c, notice that not all parent classes be in a same project,
            // it can be from a lib sometimes.), we just alarm there.
            //
            // Otherwise, we can do nothing to this, as it is just an override, and can NOT do the rename.
            //
            // In each situation, alarm in the child class is meaningless.
            if (node.getParent().isAnnotationPresent(STRING_CLASS_NAME_OVERRIDE)) {
                return super.visit(node, data);
            }
        }

        if (!variableNamingStartOrEndWithDollarAndUnderLine(node.getImage())) {
            if (!(pattern.matcher(node.getImage()).matches())) {
                ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                        I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".method", node.getImage()));
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTAnnotationTypeDeclaration node, Object data) {
        //对所有注解内的内容不做检查
        return null;
    }

    private boolean variableNamingStartOrEndWithDollarAndUnderLine(String variable) {
        return variable.startsWith(StringAndCharConstants.DOLLAR)
                || variable.startsWith(StringAndCharConstants.UNDERSCORE);
    }
}
