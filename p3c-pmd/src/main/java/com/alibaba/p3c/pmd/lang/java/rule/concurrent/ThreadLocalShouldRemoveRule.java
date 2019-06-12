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
package com.alibaba.p3c.pmd.lang.java.rule.concurrent;

import java.util.List;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeUtils;
import com.alibaba.p3c.pmd.lang.java.util.VariableUtils;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import org.jaxen.JaxenException;

/**
 * [Mandatory] Customized ThreadLocal variables must be recycled, especially when using thread pools in which threads
 * are often reused. Otherwise, it may affect subsequent business logic and cause unexpected problems such as memory
 * leak.
 *
 * @author caikang
 * @date 2017/03/29
 */
public class ThreadLocalShouldRemoveRule extends AbstractAliRule {
    private static final String XPATH_TPL = "//StatementExpression/PrimaryExpression"
        + "/PrimaryPrefix/Name[@Image='%s.remove']";

    private static final String METHOD_INITIAL_VALUE = "initialValue";

    private static final String WITH_INITIAL = "ThreadLocal.withInitial";

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        List<ASTFieldDeclaration> fieldDeclarations = node.findDescendantsOfType(ASTFieldDeclaration.class);
        if (fieldDeclarations == null || fieldDeclarations.isEmpty()) {
            return super.visit(node, data);
        }
        for (ASTFieldDeclaration fieldDeclaration : fieldDeclarations) {
            if (NodeUtils.getNodeType(fieldDeclaration) == ThreadLocal.class) {
                if (checkThreadLocalWithInitalValue(fieldDeclaration)) { continue; }
                checkThreadLocal(fieldDeclaration, node, data);
            }
        }
        return super.visit(node, data);
    }

    private boolean checkThreadLocalWithInitalValue(ASTFieldDeclaration fieldDeclaration) {
        ASTVariableDeclarator variableDeclarator = fieldDeclaration.getFirstDescendantOfType(
            ASTVariableDeclarator.class);
        if (variableDeclarator == null) {
            return false;
        }
        // ASTClassOrInterfaceBodyDeclaration.isFindBoundary=true，使用getFirstDescendantOfType不能继续向方法内部查询
        List<ASTMethodDeclarator> astMethodDeclaratorList = variableDeclarator.findDescendantsOfType(
            ASTMethodDeclarator.class, true);
        if (!astMethodDeclaratorList.isEmpty()) {
            return METHOD_INITIAL_VALUE.equals(astMethodDeclaratorList.get(0).getImage());
        }
        ASTName name = variableDeclarator.getFirstDescendantOfType(ASTName.class);
        return name != null && WITH_INITIAL.equals(name.getImage());
    }

    private void checkThreadLocal(ASTFieldDeclaration fieldDeclaration, ASTCompilationUnit node, Object data) {
        try {
            String variableName = VariableUtils.getVariableName(fieldDeclaration);
            List<Node> nodes = node.findChildNodesWithXPath(String.format(XPATH_TPL,
                variableName));
            if (nodes == null || nodes.isEmpty()) {
                ViolationUtils.addViolationWithPrecisePosition(this, fieldDeclaration, data,
                    I18nResources.getMessage("java.concurrent.ThreadLocalShouldRemoveRule.violation.msg",
                        variableName));
            }
        } catch (JaxenException ignore) {
        }
    }
}
