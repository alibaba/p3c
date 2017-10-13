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
package com.alibaba.p3c.pmd.lang.java.rule;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.fix.FixClassTypeResolver;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * re calculate node type
 *
 * @author caikang
 * @date 2016/11/20
 */
public abstract class AbstractAliRule extends AbstractJavaRule {
    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        FixClassTypeResolver classTypeResolver = new FixClassTypeResolver(AbstractAliRule.class.getClassLoader());
        node.setClassTypeResolver(classTypeResolver);
        node.jjtAccept(classTypeResolver, data);
        return super.visit(node, data);
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(I18nResources.getMessage(description));
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(I18nResources.getMessageWithExceptionHandled(message));
    }

    @Override
    public void addViolationWithMessage(Object data, Node node, String message) {
        super.addViolationWithMessage(data, node, I18nResources.getMessageWithExceptionHandled(message));
    }

    @Override
    public void addViolationWithMessage(Object data, Node node, String message, Object[] args) {
        super.addViolationWithMessage(data, node,
            String.format(I18nResources.getMessageWithExceptionHandled(message), args));
    }
}

