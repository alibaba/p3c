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
package com.alibaba.p3c.pmd.lang.java.rule.other;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.Token;

/**
 * [Recommended] The total number of lines for a method should not be more than 80.
 * Note: The total number of lines, including the method signature, closing brace, codes, comments,
 * blank lines, line breaks and any invisible lines, should not be more than 80.
 *
 * @author keriezhang
 * @date 2018/1/9
 */
public class MethodTooLongRule extends AbstractAliRule {

    private static final int MAX_LINE_COUNT = 80;
    private static final String ANNOTATION_PREFIX = "@";

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        // Include method modifiers.
        ASTClassOrInterfaceBodyDeclaration classOrInterfaceBodyDecl =
            (ASTClassOrInterfaceBodyDeclaration)node.jjtGetParent();

        int startLine = classOrInterfaceBodyDecl.getBeginLine();
        int endLine = classOrInterfaceBodyDecl.getEndLine();

        Node firstChild = classOrInterfaceBodyDecl.jjtGetChild(0);
        // Method has annotation
        if (firstChild instanceof ASTAnnotation) {
            Token firstToken = (Token)classOrInterfaceBodyDecl.jjtGetFirstToken();
            // If annotation is before modifier, exclude the annotation.
            if (ANNOTATION_PREFIX.equals(firstToken.image)) {
                ASTAnnotation annotation = (ASTAnnotation)firstChild;
                Token lastToken = (Token)annotation.jjtGetLastToken();

                // First token after annotation. The same line or next line after annotation.
                Token next = lastToken.next;
                startLine = next.beginLine;
            }
        }

        if (endLine - startLine + 1 > MAX_LINE_COUNT) {
            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                I18nResources.getMessage("java.other.MethodTooLongRule.violation.msg", node.getName()));
        }
        return super.visit(node, data);
    }
}
