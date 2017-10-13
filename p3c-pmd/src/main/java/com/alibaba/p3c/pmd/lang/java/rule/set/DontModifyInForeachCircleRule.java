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
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import org.jaxen.JaxenException;

/**
 * [Mandatory] Do not remove or add elements to a collection in a foreach loop. Please use Iterator to remove an item.
 * Iterator object should be synchronized when executing concurrent operations.
 *
 * @author shengfang.gsf
 * @date 2016/12/13
 */
public class DontModifyInForeachCircleRule extends AbstractAliRule {

    private final static String ADD = ".add";
    private final static String REMOVE = ".remove";
    private final static String CLEAR = ".clear";
    private final static String XPATH = "//ForStatement/Expression/PrimaryExpression/PrimaryPrefix/Name";
    private final static String CHILD_XPATH
        = "Statement/Block/BlockStatement/Statement/StatementExpression/PrimaryExpression/PrimaryPrefix/Name";

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (node.isInterface()) {
            return data;
        }
        try {
            List<Node> nodes = node.findChildNodesWithXPath(XPATH);
            for (Node item : nodes) {
                if (!(item instanceof ASTName)) {
                    continue;
                }
                String variableName = item.getImage();
                if (variableName == null) {
                    continue;
                }
                ASTForStatement forStatement = item.getFirstParentOfType(ASTForStatement.class);
                List<Node> blockNodes = forStatement.findChildNodesWithXPath(CHILD_XPATH);
                for (Node blockItem : blockNodes) {
                    if (!(blockItem instanceof ASTName)) {
                        continue;
                    }
                    if (judgeName(blockItem.getImage(), variableName)) {
                        addViolationWithMessage(data, blockItem,
                            "java.set.DontModifyInForeachCircleRule.violation.msg",
                            new Object[] {blockItem.getImage()});
                    }
                }
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return super.visit(node, data);
    }

    private boolean judgeName(String name, String variableName) {
        return name != null && (name.equals(variableName + ADD) ||
            name.equals(variableName + REMOVE) || name.equals(variableName + CLEAR));
    }

}
