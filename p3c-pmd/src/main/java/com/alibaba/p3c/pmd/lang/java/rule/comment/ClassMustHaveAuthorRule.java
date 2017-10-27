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

import java.util.regex.Pattern;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.ast.Comment;

/**
 * [Mandatory] Every class should include information of author(s) and date.
 *
 * @author keriezhang
 * @date 2016/12/14
 */
public class ClassMustHaveAuthorRule extends AbstractAliCommentRule {

    private static final Pattern AUTHOR_PATTERN = Pattern.compile(".*@author.*",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final String MESSAGE_KEY_PREFIX = "java.comment.ClassMustHaveAuthorRule.violation.msg";

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration decl, Object data) {
        // Exclude nested classes
        if (decl.isNested()) {
            return super.visit(decl, data);
        }

        // Exclude inner classes
        if (!decl.isPublic()) {
            return super.visit(decl, data);
        }

        checkAuthorComment(decl, data);

        return super.visit(decl, data);
    }

    @Override
    public Object visit(ASTEnumDeclaration decl, Object data) {
        // Exclude inner enum
        if (!decl.isPublic()) {
            return super.visit(decl, data);
        }

        // Inner enum should have author tag in outer class.
        ASTClassOrInterfaceDeclaration parent = decl.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        if (parent != null) {
            return super.visit(decl, data);
        }

        checkAuthorComment(decl, data);

        return super.visit(decl, data);
    }

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        assignCommentsToDeclarations(cUnit);

        return super.visit(cUnit, data);
    }

    /**
     * Check if node's comment contains author tag.
     *
     * @param decl node
     * @param data ruleContext
     */
    public void checkAuthorComment(AbstractJavaNode decl, Object data) {
        Comment comment = decl.comment();
        if (null == comment) {
            ViolationUtils.addViolationWithPrecisePosition(this, decl, data,
                I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".comment", decl.getImage()));
        } else {
            String commentContent = comment.getImage();
            boolean hasAuthor = AUTHOR_PATTERN.matcher(commentContent).matches();
            if (!hasAuthor) {
                ViolationUtils.addViolationWithPrecisePosition(this, decl, data,
                    I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".author", decl.getImage()));
            }
        }
    }
}
