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
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import org.jaxen.JaxenException;

/**
 * [Recommended] Set a size when initializing a collection if possible.
 *
 * @author shengfang.gsf
 * @date 2017/04/06
 */
public class CollectionInitShouldAssignCapacityRule extends AbstractAliRule {

    /**
     * Black List,will increase ArrayList, HashSet etc follow-up
     */
    private final static List<String> COLLECTION_LIST = NameListConfig.NAME_LIST_SERVICE
        .getNameList(CollectionInitShouldAssignCapacityRule.class.getSimpleName(), "COLLECTION_TYPE");

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        try {
            // find Collection initialization
            for (String collectionType : COLLECTION_LIST) {
                visitByCollections(node, data, collectionType);
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return super.visit(node, data);
    }

    private void visitByCollections(ASTClassOrInterfaceDeclaration node, Object data, String collectionType)
        throws JaxenException {
        String collectionArgXpath =
            "//AllocationExpression/ClassOrInterfaceType[@Image='" + collectionType + "']/../Arguments";
        List<Node> argumentsNodes = node.findChildNodesWithXPath(collectionArgXpath);

        for (Node argNode : argumentsNodes) {
            if (!(argNode instanceof ASTArguments)) {
                continue;
            }
            // filter not inner  method
            if (argNode.getFirstParentOfType(ASTMethodDeclaration.class) == null) {
                continue;
            }
            ASTArguments argumentNode = (ASTArguments)argNode;
            Integer count = argumentNode.getArgumentCount();
            // judge whether parameters have  initial size
            if (count == 0) {
                addViolationWithMessage(data, argNode,
                    "java.set.CollectionInitShouldAssignCapacityRule.violation.msg",
                    new Object[] {collectionType});
            }
        }
    }

}
