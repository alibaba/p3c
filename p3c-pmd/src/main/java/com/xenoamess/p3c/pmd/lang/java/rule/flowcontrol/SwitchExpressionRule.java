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
package com.xenoamess.p3c.pmd.lang.java.rule.flowcontrol;

import com.xenoamess.p3c.pmd.lang.java.rule.AbstractAliRule;
import net.sourceforge.pmd.lang.java.ast.ASTSwitchExpression;

/**
 * [Mandatory] In a switch expression, each case should be finished by break/return.
 * If not, a note should be included to describe at which case it will stop. Within every switch block,
 * a default statement must be present, even if it is empty.
 *
 * @author XenoAmess
 * @date 2020/4/22
 */
public class SwitchExpressionRule extends AbstractAliRule {
    private static final String MESSAGE_KEY_PREFIX = "java.flowcontrol.SwitchExpressionRule.violation";

    @Override
    public Object visit(ASTSwitchExpression node, Object data) {
        checkDefault(node, data);
        return super.visit(node, data);
    }

    /**
     * Check if switch statement contains default branch
     *
     * @param node node
     * @param data ruleContext
     */
    private void checkDefault(ASTSwitchExpression node, Object data) {
        final String switchLabelCheckXpath = "SwitchLabel[@Default = 'true']";
        final String switchLabeledExpressionCheckXpath = "SwitchLabeledExpression[SwitchLabel[@Default = 'true']]";
        final String switchLabeledBlockCheckXpath = "SwitchLabeledBlock[SwitchLabel[@Default = 'true']]";
        if (
                !node.hasDescendantMatchingXPath(switchLabelCheckXpath)
                        && !node.hasDescendantMatchingXPath(switchLabeledExpressionCheckXpath)
                        && !node.hasDescendantMatchingXPath(switchLabeledBlockCheckXpath)
        ) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".nodefault");
        }
    }
}
