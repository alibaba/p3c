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
package com.xenoamess.p3c.pmd.lang;

import com.xenoamess.p3c.pmd.I18nResources;
import com.xenoamess.p3c.pmd.lang.java.util.ViolationUtils;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.RuleViolationFactory;
import net.sourceforge.pmd.lang.rule.XPathRule;

/**
 * @author caikang
 * @date 2017/05/25
 */
public abstract class AbstractAliXpathRule extends XPathRule {
    @Override
    public void setDescription(String description) {
        super.setDescription(I18nResources.getMessageWithExceptionHandled(description));
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(I18nResources.getMessageWithExceptionHandled(message));
    }

    @Override
    public void addViolationWithMessage(Object data, Node node, String message) {
        if (ViolationUtils.shouldIgnoreViolation(this.getClass(), node)) {
            return;
        }
        super.addViolationWithMessage(data, node, I18nResources.getMessageWithExceptionHandled(message));
    }

    @Override
    public void addViolationWithMessage(Object data, Node node, String message, Object[] args) {
        if (ViolationUtils.shouldIgnoreViolation(this.getClass(), node)) {
            return;
        }
        super.addViolationWithMessage(data, node,
                String.format(I18nResources.getMessageWithExceptionHandled(message), args));
    }

    /**
     * @see RuleViolationFactory#addViolation(net.sourceforge.pmd.RuleContext, net.sourceforge.pmd.Rule, Node, String,
     * Object[])
     */
    @Override
    public void addViolation(Object data, Node node) {
        if (ViolationUtils.shouldIgnoreViolation(this.getClass(), node)) {
            return;
        }
        super.addViolation(data, node);
    }

    /**
     * @see RuleViolationFactory#addViolation(net.sourceforge.pmd.RuleContext, net.sourceforge.pmd.Rule, Node, String,
     * Object[])
     */
    @Override
    public void addViolation(Object data, Node node, String arg) {
        if (ViolationUtils.shouldIgnoreViolation(this.getClass(), node)) {
            return;
        }
        super.addViolation(data, node, arg);
    }

    /**
     * @see RuleViolationFactory#addViolation(net.sourceforge.pmd.RuleContext, net.sourceforge.pmd.Rule, Node, String,
     * Object[])
     */
    @Override
    public void addViolation(Object data, Node node, Object[] args) {
        if (ViolationUtils.shouldIgnoreViolation(this.getClass(), node)) {
            return;
        }
        super.addViolation(data, node, args);
    }

    /**
     * @see RuleViolationFactory#addViolation(net.sourceforge.pmd.RuleContext, net.sourceforge.pmd.Rule, Node, String,
     * Object[])
     */
    @Override
    public void addViolationWithMessage(Object data, Node node, String message, int beginLine, int endLine) {
        if (ViolationUtils.shouldIgnoreViolation(this.getClass(), node)) {
            return;
        }
        super.addViolationWithMessage(data, node, message, beginLine, endLine);
    }
}
