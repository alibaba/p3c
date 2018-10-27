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
package com.alibaba.p3c.pmd.lang.java.rule.naming;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.AbstractXpathRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;

/**
 * [Mandatory] Brackets are a part of an Array type. The definition could be: String[] args;
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class ArrayNamingShouldHaveBracketRule extends AbstractXpathRule {
    private static final String XPATH = "//VariableDeclaratorId[../..[@Array = 'true']]"
        + "[../../Type/ReferenceType[@Array != 'true']]";

    public ArrayNamingShouldHaveBracketRule() {
        setXPath(XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        ViolationUtils.addViolationWithPrecisePosition(this, node, data,
            I18nResources.getMessage("java.naming.ArrayNamingShouldHaveBracketRule.violation.msg",
                node.getImage()));
    }
}
