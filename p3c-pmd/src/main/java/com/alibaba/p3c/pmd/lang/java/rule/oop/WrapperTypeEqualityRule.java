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

import java.util.List;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils;
import com.alibaba.p3c.pmd.lang.java.util.NumberConstants;

import net.sourceforge.pmd.lang.java.ast.ASTEqualityExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;

/**
 * [Mandatory] The wrapper classes should be compared by equals method rather than by symbol of '==' directly.
 *
 * @author zenghou.fw
 * @date 2016/11/22
 */
public class WrapperTypeEqualityRule extends AbstractAliRule {

    @Override
    public Object visit(ASTEqualityExpression node, Object data) {
        final String literalPrefix = "PrimaryExpression/PrimaryPrefix/Literal";
        final String unaryExpression = "UnaryExpression";
        // null presents in either side of "==" or "!=" means no violation
        if (node.hasDescendantMatchingXPath(literalPrefix)
            || node.hasDescendantMatchingXPath(unaryExpression)) {
            return super.visit(node, data);
        }

        // possible elements around "==" are PrimaryExpression or UnaryExpression(e.g. a == -2)
        List<ASTPrimaryExpression> expressions = node.findChildrenOfType(ASTPrimaryExpression.class);
        if (expressions.size() == NumberConstants.INTEGER_SIZE_OR_LENGTH_2) {
            // PMD can not resolve array length type, but only the
            ASTPrimaryExpression left = expressions.get(0);
            ASTPrimaryExpression right = expressions.get(1);
            // if left is complex expression, skip
            if (left.jjtGetNumChildren() > 1) {
                return super.visit(node, data);
            }
            boolean bothArrayLength = isArrayLength(left) && isArrayLength(right);
            boolean bothWrapperType = NodeUtils.isWrapperType(left) && NodeUtils.isWrapperType(right);

            if (!bothArrayLength && bothWrapperType) {
                addViolationWithMessage(data, node, "java.oop.WrapperTypeEqualityRule.violation.msg");
            }
        }

        return super.visit(node, data);
    }

    private boolean isArrayLength(ASTPrimaryExpression expression) {
        // assume expression like "x.length" is the length of array, field with name "length" may result in
        // misrecognition
        return "length".equals(expression.jjtGetLastToken().getImage())
            && ".".equals(expression.jjtGetFirstToken().getNext().getImage());
    }

}
