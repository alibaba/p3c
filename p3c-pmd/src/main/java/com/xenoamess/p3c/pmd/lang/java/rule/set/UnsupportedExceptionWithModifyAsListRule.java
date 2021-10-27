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
package com.xenoamess.p3c.pmd.lang.java.rule.set;

import com.xenoamess.p3c.pmd.lang.java.rule.AbstractAliRule;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import org.jaxen.JaxenException;

import java.util.List;

/**
 * [Mandatory] Do not use methods which will modify the list after using Arrays.asList to convert array to list,
 * otherwise methods like add/remove/clear will throw UnsupportedOperationException.
 *
 * @author shengfang.gsf
 * @date 2016/12/13
 */
public class UnsupportedExceptionWithModifyAsListRule extends AbstractAliRule {

    private static final String ADD = ".add";
    private static final String REMOVE = ".remove";
    private static final String CLEAR = ".clear";
    private static final String XPATH
            = "//VariableDeclarator[../Type/ReferenceType/ClassOrInterfaceType[@Image='List']]/VariableInitializer"
            + "/Expression/PrimaryExpression/PrimaryPrefix/Name[@Image='Arrays.asList']";
    private static final String CHILD_XPATH
            = "BlockStatement/Statement/StatementExpression/PrimaryExpression/PrimaryPrefix/Name";

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (node.isInterface()) {
            return data;
        }
        try {
            // find Array.asList variable
            List<Node> nodes = node.findChildNodesWithXPath(XPATH);
            for (Node item : nodes) {
                if (!(item instanceof ASTName)) {
                    continue;
                }
                List<ASTVariableDeclarator> parents =
                        item.getParentsOfType(ASTVariableDeclarator.class);
                if (parents == null || parents.size() == 0 || parents.size() > 1) {
                    continue;
                }
                ASTVariableDeclarator declarator = parents.get(0);
                ASTVariableDeclaratorId variableName =
                        declarator.getFirstChildOfType(ASTVariableDeclaratorId.class);

                String valName = variableName.getImage();
                // find Variable scope code block
                ASTBlock blockNode = variableName.getFirstParentOfType(ASTBlock.class);
                if (blockNode == null || valName == null) {
                    continue;
                }
                List<Node> blockNodes = blockNode.findChildNodesWithXPath(CHILD_XPATH);
                // variable.add .removed is forbidden.
                for (Node blockItem : blockNodes) {
                    if (blockItem.getBeginLine() < item.getBeginLine()) {
                        continue;
                    }
                    if (checkBlockNodesValid(valName, blockItem)) {
                        addViolationWithMessage(data, blockItem,
                                "java.set.UnsupportedExceptionWithModifyAsListRule.violation.msg",
                                new Object[]{blockItem.getImage()});
                    }
                }

            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return super.visit(node, data);
    }

    /**
     * Only to find out whether there is any violation within the scope of the corresponding method
     *
     * @param variableName variableName
     * @param item node
     * @return ifCheckBlockNodesValid
     */
    private boolean checkBlockNodesValid(String variableName, Node item) {
        if (item instanceof ASTName) {
            String name = item.getImage();
            return judgeName(name, variableName);
        }
        return false;
    }

    /**
     * judge name equals t.add t.remove t.clear
     *
     * @param name name
     * @param variableName variableName
     * @return if name suits
     */
    private boolean judgeName(String name, String variableName) {
        return name.equals(variableName + ADD) || name.equals(variableName + REMOVE)
                || name.equals(variableName + CLEAR);
    }

}
