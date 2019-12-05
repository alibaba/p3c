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
package com.alibaba.p3c.pmd.lang.java.rule.oop;

import java.util.List;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractPojoRule;

import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils;
import com.alibaba.p3c.pmd.lang.java.util.VariableUtils;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import org.jaxen.JaxenException;

/**
 * [Mandatory]  Rules for using primitive data types and wrapper classes:
 * 1) Members of a POJO class must be wrapper classes.
 * 2) The return value and arguments of a RPC method must be wrapper classes.
 * 3) [Recommended] Local variables should be primitive data types.
 *
 * check only 1) here
 *
 * @author zenghou.fw
 * @date 2016/11/25
 */
public class PojoMustUsePrimitiveFieldRule extends AbstractPojoRule {

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (!isPojo(node)) {
            return super.visit(node, data);
        }
        try {
            List<Node> fields = node.findChildNodesWithXPath(
                "ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/FieldDeclaration");

            for (Node fieldNode : fields) {
                ASTFieldDeclaration field = (ASTFieldDeclaration)fieldNode;
                boolean shouldProcess = !field.isPublic() && !field.isStatic() && !field.isTransient();
                if (!shouldProcess) {
                    continue;
                }
                Class type = NodeUtils.getNodeType(field);
                // TODO works only in current compilation file, by crossing files will be null
                if (type != null && type.isPrimitive()) {
                    addViolationWithMessage(data, field.getFirstDescendantOfType(ASTType.class),
                        "java.oop.PojoMustUsePrimitiveFieldRule.violation.msg",
                        new Object[] {VariableUtils.getVariableName(field)});
                }
            }
        } catch (JaxenException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return super.visit(node, data);
    }
}
