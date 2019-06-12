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

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractPojoRule;
import com.alibaba.p3c.pmd.lang.java.util.VariableUtils;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableInitializer;
import org.jaxen.JaxenException;

/**
 * [Mandatory] While defining POJO classes like DO, DTO, VO, etc., do not assign any default values to the members.
 *
 * @author zenghou.fw
 * @date 2016/11/22
 */
public class PojoNoDefaultValueRule extends AbstractPojoRule {

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
                boolean shouldProcess = !field.isPublic() && !field.isFinal() && !field.isStatic() && !field
                    .isVolatile() &&
                    field.hasDescendantOfType(ASTVariableInitializer.class);
                if (!shouldProcess) {
                    continue;
                }
                ViolationUtils.addViolationWithPrecisePosition(this, field, data,
                    I18nResources.getMessage("java.oop.PojoNoDefaultValueRule.violation.msg",
                        VariableUtils.getVariableName(field)));
            }
        } catch (JaxenException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return super.visit(node, data);
    }
}
