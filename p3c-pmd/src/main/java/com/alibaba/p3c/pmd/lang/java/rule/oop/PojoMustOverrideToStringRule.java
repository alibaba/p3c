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
package com.alibaba.p3c.pmd.lang.java.rule.oop;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractPojoRule;
import com.alibaba.p3c.pmd.lang.java.util.PojoUtils;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTExtendsList;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import org.jaxen.JaxenException;

/**
 * [Mandatory] The toString method must be implemented in a POJO class. The super.toString method should be called
 * in front of the whole implementation if the current class extends another POJO class.
 *
 * @author zenghou.fw
 * @date 2016/11/25
 */
public class PojoMustOverrideToStringRule extends AbstractPojoRule {

    private static final String XPATH = "ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration"
        + "[@Public='true' and MethodDeclarator[@Image='toString'] and "
        + "MethodDeclarator[@Image='toString' and @ParameterCount='0']]";

    private static final String TOSTRING_XPATH = "//PrimaryExpression[PrimaryPrefix[Name"
        + "[(ends-with(@Image, '.toString'))]]["
        + "(../PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal[@StringLiteral"
        + "='true'])" + " and ( count(../PrimarySuffix/Arguments/ArgumentList/Expression) = 1 )]]"
        + "[not(ancestor::Expression/ConditionalAndExpression//EqualityExpression[@Image='!=']//NullLiteral)]"
        + "[not(ancestor::Expression/ConditionalOrExpression//EqualityExpression[@Image='==']//NullLiteral)]";

    private static final String LOMBOK_NAME_XPATH = "/Name["
        + "(@Image='Data' and //ImportDeclaration[@ImportedName='lombok.Data' or @ImportedName='lombok'])"
        + " or (@Image='ToString' and //ImportDeclaration[@ImportedName='lombok.ToString' or @ImportedName='lombok'])"
        + " or (@Image='lombok.Data') or (@Image='lombok.ToString')]";

    private static final String LOMBOK_XPATH = "../Annotation/MarkerAnnotation" + LOMBOK_NAME_XPATH
        + "|../Annotation/NormalAnnotation" + LOMBOK_NAME_XPATH;

    private static final String MESSAGE_KEY_PREFIX = "java.oop.PojoMustOverrideToStringRule.violation.msg";

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (node.isInterface()) {
            return super.visit(node, data);
        }

        if (!isPojo(node)) {
            return super.visit(node, data);
        }
        if (node.isAbstract() || withLombokAnnotation(node)) {
            return super.visit(node, data);
        }

        if (!node.hasDescendantMatchingXPath(XPATH)) {
            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".notostring", node.getImage()));
        } else {
            checkForExtend(node, data);
        }
        return super.visit(node, data);
    }

    private void checkForExtend(ASTClassOrInterfaceDeclaration node, Object data) {
    /*
     * The super.toString method should be called in front of the whole implementation
     * if the current class extends another POJO class
     * -> this part not checked
     */
        ASTExtendsList extendsList = node.getFirstChildOfType(ASTExtendsList.class);
        if (extendsList == null) {
            return;
        }
        String baseName = extendsList.getFirstChildOfType(ASTClassOrInterfaceType.class).getImage();
        if (!PojoUtils.isPojo(baseName)) {
            return;
        }
        try {
            // toString() definition
            ASTMethodDeclaration toStringMethod = (ASTMethodDeclaration)node.findChildNodesWithXPath(XPATH).get(0);
            ASTBlock block = toStringMethod.getBlock();
            if (block.hasDescendantMatchingXPath(TOSTRING_XPATH)) {
                addViolationWithMessage(data, block, MESSAGE_KEY_PREFIX + ".usesuper");
            }
        } catch (JaxenException e) {
            throw new RuntimeException("XPath expression " + XPATH + " failed: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Class with lombok @Data will be skipped
     */
    private boolean withLombokAnnotation(ASTClassOrInterfaceDeclaration node) {
        return node.hasDescendantMatchingXPath(LOMBOK_XPATH);
    }
}
