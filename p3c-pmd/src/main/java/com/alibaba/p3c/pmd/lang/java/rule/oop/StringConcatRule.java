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

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.NumberConstants;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAdditiveExpression;
import net.sourceforge.pmd.lang.java.ast.ASTDoStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.jaxen.JaxenException;

/**
 * [Recommended] Use the append method in StringBuilder inside a loop body when concatenating multiple strings.
 *
 * @author zenghou.fw
 * @date 2017/04/11
 */
public class StringConcatRule extends AbstractAliRule {

    private static final String XPATH =
        "Statement/Block//Expression[preceding-sibling::AssignmentOperator]/AdditiveExpression[(@Image = '+') and "
            + "count(./PrimaryExpression/PrimaryPrefix/Literal[@StringLiteral = 'true']) > 0]";

    @Override
    public Object visit(ASTForStatement node, Object data) {
        checkStringConcat(node, data, ASTForStatement.class);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTWhileStatement node, Object data) {
        checkStringConcat(node, data, ASTWhileStatement.class);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTDoStatement node, Object data) {
        checkStringConcat(node, data, ASTDoStatement.class);
        return super.visit(node, data);
    }

    /**
     * Find additive assignment with string literal, then check if the assigned variable defined out of the loop,
     *
     * @param node
     * @param data
     * @param nodeClass
     */
    private void checkStringConcat(Node node, Object data, Class nodeClass) {
        try {
            List<? extends Node> additiveNodes = node.findChildNodesWithXPath(XPATH);
            for (Node additiveNode : additiveNodes) {
                ASTAdditiveExpression additiveExpression = (ASTAdditiveExpression)additiveNode;
                Node assignmentStatement = additiveExpression.getNthParent(2);
                if (!(assignmentStatement instanceof ASTStatementExpression)) {
                    continue;
                }
                List<Node> nodes = ((ASTStatementExpression)assignmentStatement)
                    .findChildNodesWithXPath("PrimaryExpression/PrimaryPrefix/Name[@Image]");
                if (nodes == null || nodes.size() != NumberConstants.INTEGER_SIZE_OR_LENGTH_1) {
                    continue;
                }
                NameDeclaration resultVar = ((ASTName)nodes.get(0)).getNameDeclaration();
                if (resultVar != null && resultVar.getNode() != null) {
                    boolean isDefinedInLoop = false;

                    AbstractJavaNode loopStatement = (AbstractJavaNode)resultVar.getNode().getFirstParentOfType(
                        nodeClass);

                    while (loopStatement != null) {
                        if (loopStatement == node) {
                            isDefinedInLoop = true;
                            break;
                        }
                        loopStatement = (AbstractJavaNode)loopStatement.getFirstParentOfType(nodeClass);
                    }

                    // if assigned variable defined in the loop then break
                    if (isDefinedInLoop) {
                        return;
                    }
                }
                // arguments joint by "+"
                for (int i = 0; i < additiveNode.jjtGetNumChildren(); i++) {
                    Node firstArg = additiveNode.jjtGetChild(i);
                    if (!(firstArg instanceof ASTPrimaryExpression)) {
                        continue;
                    }
                    List<Node> names = ((ASTPrimaryExpression)firstArg).
                        findChildNodesWithXPath("./PrimaryPrefix/Name[@Image]");
                    if (names == null || names.size() != NumberConstants.INTEGER_SIZE_OR_LENGTH_1) {
                        continue;
                    }
                    NameDeclaration firstArgVar = ((ASTName)names.get(0)).getNameDeclaration();

                    // concat self, e.g. a = a + b;
                    if (resultVar == firstArgVar) {
                        addViolation(data, additiveNode);
                        break;
                    }
                }
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        ViolationUtils.addViolationWithPrecisePosition(this, node, data,
            I18nResources.getMessage("java.oop.PojoMustOverrideToStringRule.violation.msg"));
    }
}
