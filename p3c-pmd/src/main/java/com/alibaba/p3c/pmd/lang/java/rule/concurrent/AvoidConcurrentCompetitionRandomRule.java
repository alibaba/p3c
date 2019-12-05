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
import java.util.Random;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils;
import com.alibaba.p3c.pmd.lang.java.util.VariableUtils;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTExtendsList;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import org.jaxen.JaxenException;

/**
 * 6.13 [Recommended] Avoid using Random instance by multiple threads.
 * Although it is safe to share this instance, competition on the same seed will damage performance.
 * Note: Random instance includes instances of java.util.Random and Math.random().
 *
 * @author caikang
 * @date 2017/03/29
 */
public class AvoidConcurrentCompetitionRandomRule extends AbstractAliRule {

    private static final String XPATH_TPL = "//StatementExpression/PrimaryExpression"
        + "/PrimaryPrefix/Name[starts-with(@Image,'%s.')]";

    private static final String MATH_RANDOM_METHOD = ".random";

    private static final String MESSAGE_KEY_PREFIX = "java.concurrent.AvoidConcurrentCompetitionRandomRule";

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        ASTExtendsList extendsList = node.getFirstChildOfType(ASTExtendsList.class);
        if (extendsList == null) {
            return super.visit(node, data);
        }
        if (!hasThread(extendsList)) {
            return super.visit(node, data);
        }
        List<ASTMethodDeclaration> methodDeclarations = node.findDescendantsOfType(ASTMethodDeclaration.class);
        if (methodDeclarations == null || methodDeclarations.isEmpty()) {
            return super.visit(node, data);
        }
        checkMathRandom(methodDeclarations, data);

        List<ASTFieldDeclaration> fieldDeclarations = node.findDescendantsOfType(ASTFieldDeclaration.class);
        if (fieldDeclarations == null || fieldDeclarations.isEmpty()) {
            return super.visit(node, data);
        }
        for (ASTFieldDeclaration fieldDeclaration : fieldDeclarations) {
            if (NodeUtils.getNodeType(fieldDeclaration) == Random.class
                && fieldDeclaration.isStatic()) {
                checkRandom(fieldDeclaration, methodDeclarations, data);
            }
        }
        return super.visit(node, data);
    }

    private void checkMathRandom(List<ASTMethodDeclaration> methodDeclarations, Object data) {
        for (ASTMethodDeclaration methodDeclaration : methodDeclarations) {
            List<ASTPrimaryPrefix> primaryPrefixes
                = methodDeclaration.findDescendantsOfType(ASTPrimaryPrefix.class);
            if (primaryPrefixes == null || primaryPrefixes.isEmpty()) {
                continue;
            }
            for (ASTPrimaryPrefix primaryPrefix : primaryPrefixes) {
                if (primaryPrefix.getType() != Math.class) {
                    continue;
                }
                ASTName name = primaryPrefix.getFirstChildOfType(ASTName.class);
                if (name == null || name.getImage() == null || !name.getImage().endsWith(MATH_RANDOM_METHOD)) {
                    continue;
                }
                addViolationWithMessage(data, primaryPrefix,
                    MESSAGE_KEY_PREFIX + ".violation.msg.math.random");
            }
        }
    }

    private void checkRandom(ASTFieldDeclaration fieldDeclaration, List<ASTMethodDeclaration> methodDeclarations,
        Object data) {
        for (ASTMethodDeclaration methodDeclaration : methodDeclarations) {
            try {
                List<Node> nodes = methodDeclaration.findChildNodesWithXPath(String.format(XPATH_TPL,
                    VariableUtils.getVariableName(fieldDeclaration)));
                if (nodes == null || nodes.isEmpty()) {
                    continue;
                }
                for (Node rvNode : nodes) {
                    addViolationWithMessage(data, rvNode,
                        MESSAGE_KEY_PREFIX + ".violation.msg.random",
                        new Object[] {rvNode.getImage()});
                }
            } catch (JaxenException ignore) {
            }
        }
    }

    private boolean hasThread(ASTExtendsList extendsList) {
        List<ASTClassOrInterfaceType> typeList = extendsList.findChildrenOfType(ASTClassOrInterfaceType.class);
        if (typeList == null || typeList.isEmpty()) {
            return false;
        }
        for (ASTClassOrInterfaceType type : typeList) {
            if (type.getType() == Thread.class) {
                return true;
            }
        }
        return false;
    }
}
