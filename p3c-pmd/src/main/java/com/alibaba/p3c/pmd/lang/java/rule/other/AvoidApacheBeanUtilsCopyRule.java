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

import com.alibaba.p3c.pmd.lang.AbstractXpathRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;

/**
 * Avoid using *Apache Beanutils* to copy attributes.
 * Note: *Spring BeanUtils* and *Cglib BeanCopier* are recommended to be used, which have better performance.
 * 
 * @author keriezhang
 * @date 2016/12/14
 *
 */
public class AvoidApacheBeanUtilsCopyRule extends AbstractXpathRule {
    private static final String XPATH =
            "//PrimaryPrefix/Name[@Image='BeanUtils.copyProperties' and "
            + "//ImportDeclaration[@ImportedName='org.apache.commons.beanutils.BeanUtils']]";

    public AvoidApacheBeanUtilsCopyRule() {
        setXPath(XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        ViolationUtils.addViolationWithPrecisePosition(this, node, data);
    }
}
