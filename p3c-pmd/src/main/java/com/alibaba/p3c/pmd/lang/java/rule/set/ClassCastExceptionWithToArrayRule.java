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
package com.alibaba.p3c.pmd.lang.java.rule.set;

import java.util.List;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import org.jaxen.JaxenException;

/**
 * [Mandatory] Do not use toArray method without arguments. Since the return type is Object[], ClassCastException will
 * be thrown when casting it to a different array type。
 *
 * @author shengfang.gsf
 * @date 2016/12/13
 */
public class ClassCastExceptionWithToArrayRule extends AbstractAliRule {

    private static final String XPATH
        = "//CastExpression[Type/ReferenceType/ClassOrInterfaceType[@Image !=\"Object\"]]/PrimaryExpression";

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (node.isInterface()) {
            return data;
        }
        try {
            List<Node> nodes = node.findChildNodesWithXPath(XPATH);
            for (Node item : nodes) {
                if (!(item instanceof ASTPrimaryExpression)) {
                    continue;
                }
                ASTPrimaryExpression primaryExpression = (ASTPrimaryExpression)item;
                List<ASTPrimaryPrefix> primaryPrefixs =
                    primaryExpression.findChildrenOfType(ASTPrimaryPrefix.class);
                List<ASTPrimarySuffix> primarySuffixs =
                    primaryExpression.findChildrenOfType(ASTPrimarySuffix.class);
                if (primaryPrefixs == null || primarySuffixs == null || primaryPrefixs.isEmpty()
                    || primarySuffixs.isEmpty()) {
                    continue;
                }
                ASTPrimaryPrefix prefix = primaryPrefixs.get(0);
                ASTPrimarySuffix suffix = primarySuffixs.get(0);
                if (prefix.jjtGetNumChildren() == 0) {
                    continue;
                }
                Node prefixChildNode = prefix.jjtGetChild(0);
                String childName = prefixChildNode.getImage();
                if (childName == null) {
                    continue;
                }
                if (childName.endsWith(".toArray") && suffix.getArgumentCount() == 0
                    && primarySuffixs.size() == 1) {
                    addViolationWithMessage(data, item,
                        "java.set.ClassCastExceptionWithToArrayRule.violation.msg",
                        new Object[] {childName});
                }
            }

        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return super.visit(node, data);
    }
}
