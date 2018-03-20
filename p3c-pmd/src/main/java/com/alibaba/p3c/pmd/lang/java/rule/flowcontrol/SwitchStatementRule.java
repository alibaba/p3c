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

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.java.ast.ASTSwitchStatement;

/**
 * [Mandatory] In a switch block, each case should be finished by break/return.
 * If not, a note should be included to describe at which case it will stop. Within every switch block,
 * a default statement must be present, even if it is empty.
 *
 * @author zenghou.fw
 * @date 2016/11/17
 */
public class SwitchStatementRule extends AbstractAliRule {
    private static final String MESSAGE_KEY_PREFIX = "java.flowcontrol.SwitchStatementRule.violation";

    @Override
    public Object visit(ASTSwitchStatement node, Object data) {
        checkDefault(node, data);

        checkFallThrough(node, data);

        return super.visit(node, data);
    }

    /**
     * Check if switch statement contains default branch
     *
     * @param node
     * @param data
     */
    private void checkDefault(ASTSwitchStatement node, Object data) {
        final String switchCheckXpath = "SwitchLabel[@Default = 'true']";
        if (!node.hasDescendantMatchingXPath(switchCheckXpath)) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".nodefault");
        }
    }

    /**
     * Check the availability of break, return, throw, continue in case statement
     *
     * @param node
     * @param data
     */
    private void checkFallThrough(ASTSwitchStatement node, Object data) {
        // refer the rule MissingBreakInSwitch of PMD
        final String xpath = "../SwitchStatement[(count(.//BreakStatement)"
            + " + count(BlockStatement//Statement/ReturnStatement)"
            + " + count(BlockStatement//Statement/ContinueStatement)"
            + " + count(BlockStatement//Statement/ThrowStatement)"
            + " + count(BlockStatement//Statement/IfStatement[@Else='true'"
            + " and Statement[2][ReturnStatement|ContinueStatement|ThrowStatement]]"
            + "/Statement[1][ReturnStatement|ContinueStatement|ThrowStatement])"
            + " + count(SwitchLabel[name(following-sibling::node()) = 'SwitchLabel'])"
            + " + count(SwitchLabel[count(following-sibling::node()) = 0])"
            + "  < count (SwitchLabel[@Default != 'true'])"
            + " + count(SwitchLabel[@Default = 'true']/following-sibling::BlockStatement//Statement/ReturnStatement)"
            + " + count(SwitchLabel[@Default = 'true']/following-sibling::BlockStatement//Statement/ContinueStatement)"
            + " + count(SwitchLabel[@Default = 'true']/following-sibling::BlockStatement//Statement/ThrowStatement)"
            + ")]";

        if (node.hasDescendantMatchingXPath(xpath)) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".notermination");
        }
    }
}
