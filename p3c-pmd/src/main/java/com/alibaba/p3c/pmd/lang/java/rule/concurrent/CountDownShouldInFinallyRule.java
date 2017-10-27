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
package com.alibaba.p3c.pmd.lang.java.rule.concurrent;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTTryStatement;
import org.jaxen.JaxenException;

/**
 * [Recommended] When using CountDownLatch to convert asynchronous operations to synchronous ones,
 * each thread must call countdown method before quitting.
 * Make sure to catch any exception during thread running, to let countdown method be executed.
 * If main thread cannot reach await method, program will return until timeout.
 *
 * Note: Be careful, exception thrown by sub-thread cannot be caught by main thread.
 *
 * @author caikang
 * @date 2017/03/29
 */
public class CountDownShouldInFinallyRule extends AbstractAliRule {
    private static final String XPATH = "./Block/BlockStatement/Statement/StatementExpression"
        + "/PrimaryExpression/PrimaryPrefix/Name[ends-with(@Image,'.countDown')]";

    @Override
    public Object visit(ASTTryStatement node, Object data) {
        try {
            List<Node> nodes = node.findChildNodesWithXPath(XPATH);
            if (nodes == null || nodes.isEmpty()) {
                return super.visit(node, data);
            }
            for (Node nameNode : nodes) {
                if (!(nameNode instanceof ASTName)) {
                    continue;
                }
                ASTName name = (ASTName)nameNode;
                if (name.getType() != CountDownLatch.class) {
                    continue;
                }
                addViolationWithMessage(data, name, "java.concurrent.CountDownShouldInFinallyRule.violation.msg",
                    new Object[] {name.getImage()});
            }
        } catch (JaxenException ignore) {
        }
        return super.visit(node, data);
    }
}
