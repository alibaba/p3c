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
package com.xenoamess.p3c.pmd.lang.java.rule.oop;

import com.xenoamess.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.xenoamess.p3c.pmd.lang.java.util.ViolationUtils;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTVariableInitializer;
import org.jaxen.JaxenException;

import java.util.List;

/**
 * [Mandatory] Avoid using the constructor BigDecimal(double) to convert double value to a BigDecimal object.
 *
 * @author zenghou.fw
 * @date 2019/04/02
 */
public class BigDecimalAvoidDoubleConstructorRule extends AbstractAliRule {

    private static final String XPATH =
            "Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments[preceding-sibling" +
                    "::ClassOrInterfaceType[@Image = 'BigDecimal']]"
                    + "/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix";

    private static final String STRING_DOUBLE = "Double";

    private static final String STRING_TO_HEX_STRING = "toHexString";

    private static final String STRING_TO_STRING = "toString";

    @Override
    public Object visit(ASTVariableInitializer node, Object data) {
        try {
            List<Node> invocations = node.findChildNodesWithXPath(XPATH);
            if (invocations == null || invocations.isEmpty()) {
                return super.visit(node, data);
            }
            ASTPrimaryPrefix expression = (ASTPrimaryPrefix) invocations.get(0);

            if (isDoubleLiteral(expression) || isDoubleVariable(expression)) {
                addViolationWithMessage(data, node,
                        "java.oop.BigDecimalAvoidDoubleConstructorRule.violation.msg", null);
            }

        } catch (JaxenException e) {
            throw new RuntimeException("XPath expression " + XPATH + " failed: " + e.getLocalizedMessage(), e);
        }
        return super.visit(node, data);
    }


    @Override
    public void addViolation(Object data, Node node, String arg) {
        ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                "java.oop.BigDecimalAvoidDoubleConstructorRule.violation.msg");
    }

    private boolean isDoubleLiteral(ASTPrimaryPrefix node) {
        ASTLiteral literal = node.getFirstChildOfType(ASTLiteral.class);
        return literal != null && literal.isDoubleLiteral();
    }

    private boolean isDoubleVariable(ASTPrimaryPrefix node) {
        ASTName name = node.getFirstChildOfType(ASTName.class);
        if (!(name != null && Double.class == name.getType())) {
            return false;
        }
        GenericToken firstToken = node.jjtGetFirstToken();
        GenericToken lastToken = node.jjtGetLastToken();

        return !(STRING_DOUBLE.equals(firstToken.getImage()) &&
                (
                        STRING_TO_HEX_STRING.equals(lastToken.getImage())
                                || STRING_TO_STRING.equals(lastToken.getImage())
                )
        );
    }
}
