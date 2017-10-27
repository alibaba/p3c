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
package com.alibaba.p3c.pmd.lang.java.rule.constant;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.java.ast.ASTLiteral;

/**
 * [Mandatory] 'L' instead of 'l' should be used for long or Long variable because 'l' is easily to
 * be regarded as number 1 in mistake.
 *
 * @author shengfang.gsf
 * @date 2016/12/13
 */
public class UpperEllRule extends AbstractAliRule {
    private static final String LOWERCASE_L = "l";

    @Override
    public Object visit(ASTLiteral node, Object data) {
        String image = node.getImage();
        // if it is an integer and ends with l, collects the current violation code
        if (image != null && node.isLongLiteral() && image.endsWith(LOWERCASE_L)) {
            addViolationWithMessage(data, node, "java.constant.UpperEllRule.violation.msg",
                new Object[] {node.getImage()});
        }
        return super.visit(node, data);
    }

}
