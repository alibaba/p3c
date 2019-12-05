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
import com.alibaba.p3c.pmd.lang.java.util.VariableUtils;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;

/**
 * When using regex, precompile needs to be done in order to increase the matching performance.
 * Note: Do not define Pattern pattern = Pattern.compile(.); within method body.
 *
 * @author keriezhang
 * @date 2016/12/14
 */
public class AvoidPatternCompileInMethodRule extends AbstractXpathRule {
    /**
     * The parameter of Pattern.compile cannot be a string literal.
     */
    private static final String XPATH = "//MethodDeclaration//PrimaryExpression["
        + "PrimaryPrefix/Name[@Image='Pattern.compile'] and "
        + "PrimarySuffix/Arguments/ArgumentList/Expression/"
        + "PrimaryExpression/PrimaryPrefix/Literal[@StringLiteral='true']]";

    public AvoidPatternCompileInMethodRule() {
        setXPath(XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        ASTLocalVariableDeclaration localVariableDeclaration = node.getFirstParentOfType(
            ASTLocalVariableDeclaration.class);
        if (localVariableDeclaration == null) {
            super.addViolation(data, node, arg);
        } else {
            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                I18nResources.getMessage("java.other.AvoidPatternCompileInMethodRule.violation.msg",
                    VariableUtils.getVariableName(localVariableDeclaration)));
        }
    }
}
