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
package com.alibaba.p3c.pmd.lang.java.rule.flowcontrol;

import com.alibaba.p3c.pmd.lang.AbstractXpathRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;

/**
 * [Recommended] Avoid using the negation operator '!'.
 * Note: The negation operator is not easy to be quickly understood. There must be a positive
 * way to represent the same logic.
 *
 * @author zenghou.fw
 * @date 2017/11/21
 */
public class AvoidNegationOperatorRule extends AbstractXpathRule {
    private static final String XPATH = "//UnaryExpressionNotPlusMinus[child::PrimaryExpression"
        + "//PrimaryPrefix/Expression/RelationalExpression]"
        + "|//UnaryExpressionNotPlusMinus[child::PrimaryExpression"
        + "//PrimaryPrefix/Expression/EqualityExpression]";

    public AvoidNegationOperatorRule() {
        setXPath(XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        ViolationUtils.addViolationWithPrecisePosition(this, node, data,
            "java.flowcontrol.AvoidNegationOperatorRule.violation.msg");
    }
}
