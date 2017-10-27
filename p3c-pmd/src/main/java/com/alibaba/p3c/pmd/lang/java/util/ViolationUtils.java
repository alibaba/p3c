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
package com.alibaba.p3c.pmd.lang.java.util;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.rule.AbstractRule;

/**
 * @author caikang
 * @date 2017/01/14
 */
public class ViolationUtils {
    public static void addViolationWithPrecisePosition(AbstractRule rule, Node node, Object data) {
        addViolationWithPrecisePosition(rule, node, data, null);
    }

    public static void addViolationWithPrecisePosition(AbstractRule rule, Node node, Object data,
        String message) {
        if (node instanceof ASTFieldDeclaration) {
            ASTVariableDeclaratorId variableDeclaratorId = node.getFirstDescendantOfType(ASTVariableDeclaratorId.class);
            addViolation(rule, variableDeclaratorId, data, message);
            return;
        }
        if (node instanceof ASTMethodDeclaration) {
            ASTMethodDeclarator declarator = node.getFirstChildOfType(ASTMethodDeclarator.class);
            addViolation(rule, declarator, data, message);
            return;
        }
        addViolation(rule, node, data, message);
    }

    private static void addViolation(AbstractRule rule, Node node, Object data, String message) {
        if (message == null) {
            rule.addViolation(data, node);
        } else {
            rule.addViolationWithMessage(data, node, message);
        }
    }
}
