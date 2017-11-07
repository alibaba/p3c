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
import java.util.regex.Pattern;

import com.alibaba.p3c.pmd.lang.java.rule.util.NodeSortUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.Comment;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

/**
 * [Recommended] Codes or configuration that is noticed to be obsoleted should be resolutely removed from projects.
 *
 * @author keriezhang
 * @date 2017/04/14
 */
public class RemoveCommentedCodeRule extends AbstractAliCommentRule {

    private static final Pattern SUPPRESS_PATTERN = Pattern.compile("\\s*///.*", Pattern.DOTALL);

    private static final Pattern PRE_TAG_PATTERN = Pattern.compile(".*<pre>.*", Pattern.DOTALL);

    private static final Pattern IMPORT_PATTERN = Pattern.compile(".*import\\s(static\\s)?(\\w*\\.)*\\w*;.*",
        Pattern.DOTALL);

    private static final Pattern FIELD_PATTERN = Pattern.compile(".*private\\s+(\\w*)\\s+(\\w*);.*", Pattern.DOTALL);

    private static final Pattern METHOD_PATTERN = Pattern.compile(
        ".*(public|protected|private)\\s+\\w+\\s+\\w+\\(.*\\)\\s+\\{.*", Pattern.DOTALL);

    /**
     * If string matches format ".xxx(.*);\n", then mark it as code.
     */
    private static final Pattern STATEMENT_PATTERN = Pattern.compile(".*\\.\\w+\\(.*\\);\n.*", Pattern.DOTALL);

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        checkCommentsBetweenDeclarations(cUnit, data);

        return super.visit(cUnit, data);
    }

    protected void checkCommentsBetweenDeclarations(ASTCompilationUnit cUnit, Object data) {

        SortedMap<Integer, Node> itemsByLineNumber = orderedCommentsAndDeclarations(cUnit);
        Comment lastComment = null;
        boolean suppressWarning = false;
        CommentPatternEnum commentPattern = CommentPatternEnum.NONE;

        for (Entry<Integer, Node> entry : itemsByLineNumber.entrySet()) {
            Node value = entry.getValue();

            if (value instanceof JavaNode) {
                JavaNode node = (JavaNode)value;

                // add violation on the node after comment.
                if (lastComment != null && isCommentBefore(lastComment, node)) {
                    // find code comment, but need filter some case.
                    if (!CommentPatternEnum.NONE.equals(commentPattern)) {
                        // check statement pattern only in method
                        boolean statementOutsideMethod = CommentPatternEnum.STATEMENT.equals(commentPattern)
                            && !(node instanceof ASTBlockStatement);
                        if (!statementOutsideMethod) {
                            addViolationWithMessage(data, node, getMessage(),
                                lastComment.getBeginLine(),
                                lastComment.getEndLine());
                        }
                    }
                    lastComment = null;
                }

                // reset data after each node.
                suppressWarning = false;
                commentPattern = CommentPatternEnum.NONE;

            } else if (value instanceof Comment) {
                lastComment = (Comment)value;
                String content = lastComment.getImage();

                if (!suppressWarning) {
                    suppressWarning = SUPPRESS_PATTERN.matcher(content).matches();
                }

                if (!suppressWarning && CommentPatternEnum.NONE.equals(commentPattern)) {
                    commentPattern = this.scanCommentedCode(content);
                }
            }
        }
    }

    /**
     * Common Situations, check in following order:
     * 1. commented import
     * 2. commented field
     * 3. commented method
     * 4. commented statement
     *
     * @param content comment content
     * @return check result
     */
    protected CommentPatternEnum scanCommentedCode(String content) {
        CommentPatternEnum pattern = CommentPatternEnum.NONE;

        // Skip comment which contains pre tag.
        if (PRE_TAG_PATTERN.matcher(content).matches()) {
            return pattern;
        }

        if (IMPORT_PATTERN.matcher(content).matches()) {
            pattern = CommentPatternEnum.IMPORT;
        } else if (FIELD_PATTERN.matcher(content).matches()) {
            pattern = CommentPatternEnum.FIELD;
        } else if (METHOD_PATTERN.matcher(content).matches()) {
            pattern = CommentPatternEnum.METHOD;
        } else if (STATEMENT_PATTERN.matcher(content).matches()) {
            pattern = CommentPatternEnum.STATEMENT;
        }

        return pattern;
    }

    @Override
    protected SortedMap<Integer, Node> orderedCommentsAndDeclarations(ASTCompilationUnit cUnit) {
        SortedMap<Integer, Node> itemsByLineNumber = new TreeMap<>();

        List<ASTImportDeclaration> importDecl = cUnit
            .findDescendantsOfType(ASTImportDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, importDecl);

        List<ASTClassOrInterfaceDeclaration> classDecl = cUnit
            .findDescendantsOfType(ASTClassOrInterfaceDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, classDecl);

        List<ASTFieldDeclaration> fields = cUnit.findDescendantsOfType(ASTFieldDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, fields);

        List<ASTMethodDeclaration> methods = cUnit.findDescendantsOfType(ASTMethodDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, methods);

        List<ASTConstructorDeclaration> constructors = cUnit.findDescendantsOfType(ASTConstructorDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, constructors);

        List<ASTBlockStatement> blockStatements = cUnit.findDescendantsOfType(ASTBlockStatement.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, blockStatements);

        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, cUnit.getComments());

        return itemsByLineNumber;
    }

    private boolean isCommentBefore(Comment n1, Node n2) {
        return n1.getEndLine() < n2.getBeginLine() || n1.getEndLine() == n2.getBeginLine()
            && n1.getEndColumn() < n2.getBeginColumn();
    }

    enum CommentPatternEnum {
        /**
         * comment has code pattern
         */
        IMPORT,
        FIELD,
        METHOD,
        STATEMENT,
        NONE
    }

}
