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
package com.xenoamess.p3c.pmd.lang.java.rule.naming;

import com.xenoamess.p3c.pmd.I18nResources;
import com.xenoamess.p3c.pmd.lang.AbstractAliXpathRule;
import com.xenoamess.p3c.pmd.lang.java.util.ViolationUtils;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;

/**
 * [Mandatory] Do not add 'is' as prefix while defining Boolean variable, since it may cause a serialization exception
 * in some Java Frameworks.
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class BooleanPropertyShouldNotStartWithIsRule extends AbstractAliXpathRule {
    private static final String XPATH = "//VariableDeclaratorId[(ancestor::ClassOrInterfaceDeclaration)["
            + "@Interface='false' and ( ends-with(@SimpleName, 'DO') or ends-with(@SimpleName, 'DTO')"
            + " or ends-with(@SimpleName, 'VO') or ends-with(@SimpleName, 'DAO'))]]"
            + "[../../../FieldDeclaration/Type/PrimitiveType[@Image = 'boolean']][.[ starts-with(@Name, 'is')]]";

    public BooleanPropertyShouldNotStartWithIsRule() {
        setXPath(XPATH);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        if (node instanceof ASTVariableDeclaratorId) {
            ViolationUtils.addViolationWithPrecisePosition(
                    this,
                    node,
                    data,
                    I18nResources.getMessage("java.naming.BooleanPropertyShouldNotStartWithIsRule.violation.msg",
                            node.getImage()
                    )
            );
        } else {
            super.addViolation(data, node, arg);
        }
    }
}
