package com.alibaba.p3c.pmd.lang.java.rule.set;

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

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import org.jaxen.JaxenException;

import java.util.List;

/**
 * [Mandatory] Equals method must be with hashCode() method.
 *
 * @author leonard99559
 * @date 2019/10/16
 */
public class EqualsHashCodeRule extends AbstractAliRule {

    @Override
    public Object visit(ASTCompilationUnit rootNode, Object data) {

        try {
            List<Node> nodeList = rootNode.findChildNodesWithXPath(
                    "//ClassOrInterfaceDeclaration[@Abstract='false']"
                            + "//ClassOrInterfaceBodyDeclaration[./Annotation[@AnnotationName = 'Override']]"
                            + "//MethodDeclaration[@Name = 'hashCode' or @Name = 'equals']"
            );
            if (nodeList.size() == 1) {
                addViolationWithMessage(data, nodeList.get(0),
                        "java.set.EqualsHashCodeRule.rule.msg");
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return super.visit(rootNode, data);
    }

}
