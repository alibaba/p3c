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
 * [Mandatory] All Service and DAO classes must be interface based on SOA principle. Implementation class names
 * should be ended with Impl.
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class ServiceOrDaoClassShouldEndWithImplRule extends AbstractXpathRule {
    private static final String XPATH = "//ClassOrInterfaceDeclaration"
        + "[ .[@Interface='false'] and .[@Abstract='false'] and ./ImplementsList/ClassOrInterfaceType[ ends-with(@Image, 'Service') or "
        + "ends-with(@Image, 'DAO')]]"
        + "[not(.[ ends-with(@Image, 'Impl')])]";

    public ServiceOrDaoClassShouldEndWithImplRule() {
        setXPath(XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        ViolationUtils.addViolationWithPrecisePosition(this, node, data,
            I18nResources.getMessage("java.naming.ServiceOrDaoClassShouldEndWithImplRule.violation.msg",
                node.getImage()));
    }
}
