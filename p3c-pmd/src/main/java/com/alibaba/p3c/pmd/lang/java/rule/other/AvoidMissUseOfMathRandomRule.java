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
 * [Mandatory] The return type of Math.random() is double, value range is 0&lt;=x&lt;1 (0 is possible).
 * If a random integer is required, do not multiply x by 10 then round the result.
 * The correct way is to use nextInt or nextLong method which belong to Random Object.
 *
 * @author keriezhang
 * @date 2017/04/14
 */
public class AvoidMissUseOfMathRandomRule extends AbstractXpathRule {

    private static final String XPATH =
        "//PrimaryExpression[./PrimaryPrefix/Name[@Image='Math.random'] and "
            + "../../MultiplicativeExpression/PrimaryExpression/PrimaryPrefix/Literal[matches(@Image, '^\\d+$')] and "
            + "../../../../../../CastExpression/Type/PrimitiveType[matches(@Image, '^(int|long)$')]"
            + "]";

    public AvoidMissUseOfMathRandomRule() {
        setXPath(XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        ViolationUtils.addViolationWithPrecisePosition(this, node, data);
    }
}
