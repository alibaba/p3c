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
import java.util.regex.Pattern;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTNameList;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.Comment;
import net.sourceforge.pmd.lang.java.ast.FormalComment;
import org.jaxen.JaxenException;

/**
 * [Mandatory] Abstract methods (including methods in interface) should be commented by Javadoc.
 * Javadoc should include method instruction, description of parameters, return values and possible exception.
 *
 * @author keriezhang
 * @date 2016/12/14
 */
public class AbstractMethodOrInterfaceMethodMustUseJavadocRule extends AbstractAliCommentRule {

    private static final String METHOD_IN_INTERFACE_XPATH =
        "./ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/MethodDeclaration";
    private static final String METHOD_VARIABLE_DECLARATOR_XPATH
        = "./MethodDeclarator/FormalParameters/FormalParameter/VariableDeclaratorId";

    private static final String MESSAGE_KEY_PREFIX
        = "java.comment.AbstractMethodOrInterfaceMethodMustUseJavadocRule.violation.msg";

    private static final Pattern EMPTY_CONTENT_PATTERN = Pattern.compile("[/*\\n\\r\\s]+(@.*)?", Pattern.DOTALL);
    private static final Pattern RETURN_PATTERN = Pattern.compile(".*@return.*", Pattern.DOTALL);

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration decl, Object data) {
        if (decl.isAbstract()) {
            List<ASTMethodDeclaration> methods = decl.findDescendantsOfType(ASTMethodDeclaration.class);
            for (ASTMethodDeclaration method : methods) {
                if (!method.isAbstract()) {
                    continue;
                }
                Comment comment = method.comment();
                if (null == comment || !(comment instanceof FormalComment)) {
                    ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                        I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".abstract",
                            method.getMethodName()));
                } else {
                    this.checkMethodCommentFormat(method, data);
                }
            }
        }
        if (!decl.isInterface()) {
            return super.visit(decl, data);
        }
        List<Node> methodNodes;
        try {
            methodNodes = decl.findChildNodesWithXPath(METHOD_IN_INTERFACE_XPATH);
        } catch (JaxenException e) {
            throw new RuntimeException("XPath expression " + METHOD_IN_INTERFACE_XPATH
                + " failed: " + e.getLocalizedMessage(), e);
        }

        for (Node node : methodNodes) {
            ASTMethodDeclaration method = (ASTMethodDeclaration)node;
            Comment comment = method.comment();
            if (null == comment || !(comment instanceof FormalComment)) {
                ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                    I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".interface",
                        method.getMethodName()));
            } else {
                this.checkMethodCommentFormat(method, data);
            }
        }
        return super.visit(decl, data);
    }

    public void checkMethodCommentFormat(ASTMethodDeclaration method, Object data) {
        Comment comment = method.comment();
        String commentContent = comment.getImage();

        // method instruction
        if (EMPTY_CONTENT_PATTERN.matcher(commentContent).matches()) {
            ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".desc",
                    method.getMethodName()));
        }

        // description of parameters
        List<Node> variableDeclaratorIds;
        try {
            variableDeclaratorIds = method.findChildNodesWithXPath(METHOD_VARIABLE_DECLARATOR_XPATH);
        } catch (JaxenException e) {
            throw new RuntimeException(
                "XPath expression " + METHOD_VARIABLE_DECLARATOR_XPATH + " failed: " + e.getLocalizedMessage(), e);
        }

        for (Node variableDeclaratorId : variableDeclaratorIds) {
            ASTVariableDeclaratorId param = (ASTVariableDeclaratorId)variableDeclaratorId;
            String paramName = param.getImage();
            Pattern paramNamePattern = Pattern.compile(".*@param\\s+" + paramName + ".*", Pattern.DOTALL);

            if (!paramNamePattern.matcher(commentContent).matches()) {
                ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                    I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".parameter",
                        method.getMethodName(), paramName));
            }
        }

        // return values
        if (!method.isVoid() && !RETURN_PATTERN.matcher(commentContent).matches()) {

            ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".return",
                    method.getMethodName()));
        }

        // possible exception
        ASTNameList nameList = method.getThrows();
        if (null != nameList) {
            List<ASTName> exceptions = nameList.findDescendantsOfType(ASTName.class);
            for (ASTName exception : exceptions) {
                String exceptionName = exception.getImage();
                Pattern exceptionPattern = Pattern.compile(".*@throws\\s+"
                    + exceptionName + ".*", Pattern.DOTALL);

                if (!exceptionPattern.matcher(commentContent).matches()) {
                    ViolationUtils.addViolationWithPrecisePosition(this, method, data,
                        I18nResources.getMessage(MESSAGE_KEY_PREFIX + ".exception",
                            method.getMethodName(), exceptionName));
                }
            }
        }
    }

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        assignCommentsToDeclarations(cUnit);
        return super.visit(cUnit, data);
    }

}
