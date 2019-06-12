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
package com.alibaba.p3c.pmd.lang.java.rule.exception;

import java.util.List;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMemberValuePair;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import org.jaxen.JaxenException;

/**
 * [Mandatory] Make sure to invoke the rollback if a method throws an Exception.
 *
 * @author caikang
 * @date 2017/03/29
 */
public class TransactionMustHaveRollbackRule extends AbstractAliRule {
    private static final String TRANSACTIONAL_ANNOTATION_NAME = "Transactional";
    private static final String TRANSACTIONAL_FULL_NAME = "org.springframework.transaction.annotation."
        + TRANSACTIONAL_ANNOTATION_NAME;
    private static final String ROLLBACK_PREFIX = "rollback";

    private static final String READ_ONLY = "readOnly";

    private static final String PROPAGATION_NOT_SUPPORTED = "Propagation.NOT_SUPPORTED";

    private static final String XPATH_FOR_ROLLBACK = "//StatementExpression/PrimaryExpression"
        + "/PrimaryPrefix/Name[ends-with(@Image,'rollback')]";

    private static final String MESSAGE_KEY_PREFIX = "java.exception.TransactionMustHaveRollbackRule.violation.msg";

    @Override
    public Object visit(ASTAnnotation node, Object data) {
        ASTName name = node.getFirstDescendantOfType(ASTName.class);
        boolean noTransactional = name == null || !(TRANSACTIONAL_ANNOTATION_NAME.equals(name.getImage())
            && !TRANSACTIONAL_FULL_NAME.equals(name.getImage()));
        if (noTransactional) {
            return super.visit(node, data);
        }
        List<ASTMemberValuePair> memberValuePairList = node.findDescendantsOfType(ASTMemberValuePair.class);
        if (shouldSkip(memberValuePairList)) {
            return super.visit(node, data);
        }

        ASTClassOrInterfaceDeclaration classOrInterfaceDeclaration
            = getSiblingForType(node, ASTClassOrInterfaceDeclaration.class);
        if (classOrInterfaceDeclaration != null) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".simple");
            return super.visit(node, data);
        }

        ASTMethodDeclaration methodDeclaration = getSiblingForType(node, ASTMethodDeclaration.class);
        if (methodDeclaration == null) {
            return super.visit(node, data);
        }
        try {
            List<Node> nodes = methodDeclaration.findChildNodesWithXPath(XPATH_FOR_ROLLBACK);
            if (nodes != null && !nodes.isEmpty()) {
                return super.visit(node, data);
            }
            addViolationWithMessage(data, methodDeclaration, MESSAGE_KEY_PREFIX,
                new Object[] {methodDeclaration.getMethodName()});
        } catch (JaxenException ignore) {
        }
        return super.visit(node, data);
    }

    private boolean shouldSkip(List<ASTMemberValuePair> memberValuePairList) {
        for (ASTMemberValuePair pair : memberValuePairList) {
            String image = pair.getImage();
            if (image == null) {
                continue;
            }
            if (image.startsWith(ROLLBACK_PREFIX) || image.startsWith(READ_ONLY)) {
                return true;
            }
            ASTName name = pair.getFirstDescendantOfType(ASTName.class);
            if (name != null && PROPAGATION_NOT_SUPPORTED.equals(name.getImage())) {
                return true;
            }
        }
        return false;
    }

    /**
     * annotation is sibling of classOrInterface declaration or method declaration
     *
     * @param node transactional annotation
     * @param clz  classOrInterface declaration or method declaration
     * @param <T>  generic
     * @return sibling node
     */
    private <T> T getSiblingForType(ASTAnnotation node, Class<T> clz) {
        Node parent = node.jjtGetParent();
        int num = parent.jjtGetNumChildren();
        for (int i = 0; i < num; i++) {
            Node child = parent.jjtGetChild(i);
            if (clz.isAssignableFrom(child.getClass())) {
                return clz.cast(child);
            }
            if (!(child instanceof ASTAnnotation)) {
                return null;
            }
        }
        return null;
    }
}
