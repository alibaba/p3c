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
import net.sourceforge.pmd.lang.java.ast.ASTName;
import org.jaxen.JaxenException;

/**
 * [Mandatory] Do not cast subList in class ArrayList, otherwise ClassCastException will be
 * thrown：java.util.RandomAccessSubList
 * cannot be cast to java.util.ArrayList ;
 *
 * @author shengfang.gsf
 * @date 2016/12/13
 */
public class ClassCastExceptionWithSubListToArrayListRule extends AbstractAliRule {

    private static final String XPATH =
        "//CastExpression[Type/ReferenceType/ClassOrInterfaceType[@Image = "
            + "\"ArrayList\"]]/PrimaryExpression/PrimaryPrefix/Name[ends-with(@Image,'.subList')]";

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
                addViolationWithMessage(data, item,
                    "java.set.ClassCastExceptionWithSubListToArrayListRule.violation.msg",
                    new Object[] {item.getImage()});
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return super.visit(node, data);
    }
}
