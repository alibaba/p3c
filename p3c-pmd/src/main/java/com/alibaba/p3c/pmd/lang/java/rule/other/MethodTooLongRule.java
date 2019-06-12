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

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeSortUtils;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.ast.Comment;
import net.sourceforge.pmd.lang.java.ast.FormalComment;
import net.sourceforge.pmd.lang.java.ast.MultiLineComment;
import net.sourceforge.pmd.lang.java.ast.SingleLineComment;
import net.sourceforge.pmd.lang.java.ast.Token;

/**
 * [Recommended] The total number of lines for a method should not be more than 80.
 * Note: The total number of lines, including the method signature, closing brace, codes, blank lines,
 * line breaks and any invisible lines, should not be more than 80 (comments are not included).
 *
 * @author keriezhang
 * @date 2018/1/9
 */
public class MethodTooLongRule extends AbstractAliRule {

    private static final int MAX_LINE_COUNT = 80;
    private static final String ANNOTATION_PREFIX = "@";

    /**
     * sortedMap will be reinitialized for each source file.
     */
    private SortedMap<Integer, Node> sortedNodeAndComment;

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        sortedNodeAndComment = orderedCommentsAndExpressions(cUnit);
        return super.visit(cUnit, data);
    }

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

        // Get comment line count.
        int commentLineCount = getCommentLineCount(node);

        if (endLine - startLine - commentLineCount + 1 > MAX_LINE_COUNT) {
            ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                I18nResources.getMessage("java.other.MethodTooLongRule.violation.msg", node.getName()));
        }
        return data;
    }

    /**
     * Order comments and expressions.
     *
     * @param cUnit compilation unit
     * @return sorted comments and expressions
     */
    protected SortedMap<Integer, Node> orderedCommentsAndExpressions(ASTCompilationUnit cUnit) {

        SortedMap<Integer, Node> itemsByLineNumber = new TreeMap<>();

        // expression nodes
        List<ASTExpression> expressionNodes = cUnit.findDescendantsOfType(ASTExpression.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, expressionNodes);

        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, cUnit.getComments());

        return itemsByLineNumber;
    }

    /**
     * Get number of comment lines
     *
     * @param methodDecl
     * @return
     */
    private int getCommentLineCount(ASTMethodDeclaration methodDecl) {
        int lineCount = 0;
        AbstractJavaNode lastNode = null;

        for (Entry<Integer, Node> entry : sortedNodeAndComment.entrySet()) {
            Node value = entry.getValue();
            if (value.getBeginLine() <= methodDecl.getBeginLine()) {
                continue;
            }
            if (value.getBeginLine() > methodDecl.getEndLine()) {
                break;
            }

            // value should be either expression or comment.
            if (value instanceof AbstractJavaNode) {
                lastNode = (AbstractJavaNode)value;
            } else if (value instanceof FormalComment || value instanceof MultiLineComment) {
                Comment comment = (Comment)value;
                lineCount += comment.getEndLine() - comment.getBeginLine() + 1;
            } else if (value instanceof SingleLineComment) {
                SingleLineComment singleLineComment = (SingleLineComment)value;
                // Comment may in the same line with node.
                if (lastNode == null || singleLineComment.getBeginLine() != lastNode.getBeginLine()) {
                    lineCount += 1;
                }
            }
        }
        return lineCount;
    }
}
