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

import java.util.Timer;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaTypeNode;

/**
 * [Mandatory] Run multiple TimeTask by using ScheduledExecutorService rather than Timer
 * because Timer will kill all running threads in case of failing to catch exception.
 *
 * @author caikang
 * @date 2016/11/15
 */
public class AvoidUseTimerRule extends AbstractAliRule {
    @Override
    public Object visit(ASTVariableDeclarator node, Object data) {
        checkType(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTPrimaryExpression node, Object data) {
        ASTVariableDeclarator variableDeclarator = node.getFirstParentOfType(ASTVariableDeclarator.class);
        if (variableDeclarator != null && variableDeclarator.getType() == Timer.class) {
            return super.visit(node, data);
        }
        checkType(node, data);
        return super.visit(node, data);
    }

    private void checkType(AbstractJavaTypeNode node, Object data) {
        if (node.getType() == Timer.class) {
            addViolationWithMessage(data, node,"java.concurrent.AvoidUseTimerRule.violation.msg");
        }
    }
}
