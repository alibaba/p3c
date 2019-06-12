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
import java.util.concurrent.ThreadFactory;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils;
import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;
import net.sourceforge.pmd.lang.java.ast.ASTImplementsList;
import net.sourceforge.pmd.lang.java.ast.ASTInitializer;
import net.sourceforge.pmd.lang.java.ast.ASTLambdaExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTResultType;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.Token;

/**
 * [Mandatory] Threads should be provided by thread pools. Explicitly creating threads is not allowed.
 * Note: Using thread pool can reduce the time of creating and destroying thread and save system resource.
 * If we do not use thread pools, lots of similar threads will be created which lead to
 * "running out of memory" or over-switching problems.
 *
 * Detection rule
 * New Thread can only be created in ThreadFactory.newThread method,as Runtime.getRuntime().addShutdownHook() parameter,
 * or in static block
 *
 * @author caikang
 * @date 2016/11/15
 * @see ThreadShouldSetNameRule
 */
public class AvoidManuallyCreateThreadRule extends AbstractAliRule {

    private static final String METHOD_NEW_THREAD = "newThread";

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        if (node.getType() != Thread.class) {
            return super.visit(node, data);
        }
        if (isAddShutdownHook(node) || isInStaticInitializer(node)) {
            return super.visit(node, data);
        }
        //Allocation in lambda block is ignored
        if (node.getFirstParentOfType(ASTLambdaExpression.class) != null) {
            return super.visit(node, data);
        }
        ASTFieldDeclaration fieldDeclaration = node.getFirstParentOfType(ASTFieldDeclaration.class);
        //field declaration with thread allocated
        if (fieldDeclaration != null && NodeUtils.getNodeType(fieldDeclaration) == Thread.class) {
            return addViolationAndReturn(node, data);
        }
        //Declare thread factory field use lambda
        if (node.getDataFlowNode() == null && node.getFirstParentOfType(ASTLambdaExpression.class) != null) {
            if (fieldDeclaration == null || NodeUtils.getNodeType(fieldDeclaration) != ThreadFactory.class) {
                return addViolationAndReturn(node, data);
            }
            return super.visit(node, data);
        }

        //in newThread(Runnable) method is ok
        if (isInNewThreadMethod(node)) {
            return super.visit(node, data);
        }
        //implements of ThreadFactory
        boolean isThreadFactory = (checkForNamingClass(node) || threadFactoryVariable(node))
            && isInPrimaryOrProtectedMethod(node);
        if (isThreadFactory) {
            return super.visit(node, data);
        }
        return addViolationAndReturn(node, data);
    }

    private boolean isAddShutdownHook(ASTAllocationExpression node) {
        ASTBlockStatement blockStatement = node.getFirstParentOfType(ASTBlockStatement.class);
        if (blockStatement == null) {
            return false;
        }
        Token token = (Token)blockStatement.jjtGetFirstToken();
        return Runtime.class.getSimpleName().equals(token.image);
    }

    private boolean isInStaticInitializer(ASTAllocationExpression node) {
        ASTInitializer initializer = node.getFirstParentOfType(ASTInitializer.class);
        return initializer != null && initializer.isStatic();
    }

    private boolean threadFactoryVariable(ASTAllocationExpression node) {
        ASTMethodDeclaration methodDeclaration = node.getFirstParentOfType(ASTMethodDeclaration.class);
        if (methodDeclaration == null) {
            return false;
        }
        ASTVariableDeclarator variableDeclarator = methodDeclaration.getFirstParentOfType(ASTVariableDeclarator.class);
        return variableDeclarator != null && variableDeclarator.getType() == ThreadFactory.class;
    }

    private boolean isInNewThreadMethod(ASTAllocationExpression node) {
        ASTMethodDeclaration methodDeclaration = node.getFirstParentOfType(ASTMethodDeclaration.class);
        if (methodDeclaration == null) {
            return false;
        }
        if (!returnThread(methodDeclaration)) {
            return false;
        }
        if (!METHOD_NEW_THREAD.equals(methodDeclaration.getMethodName())) {
            return false;
        }
        List<ASTFormalParameter> parameters = methodDeclaration.getFirstDescendantOfType(ASTFormalParameters.class)
            .findChildrenOfType(ASTFormalParameter.class);
        return parameters.size() == 1
            && parameters.get(0).getFirstChildOfType(ASTType.class).getType() == Runnable.class;
    }

    private boolean isInPrimaryOrProtectedMethod(ASTAllocationExpression node) {
        ASTMethodDeclaration methodDeclaration = node.getFirstParentOfType(ASTMethodDeclaration.class);
        return methodDeclaration != null && returnThread(methodDeclaration) && (methodDeclaration.isPrivate()
            || methodDeclaration.isProtected());
    }

    private boolean returnThread(ASTMethodDeclaration methodDeclaration) {
        ASTResultType resultType = methodDeclaration.getFirstChildOfType(ASTResultType.class);
        ASTType type = resultType.getFirstChildOfType(ASTType.class);
        return type != null && type.getType() == Thread.class;
    }

    private Object addViolationAndReturn(ASTAllocationExpression node, Object data) {
        addViolationWithMessage(data, node, "java.concurrent.AvoidManuallyCreateThreadRule.violation.msg");
        return super.visit(node, data);
    }

    private boolean checkForNamingClass(ASTAllocationExpression node) {
        ASTClassOrInterfaceDeclaration classOrInterfaceDeclaration =
            node.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        if (classOrInterfaceDeclaration == null) {
            return false;
        }
        ASTImplementsList implementsList = classOrInterfaceDeclaration.getFirstChildOfType(ASTImplementsList.class);
        if (implementsList == null) {
            return false;
        }
        List<ASTClassOrInterfaceType> interfaceTypes = implementsList.findChildrenOfType(ASTClassOrInterfaceType.class);
        for (ASTClassOrInterfaceType type : interfaceTypes) {
            if (type.getType() == ThreadFactory.class) {
                return true;
            }
        }
        return false;
    }
}
