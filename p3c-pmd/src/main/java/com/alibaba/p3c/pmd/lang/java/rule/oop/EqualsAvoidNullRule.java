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
import com.alibaba.p3c.pmd.lang.java.util.StringAndCharConstants;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.ast.Token;
import org.jaxen.JaxenException;

/**
 * [Mandatory] Since NullPointerException can possibly be thrown while calling the equals method of Object,
 * equals should be invoked by a constant or an object that is definitely not null.
 *
 * @author zenghou.fw
 * @date 2016/11/29
 */
public class EqualsAvoidNullRule extends AbstractAliRule {

    private static final String XPATH = "//PrimaryExpression[" + "(PrimaryPrefix[Name[(ends-with(@Image, '.equals'))]]|"
        + "PrimarySuffix[@Image='equals'])"
        + "[(../PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix) and "
        + "( count(../PrimarySuffix/Arguments/ArgumentList/Expression) = 1 )]]"
        + "[not(ancestor::Expression/ConditionalAndExpression//EqualityExpression[@Image='!=']//NullLiteral)]"
        + "[not(ancestor::Expression/ConditionalOrExpression//EqualityExpression[@Image='==']//NullLiteral)]";

    private static final String INVOCATION_PREFIX_XPATH
        = "PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression[not(PrimarySuffix)]/PrimaryPrefix";

    private static final String METHOD_EQUALS = "equals";

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        try {
            List<Node> equalsInvocations = node.findChildNodesWithXPath(XPATH);
            if (equalsInvocations == null || equalsInvocations.isEmpty()) {
                return super.visit(node, data);
            }
            for (Node invocation : equalsInvocations) {
                // https://github.com/alibaba/p3c/issues/471
                if (callerIsLiteral(invocation)) {
                    return super.visit(node, data);
                }

                // if arguments of equals is complicate expression, skip the check
                List<? extends Node> simpleExpressions = invocation.findChildNodesWithXPath(INVOCATION_PREFIX_XPATH);
                if (simpleExpressions == null || simpleExpressions.isEmpty()) {
                    return super.visit(node, data);
                }

                ASTPrimaryPrefix right = (ASTPrimaryPrefix)simpleExpressions.get(0);
                if (right.getFirstChildOfType(ASTLiteral.class) != null) {
                    ASTLiteral literal = right.getFirstChildOfType(ASTLiteral.class);
                    if (literal.isStringLiteral()) {
                        // other literals has no equals method, can not be flipped
                        addRuleViolation(data, invocation);
                    }
                } else {
                    ASTName name = right.getFirstChildOfType(ASTName.class);
                    // TODO works only in current compilation file, by crossing files will be null
                    boolean nameInvalid = name == null || name.getNameDeclaration() == null
                        || name.getNameDeclaration().getNode() == null;
                    if (nameInvalid) {
                        return super.visit(node, data);
                    }
                    Node nameNode = name.getNameDeclaration().getNode();
                    if ((nameNode instanceof ASTVariableDeclaratorId) && (nameNode.getNthParent(
                        2) instanceof ASTFieldDeclaration)) {
                        ASTFieldDeclaration field = (ASTFieldDeclaration)nameNode.getNthParent(2);
                        if (NodeUtils.isConstant(field)) {
                            addRuleViolation(data, invocation);
                        }
                    }
                }
            }
        } catch (JaxenException e) {
            throw new RuntimeException("XPath expression " + XPATH + " failed: " + e.getLocalizedMessage(), e);
        }
        return super.visit(node, data);
    }

    private boolean callerIsLiteral(Node equalsInvocation) {
        if (equalsInvocation instanceof ASTPrimaryExpression) {
            ASTPrimaryPrefix caller = equalsInvocation.getFirstChildOfType(ASTPrimaryPrefix.class);
            return caller != null && caller.getFirstChildOfType(ASTLiteral.class) != null;
        }
        return false;
    }

    private String getInvocationName(AbstractJavaNode javaNode) {
        Token token = (Token)javaNode.jjtGetFirstToken();
        StringBuilder sb = new StringBuilder(token.image).append(token.image);
        while (token.next != null && token.next.image != null && !METHOD_EQUALS.equals(token.next.image)) {
            token = token.next;
            sb.append(token.image);
        }
        if (sb.charAt(sb.length() - 1) == StringAndCharConstants.DOT) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private void addRuleViolation(Object data, Node invocation) {
        if (invocation instanceof AbstractJavaNode) {
            AbstractJavaNode javaNode = (AbstractJavaNode)invocation;
            addViolationWithMessage(data, invocation, "java.oop.EqualsAvoidNullRule.violation.msg",
                new Object[] {getInvocationName(javaNode)});
        } else {
            addViolation(data, invocation);
        }
    }
}
