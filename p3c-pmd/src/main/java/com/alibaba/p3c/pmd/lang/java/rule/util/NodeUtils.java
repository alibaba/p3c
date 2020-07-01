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
package com.alibaba.p3c.pmd.lang.java.rule.util;

import java.util.concurrent.locks.Lock;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaAccessTypeNode;
import net.sourceforge.pmd.lang.java.ast.Token;
import net.sourceforge.pmd.lang.java.typeresolution.TypeHelper;

/**
 * @author caikang
 * @date 2016/11/16
 */
public class NodeUtils {
    public static final String LOCK_NAME = "lock";
    public static final String LOCK_INTERRUPTIBLY_NAME = "lockInterruptibly";
    public static final String UN_LOCK_NAME = "unlock";

    public static boolean isParentOrSelf(Node descendant, Node ancestor) {
        if (descendant == ancestor) {
            return true;
        }
        if (descendant == null || ancestor == null) {
            return false;
        }
        Node parent = descendant.jjtGetParent();
        while (parent != ancestor && parent != null) {
            parent = parent.jjtGetParent();
        }
        return parent == ancestor;
    }

    /**
     * TODO optimize
     *
     * @param expression expression
     * @return true if wrapper type
     */
    public static boolean isWrapperType(ASTPrimaryExpression expression) {
        return TypeHelper.isA(expression, Integer.class)
            || TypeHelper.isA(expression, Long.class)
            || TypeHelper.isA(expression, Boolean.class)
            || TypeHelper.isA(expression, Byte.class)
            || TypeHelper.isA(expression, Double.class)
            || TypeHelper.isA(expression, Short.class)
            || TypeHelper.isA(expression, Float.class)
            || TypeHelper.isA(expression, Character.class);
    }

    public static boolean isConstant(ASTFieldDeclaration field) {
        return field != null && field.isStatic() && field.isFinal();
    }

    public static Class<?> getNodeType(AbstractJavaAccessTypeNode node) {
        return node == null ? null : node.getType();
    }

    public static boolean isLockStatementExpression(ASTStatementExpression statementExpression) {
        return isLockTypeAndMethod(statementExpression, LOCK_NAME);
    }

    public static boolean isUnLockStatementExpression(ASTStatementExpression statementExpression) {
        return isLockTypeAndMethod(statementExpression, UN_LOCK_NAME);
    }

    private static boolean isLockTypeAndMethod(ASTStatementExpression statementExpression, String methodName) {
        ASTName name = statementExpression.getFirstDescendantOfType(ASTName.class);
        if (name == null || name.getType() == null || !Lock.class.isAssignableFrom(name.getType())) {
            return false;
        }
        Token token = (Token)name.jjtGetLastToken();
        return methodName.equals(token.image);
    }

    public static boolean isLockNode(Node node) {
        if (!(node instanceof ASTStatementExpression)) {
            return false;
        }
        ASTStatementExpression statementExpression = (ASTStatementExpression)node;
        return isLockStatementExpression(statementExpression);
    }
}
