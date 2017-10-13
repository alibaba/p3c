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
package com.alibaba.p3c.pmd.lang.java.rule.other;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.AbstractXpathRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;

/**
 * Use System.currentTimeMillis() to get the current millisecond. Do not use new Date().getTime().
 *
 * @author keriezhang
 * @date 2016/12/14
 */
public class AvoidNewDateGetTimeRule extends AbstractXpathRule {

    private static final String XPATH =
        "//PrimaryExpression"
            + "["
            + "PrimaryPrefix/AllocationExpression/ClassOrInterfaceType[@Image='Date'] and "
            + "PrimaryPrefix/AllocationExpression/Arguments[@ArgumentCount=0] and "
            + "PrimarySuffix[@Image='getTime'] and "
            + "PrimarySuffix/Arguments[@ArgumentCount=0]"
            + "]";

    public AvoidNewDateGetTimeRule() {
        setXPath(XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        ViolationUtils.addViolationWithPrecisePosition(this, node, data,
            I18nResources.getMessage("java.other.AvoidNewDateGetTimeRule.violation.msg"));
    }
}
