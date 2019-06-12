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

import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;

/**
 * [Mandatory] Braces are used with if, else, for, do and while statements, even if the body contains only a single
 * statement. Avoid using the following example:
 * <pre>
 * if (condition) statements;
 * </pre>
 *
 * @author zenghou.fw
 * @date 2016/11/22
 */
public class NeedBraceRule extends AbstractAliRule {
    private static final String STATEMENT_BLOCK = "Statement/Block";

    private static final String MESSAGE_KEY = "java.flowcontrol.NeedBraceRule.violation.msg";

    @Override
    public Object visit(ASTIfStatement node, Object data) {
        // SwitchStatement without {} fail by compilaton, no need to check here
        if (!node.hasDescendantMatchingXPath(STATEMENT_BLOCK)) {
            addViolationWithMessage(data, node, MESSAGE_KEY,
                new Object[] {node.jjtGetFirstToken().toString()});
        }
        if (node.hasElse()) {
            // IfStatement with else have 2 expression blocks, should never throws NPE
            ASTStatement elseStms = node.findChildrenOfType(ASTStatement.class).get(1);

            if (!elseStms.hasDescendantOfAnyType(ASTBlock.class, ASTIfStatement.class)) {
                addViolationWithMessage(data, elseStms, MESSAGE_KEY, new Object[] {"else"});
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTForStatement node, Object data) {
        if (!node.hasDescendantMatchingXPath(STATEMENT_BLOCK)) {
            addViolationWithMessage(data, node, MESSAGE_KEY, new Object[] {"for"});
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTWhileStatement node, Object data) {
        if (!node.hasDescendantMatchingXPath(STATEMENT_BLOCK)) {
            addViolationWithMessage(data, node, MESSAGE_KEY, new Object[] {"while"});
        }
        return super.visit(node, data);
    }

}
