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

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.dfa.DataFlowNode;
import net.sourceforge.pmd.lang.dfa.StartOrEndDataFlowNode;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTSynchronizedStatement;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.ast.Token;

import static com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils.isLockNode;
import static com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils.isLockStatementExpression;
import static com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils.isUnLockStatementExpression;

/**
 * [Mandatory] SimpleDataFormat is unsafe, do not define it as a static variable.
 * If have to, lock or DateUtils class must be used.
 *
 * @author caikang
 * @date 2016/11/25
 */
public class AvoidCallStaticSimpleDateFormatRule extends AbstractAliRule {
    private static final String FORMAT_METHOD_NAME = "format";

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        if (node.isSynchronized()) {
            return super.visit(node, data);
        }

        handleMethod(node, data);
        return super.visit(node, data);
    }

    private void handleMethod(ASTMethodDeclaration methodDeclaration, Object data) {
        DataFlowNode dataFlowNode = methodDeclaration.getDataFlowNode();
        if (dataFlowNode == null || dataFlowNode.getFlow() == null) {
            return;
        }
        // records of violations,lock block excepted
        Stack<Node> stack = new Stack<>();
        Set<String> localSimpleDateFormatNames = new HashSet<>();
        for (DataFlowNode flowNode : dataFlowNode.getFlow()) {
            handleFlowNode(stack, localSimpleDateFormatNames, flowNode);
        }
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node instanceof ASTPrimaryExpression) {
                addViolationWithMessage(data, node,
                    "java.concurrent.AvoidCallStaticSimpleDateFormatRule.violation.msg",
                    new Object[] {getExpressName((ASTPrimaryExpression)node)});
            }
        }
    }

    private void handleFlowNode(Stack<Node> stack, Set<String> localSimpleDateFormatNames, DataFlowNode flowNode) {
        if (flowNode instanceof StartOrEndDataFlowNode || flowNode.getNode() instanceof ASTMethodDeclaration) {
            return;
        }
        // collect local variables of type SimpleDateFormat if match,then return
        if (flowNode.getNode() instanceof ASTVariableDeclarator) {
            ASTVariableDeclarator variableDeclarator = (ASTVariableDeclarator)flowNode.getNode();
            if (variableDeclarator.getType() == SimpleDateFormat.class) {
                ASTVariableDeclaratorId variableDeclaratorId =
                    variableDeclarator.getFirstChildOfType(ASTVariableDeclaratorId.class);
                localSimpleDateFormatNames.add(variableDeclaratorId.getImage());
                return;
            }
        }

        if (flowNode.getNode() instanceof ASTStatementExpression) {
            ASTStatementExpression statementExpression = (ASTStatementExpression)flowNode.getNode();
            if (isLockStatementExpression(statementExpression)) {
                // add lock node
                stack.push(flowNode.getNode());
                return;
            }

            if (isUnLockStatementExpression(statementExpression)) {
                // remove element in lock block
                while (!stack.isEmpty()) {
                    Node node = stack.pop();
                    if (isLockNode(node)) {
                        break;
                    }
                }
                return;
            }
        }
        AbstractJavaNode javaNode = (AbstractJavaNode)flowNode.getNode();
        ASTPrimaryExpression flowPrimaryExpression = javaNode.getFirstDescendantOfType(ASTPrimaryExpression.class);
        if (flowPrimaryExpression == null) {
            return;
        }
        if (flowPrimaryExpression.getFirstParentOfType(ASTSynchronizedStatement.class) != null) {
            return;
        }
        if (!isStaticSimpleDateFormatCall(flowPrimaryExpression, localSimpleDateFormatNames)) {
            return;
        }
        // add violation element (include those in lock block,until we meet unlock block,we can remove them)
        stack.push(flowPrimaryExpression);
    }

    private String getExpressName(ASTPrimaryExpression primaryExpression) {
        ASTName name = primaryExpression.getFirstDescendantOfType(ASTName.class);
        return name.getImage();
    }

    private boolean isStaticSimpleDateFormatCall(
        ASTPrimaryExpression primaryExpression,
        Set<String> localSimpleDateFormatNames
    ) {
        if (primaryExpression.jjtGetNumChildren() == 0) {
            return false;
        }
        ASTName name = primaryExpression.getFirstDescendantOfType(ASTName.class);
        if (name == null || name.getType() != SimpleDateFormat.class) {
            return false;
        }
        if (name.getNameDeclaration() == null || name.getNameDeclaration().getName() == null) {
            return false;
        }
        if (localSimpleDateFormatNames.contains(name.getNameDeclaration().getName())) {
            return false;
        }
        ASTPrimaryPrefix primaryPrefix = (ASTPrimaryPrefix)primaryExpression.jjtGetChild(0);
        if (primaryPrefix.getType() != SimpleDateFormat.class) {
            return false;
        }

        Token token = (Token)primaryPrefix.jjtGetLastToken();
        return FORMAT_METHOD_NAME.equals(token.image);
    }

}
