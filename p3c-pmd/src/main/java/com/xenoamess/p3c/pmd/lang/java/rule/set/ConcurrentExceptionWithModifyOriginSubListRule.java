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
import org.jaxen.JaxenException;

import java.util.List;

/**
 * [Mandatory] When using subList, be careful to modify the size of original list. It might cause
 * ConcurrentModificationException when performing traversing, adding or deleting on the subList.
 *
 * @author shengfang.gsf
 * @date 2016/12/13
 */
public class ConcurrentExceptionWithModifyOriginSubListRule extends AbstractAliRule {

    private static final String ADD = ".add";
    private static final String REMOVE = ".remove";
    private static final String CLEAR = ".clear";
    private static final String XPATH
            = "//VariableDeclarator[../Type/ReferenceType/ClassOrInterfaceType[@Image='List']]/VariableInitializer"
            + "/Expression/PrimaryExpression/PrimaryPrefix/Name[ends-with(@Image,'.subList')]";
    private static final String CHILD_XPATH
            = "BlockStatement/Statement/StatementExpression/PrimaryExpression/PrimaryPrefix/Name";

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
                String valName = getBeforeSubListVal(item.getImage());
                ASTBlock blockNode = item.getFirstParentOfType(ASTBlock.class);
                if (blockNode == null || valName == null) {
                    continue;
                }
                List<Node> blockNodes = blockNode.findChildNodesWithXPath(CHILD_XPATH);

                for (Node blockItem : blockNodes) {
                    // adding or deleting on the subList result is forbidden
                    if (blockItem.getBeginLine() < item.getBeginLine()) {
                        continue;
                    }
                    if (checkBlockNodesValid(valName, blockItem)) {
                        addViolationWithMessage(data, blockItem,
                                "java.set.ConcurrentExceptionWithModifyOriginSubListRule.violation.msg",
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
     * Find subList original variable
     *
     * @param image image
     * @return image.substring(0, image.indexOf ( " . "))
     */
    private static String getBeforeSubListVal(String image) {
        return image == null ? null : image.substring(0, image.indexOf("."));
    }

    /**
     * Only to find out whether there is any violation within the scope of the  method
     *
     * @param variableName variableName
     * @param item node
     * @return ifCheckBlockNodesValid
     */
    private static boolean checkBlockNodesValid(String variableName, Node item) {
        if (item instanceof ASTName) {
            String name = item.getImage();
            return judgeName(name, variableName);
        }
        return false;
    }

    /**
     * judge name equals to  t.add / t.remove / t.clear
     *
     * @param name name
     * @param variableName variableName
     * @return if name
     */
    private static boolean judgeName(String name, String variableName) {
        return name.equals(variableName + ADD) || name.equals(variableName + REMOVE)
                || name.equals(variableName + CLEAR);
    }
}
