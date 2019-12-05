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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.java.ast.ASTAllocationExpression;
import net.sourceforge.pmd.lang.java.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;
import net.sourceforge.pmd.lang.java.ast.ASTLambdaExpression;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;

/**
 * [Mandatory] A meaningful thread name is helpful to trace the error information,
 * so assign a name when creating threads or thread pools.
 *
 * Detection rule
 * 1. Use specific constructor while create thread pool
 * 2. Use Executors.defaultThreadFactory() is not allowed
 *
 * @author caikang
 * @date 2016/11/16
 * @see AvoidManuallyCreateThreadRule
 */
public class ThreadShouldSetNameRule extends AbstractAliRule {
    private static final int ARGUMENT_LENGTH_2 = 2;
    private static final int ARGUMENT_LENGTH_6 = 6;
    private static final int INDEX_1 = 1;
    private static final int SINGLE_LENGTH = 1;

    private static final String MESSAGE_KEY_PREFIX = "java.concurrent.ThreadShouldSetNameRule.violation.msg";

    @Override
    public Object visit(ASTAllocationExpression node, Object data) {
        //Custom Class
        if (node.getType() == null) {
            return super.visit(node, data);
        }
        if (!ExecutorService.class.isAssignableFrom(node.getType())) {
            return super.visit(node, data);
        }
        if (ThreadPoolExecutor.class == node.getType()) {
            return checkThreadPoolExecutor(node, data);
        }
        if (ScheduledThreadPoolExecutor.class == node.getType()) {
            return checkSchedulePoolExecutor(node, data);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        return super.visit(node, data);
    }

    private Object checkThreadPoolExecutor(ASTAllocationExpression node, Object data) {
        ASTArgumentList argumentList = node.getFirstDescendantOfType(ASTArgumentList.class);
        if (argumentList.jjtGetNumChildren() > ARGUMENT_LENGTH_6) {
            return true;
        }
        if (argumentList.jjtGetNumChildren() < ARGUMENT_LENGTH_6
            || !checkThreadFactoryArgument((ASTExpression)argumentList.jjtGetChild(ARGUMENT_LENGTH_6 - INDEX_1))) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".ThreadPoolExecutor");
        }
        return super.visit(node, data);
    }

    private Object checkSchedulePoolExecutor(ASTAllocationExpression node, Object data) {
        ASTArgumentList argumentList = node.getFirstDescendantOfType(ASTArgumentList.class);
        if (argumentList.jjtGetNumChildren() > ARGUMENT_LENGTH_2) {
            return true;
        }
        if (argumentList.jjtGetNumChildren() < ARGUMENT_LENGTH_2
            || !checkThreadFactoryArgument((ASTExpression)argumentList.jjtGetChild(ARGUMENT_LENGTH_2 - INDEX_1))) {
            addViolationWithMessage(data, node, MESSAGE_KEY_PREFIX + ".ScheduledThreadPoolExecutor");
        }
        return super.visit(node, data);
    }

    private boolean checkThreadFactoryArgument(ASTExpression expression) {
        if (expression.getType() != null && ThreadFactory.class.isAssignableFrom(expression.getType())) {
            return true;
        }
        ASTName name = expression.getFirstDescendantOfType(ASTName.class);
        if (name != null && name.getType() == Executors.class) {
            return false;
        }
        ASTLambdaExpression lambdaExpression = expression.getFirstDescendantOfType(ASTLambdaExpression.class);
        if (lambdaExpression != null) {
            return isThreadFactoryLambda(lambdaExpression);
        } else if (expression.getType() != null
            && RejectedExecutionHandler.class.isAssignableFrom(expression.getType())) {
            return false;
        }
        return true;
    }

    private boolean isThreadFactoryLambda(ASTLambdaExpression lambdaExpression) {
        List<ASTVariableDeclaratorId> variableDeclaratorIds =
            lambdaExpression.findChildrenOfType(ASTVariableDeclaratorId.class);
        if (variableDeclaratorIds != null && !variableDeclaratorIds.isEmpty()) {
            return variableDeclaratorIds.size() == SINGLE_LENGTH;
        }

        // like (Runnable r) ->
        ASTFormalParameters parameters = lambdaExpression.getFirstChildOfType(ASTFormalParameters.class);
        if (parameters == null) {
            return false;
        }

        ASTFormalParameter parameter = parameters.getFirstChildOfType(ASTFormalParameter.class);
        if (parameter == null) {
            return false;
        }
        ASTVariableDeclaratorId variableDeclaratorId = parameter.getFirstChildOfType(ASTVariableDeclaratorId.class);
        if (variableDeclaratorId == null) {
            return false;
        }
        return Runnable.class == variableDeclaratorId.getType();
    }
}
