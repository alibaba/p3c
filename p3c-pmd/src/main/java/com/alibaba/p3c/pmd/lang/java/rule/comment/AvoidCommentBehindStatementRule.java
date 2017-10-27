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
package com.alibaba.p3c.pmd.lang.java.rule.comment;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeSortUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTEnumConstant;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.ast.Comment;

/**
 * [Mandatory] Single line comments in a method should be put above the code to be commented, by using // and
 * multiple lines by using \/* *\/. Alignment for comments should be noticed carefully.
 *
 * @author keriezhang
 * @date 2016/12/14
 */
public class AvoidCommentBehindStatementRule extends AbstractAliCommentRule {

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        SortedMap<Integer, Node> itemsByLineNumber = orderedCommentsAndExpressions(cUnit);
        AbstractJavaNode lastNode = null;

        for (Entry<Integer, Node> entry : itemsByLineNumber.entrySet()) {
            Node value = entry.getValue();
            if (value instanceof AbstractJavaNode) {
                lastNode = (AbstractJavaNode)value;
            } else if (value instanceof Comment) {
                Comment comment = (Comment)value;
                if (lastNode != null && (comment.getBeginLine() == lastNode.getBeginLine())
                    && (comment.getEndColumn() > lastNode.getBeginColumn())) {
                    addViolationWithMessage(data, lastNode,
                        I18nResources.getMessage("java.comment.AvoidCommentBehindStatementRule.violation.msg"),
                        comment.getBeginLine(), comment.getEndLine());
                }
            }
        }

        return super.visit(cUnit, data);
    }

    /**
     * Check comments behind nodes.
     *
     * @param cUnit compilation unit
     * @return sorted comments and expressions
     */
    protected SortedMap<Integer, Node> orderedCommentsAndExpressions(ASTCompilationUnit cUnit) {

        SortedMap<Integer, Node> itemsByLineNumber = new TreeMap<>();

        // expression nodes
        List<ASTExpression> expressionNodes = cUnit.findDescendantsOfType(ASTExpression.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, expressionNodes);

        // filed declaration nodes
        List<ASTFieldDeclaration> fieldNodes =
            cUnit.findDescendantsOfType(ASTFieldDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, fieldNodes);

        // enum constant nodes
        List<ASTEnumConstant> enumConstantNodes =
            cUnit.findDescendantsOfType(ASTEnumConstant.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, enumConstantNodes);

        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, cUnit.getComments());

        return itemsByLineNumber;
    }

}
