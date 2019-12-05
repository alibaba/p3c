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
package com.alibaba.p3c.pmd.lang.java.rule.oop;

import net.sourceforge.pmd.testframework.SimpleAggregatorTst;

/**
 * Test for oop rules.
 *
 * @author zenghou.fw
 * @date 2016/11/29
 *
 */
public class OopRuleTest extends SimpleAggregatorTst {

    // 加载CLASSPATH下的rulesets/java/ali-oop.xml
    private static final String RULESET = "java-ali-oop";

    @Override
    public void setUp() {
        addRule(RULESET, "EqualsAvoidNullRule");
        addRule(RULESET, "WrapperTypeEqualityRule");
        addRule(RULESET, "PojoNoDefaultValueRule");
        addRule(RULESET, "PojoMustUsePrimitiveFieldRule");
        addRule(RULESET, "PojoMustOverrideToStringRule");
        addRule(RULESET, "StringConcatRule");
        addRule(RULESET, "BigDecimalAvoidDoubleConstructorRule");
    }
}
