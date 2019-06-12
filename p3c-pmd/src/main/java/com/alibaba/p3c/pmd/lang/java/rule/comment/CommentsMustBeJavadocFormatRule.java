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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.util.NodeSortUtils;

import com.alibaba.p3c.pmd.lang.java.util.VariableUtils;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaAccessNode;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.ast.Comment;
import net.sourceforge.pmd.lang.java.ast.MultiLineComment;
import net.sourceforge.pmd.lang.java.ast.SingleLineComment;
import net.sourceforge.pmd.lang.java.ast.Token;
import org.apache.commons.lang3.StringUtils;

/**
 * [Mandatory] Javadoc should be used for classes, class variables and methods.
 * The format should be '\/** comment *\/', rather than '// xxx'.
 *
 * @author keriezhang
 * @date 2016/12/14
 */
public class CommentsMustBeJavadocFormatRule extends AbstractAliCommentRule {

    private static final String MESSAGE_KEY_PREFIX = "java.comment.CommentsMustBeJavadocFormatRule.violation.msg";

    @Override
    public Object visit(final ASTClassOrInterfaceDeclaration decl, Object data) {
        checkComment(decl, data, () -> I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".class",
            decl.getImage()));
        return super.visit(decl, data);
    }

    @Override
    public Object visit(final ASTConstructorDeclaration decl, Object data) {
        checkComment(decl, data, () -> {
            String constructorName = ((Token)decl.jjtGetFirstToken()).image;
            if (decl.getFormalParameters().getParameterCount() == 0) {
                return I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".constructor.default",
                    constructorName);
            }
            List<ASTFormalParameter> formalParameters = decl.getFormalParameters()
                .findChildrenOfType(ASTFormalParameter.class);
            List<String> strings = new ArrayList<>(formalParameters.size());

            for (ASTFormalParameter formalParameter : formalParameters) {
                strings.add(formalParameter.jjtGetFirstToken().toString() + " "
                    + formalParameter.jjtGetLastToken().toString());
            }
            return I18nResources
                .getMessage(MESSAGE_KEY_PREFIX + ".constructor.parameter",
                    constructorName,
                    StringUtils.join(strings, ","));
        });
        return super.visit(decl, data);
    }

    @Override
    public Object visit(final ASTMethodDeclaration decl, Object data) {
        checkComment(decl, data, () -> I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".method",
            decl.getMethodName()));
        return super.visit(decl, data);
    }

    @Override
    public Object visit(final ASTFieldDeclaration decl, Object data) {
        checkComment(decl, data, () -> I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".field",
            VariableUtils.getVariableName(decl)));
        return super.visit(decl, data);
    }

    @Override
    public Object visit(final ASTEnumDeclaration decl, Object data) {
        checkComment(decl, data, () -> I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".enum",
            decl.getImage()));
        return super.visit(decl, data);
    }

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        assignCommentsToDeclarations(cUnit);

        return super.visit(cUnit, data);
    }

    private void checkComment(AbstractJavaAccessNode decl, Object data, MessageMaker maker) {
        Comment comment = decl.comment();
        if (comment instanceof SingleLineComment || comment instanceof MultiLineComment) {
            addViolationWithMessage(data, decl,
                maker.make(), comment.getBeginLine(), comment.getEndLine());
        }
    }

    @Override
    protected void assignCommentsToDeclarations(ASTCompilationUnit cUnit) {

        SortedMap<Integer, Node> itemsByLineNumber = orderedComments(cUnit);
        Comment lastComment = null;
        AbstractJavaNode lastNode = null;

        for (Entry<Integer, Node> entry : itemsByLineNumber.entrySet()) {
            Node value = entry.getValue();

            if (value instanceof AbstractJavaNode) {
                AbstractJavaNode node = (AbstractJavaNode)value;

                // skip annotation node, we will deal with it later.
                if (node instanceof ASTAnnotation) {
                    continue;
                }

                // Check if comment is one line above class, field, method.
                if (lastComment != null && isCommentOneLineBefore(itemsByLineNumber, lastComment, lastNode, node)) {
                    node.comment(lastComment);
                    lastComment = null;
                }

                lastNode = node;
            } else if (value instanceof Comment) {
                lastComment = (Comment)value;
            }
        }
    }

    protected SortedMap<Integer, Node> orderedComments(ASTCompilationUnit cUnit) {

        SortedMap<Integer, Node> itemsByLineNumber = new TreeMap<>();

        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, cUnit.getComments());

        List<ASTAnnotation> annotations = cUnit.findDescendantsOfType(ASTAnnotation.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, annotations);

        List<ASTClassOrInterfaceDeclaration> classDecl =
            cUnit.findDescendantsOfType(ASTClassOrInterfaceDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, classDecl);

        List<ASTFieldDeclaration> fields = cUnit.findDescendantsOfType(ASTFieldDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, fields);

        List<ASTMethodDeclaration> methods = cUnit.findDescendantsOfType(ASTMethodDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, methods);

        List<ASTConstructorDeclaration> constructors = cUnit.findDescendantsOfType(ASTConstructorDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, constructors);

        List<ASTEnumDeclaration> enumDecl = cUnit.findDescendantsOfType(ASTEnumDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, enumDecl);

        return itemsByLineNumber;
    }

    private boolean isCommentOneLineBefore(SortedMap<Integer, Node> items, Comment lastComment, Node lastNode, Node node) {
        ASTClassOrInterfaceBodyDeclaration parentClass =
            node.getFirstParentOfType(ASTClassOrInterfaceBodyDeclaration.class);

        // Skip comments inside inner class.
        if (parentClass != null && parentClass.isAnonymousInnerClass()) {
            return false;
        }

        // Skip comments behind nodes.
        if (lastNode != null && lastNode.getEndLine() == lastComment.getEndLine()) {
            return false;
        }

        // check if there is nothing in the middle except annotations.
        SortedMap<Integer, Node> subMap = items.subMap(NodeSortUtils.generateIndex(lastComment),
            NodeSortUtils.generateIndex(node));
        Iterator<Entry<Integer, Node>> iter = subMap.entrySet().iterator();

        // skip the first comment node.
        iter.next();
        int lastEndLine = lastComment.getEndLine();

        while (iter.hasNext()) {
            Entry<Integer, Node> entry = iter.next();
            Node value = entry.getValue();

            // only annotation node is allowed between comment and node.
            if (!(value instanceof ASTAnnotation)) {
                return false;
            }

            // allow annotation node after comment.
            if (lastEndLine + 1 == value.getBeginLine()) {
                lastEndLine = value.getEndLine();
            }
        }

        return lastEndLine + 1 == node.getBeginLine();
    }

    /**
     * Generate rule violation message.
     */
    interface MessageMaker {
        /**
         * Generate violation message.
         *
         * @return message
         */
        String make();
    }
}
