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
package com.alibaba.p3c.pmd.lang.java.rule.orm;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.VariableUtils;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;

/**
 * [Mandatory] iBatis built in queryForList(String statementName, int start, int size) is not recommended.
 * Note: It may lead to OOM issue because its implementation is to retrieve all DB records of statementName's
 * corresponding SQL statement, then start, size subset is applied through subList.
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class IbatisMethodQueryForListRule extends AbstractAliRule {
    private static final String SQL_MAP_CLIENT_IMPORT_FULL_NAME = "com.ibatis.sqlmap.client.SqlMapClient";
    private static final String SQL_MAP_CLIENT_IMPORT_SIMPLE_NAME = "com.ibatis.sqlmap.client.*";
    private static final String SQL_MAP_CLIENT_NAME = "SqlMapClient";
    private static final String IBATIS_QUERY_FOR_LIST_METHOD_NAME = ".queryForList";
    private static final String PRIMARY_METHOD_NAME_XPATH = "PrimaryPrefix/Name";
    private static final String PRIMARY_METHOD_ARGUMENT_XPATH
        = "PrimarySuffix/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Literal";
    private static final String FIELDS_XPATH = "ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/FieldDeclaration";
    private static final int LITERALS_SIZE = 3;

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        boolean hasImportSqlMapClient = hasSqlMapClientImport(node.findChildrenOfType(ASTImportDeclaration.class));
        if (!hasImportSqlMapClient) {
            return super.visit(node, data);
        }
        List<ASTClassOrInterfaceDeclaration> classOrInterfaceDeclarations
            = node.findDescendantsOfType(ASTClassOrInterfaceDeclaration.class);
        if (classOrInterfaceDeclarations == null || classOrInterfaceDeclarations.isEmpty()) {
            return super.visit(node, data);
        }
        for (ASTClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
            visitAstClassOrInterfaceDeclaration(classOrInterfaceDeclaration, data);
        }
        return super.visit(node, data);
    }

    private void visitAstClassOrInterfaceDeclaration(ASTClassOrInterfaceDeclaration classOrInterfaceDeclaration,
        Object data) {
        try {
            List<Node> fieldDeclarations = classOrInterfaceDeclaration.findChildNodesWithXPath(FIELDS_XPATH);
            Set<String> sqlMapFields = getSqlMapFields(fieldDeclarations);
            if (sqlMapFields.isEmpty()) {
                return;
            }
            List<ASTPrimaryExpression> primaryExpressions = classOrInterfaceDeclaration.findDescendantsOfType(
                ASTPrimaryExpression.class);
            for (ASTPrimaryExpression primaryExpression : primaryExpressions) {
                visitPrimaryExpression(primaryExpression, data, sqlMapFields);
            }
        } catch (JaxenException ignored) {
        }
    }

    private Set<String> getSqlMapFields(List<Node> fieldDeclarations) {
        if (fieldDeclarations == null || fieldDeclarations.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        for (Node node : fieldDeclarations) {
            ASTFieldDeclaration fieldDeclaration = (ASTFieldDeclaration)node;
            if (sqlMapClientField(fieldDeclaration)) {
                set.add(VariableUtils.getVariableName(fieldDeclaration));
            }
        }
        return set;
    }

    /**
     * if there is no introduction of related packages, skip the inspection
     * The import statement is generally at the start of the class, do some cleaning
     */
    private boolean hasSqlMapClientImport(List<ASTImportDeclaration> importDeclarations) {
        if (importDeclarations == null || importDeclarations.isEmpty()) {
            return false;
        }
        for (ASTImportDeclaration importDeclaration : importDeclarations) {
            ASTName astName = importDeclaration.getFirstChildOfType(ASTName.class);
            boolean hasImport = astName != null && (SQL_MAP_CLIENT_IMPORT_FULL_NAME.equals(astName.getImage())
                || SQL_MAP_CLIENT_IMPORT_SIMPLE_NAME.equals(astName.getImage()));
            if (hasImport) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if any method matches queryForList(String statementName,int start,int size)
     */
    private void visitPrimaryExpression(ASTPrimaryExpression node, Object data,
        Set<String> sqlMapFields) throws JaxenException {
        List<Node> astNames = node.findChildNodesWithXPath(PRIMARY_METHOD_NAME_XPATH);
        for (Node astName : astNames) {
            String methodName = astName.getImage();
            //method name not match
            if (!(StringUtils.isNotEmpty(methodName) && methodName.contains(
                IBATIS_QUERY_FOR_LIST_METHOD_NAME))) {
                continue;
            }
            String methodInvokeName = methodName.substring(0,
                methodName.indexOf(IBATIS_QUERY_FOR_LIST_METHOD_NAME));
            //the method caller is empty
            if (StringUtils.isEmpty(methodInvokeName)) {
                continue;
            }

            //the method caller not matches SqlMapClient
            if (!sqlMapFields.contains(methodInvokeName)) {
                continue;
            }
            //method parameters not match
            List<Node> literals = node.findChildNodesWithXPath(PRIMARY_METHOD_ARGUMENT_XPATH);
            if (literals == null || (literals.size() != LITERALS_SIZE)) {
                continue;
            }
            boolean firstMethodArgumentString = "java.lang.String".equals(
                ((ASTLiteral)(literals.get(0))).getType().getName());
            boolean secondMethodArgumentInt = "int".equals(
                ((ASTLiteral)(literals.get(1))).getType().getName());
            boolean thirdMethodArgumentInt = "int".equals(
                ((ASTLiteral)(literals.get(2))).getType().getName());
            //if the parameter name and method name all matching, that is a violation of the rules
            if (firstMethodArgumentString && secondMethodArgumentInt
                && thirdMethodArgumentInt) {
                ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                    I18nResources.getMessage("java.naming.IbatisMethodQueryForListRule.violation.msg"));
            }

        }
    }

    /**
     * if the attributes of a class defines the SqlMapClient object,collect these object name
     */
    private boolean sqlMapClientField(ASTFieldDeclaration node) {
        try {
            List<Node> astClassOrInterfaceTypes = node.findChildNodesWithXPath(
                "Type/ReferenceType/ClassOrInterfaceType");
            //find  the SqlMapClient attribute node, collect these node's parent to sqlMapClientTypeFieldList
            for (Node astClassOrInterfaceType : astClassOrInterfaceTypes) {
                String fieldTypeName = astClassOrInterfaceType.getImage();
                if (SQL_MAP_CLIENT_NAME.equals(fieldTypeName)
                    || SQL_MAP_CLIENT_IMPORT_FULL_NAME.equals(fieldTypeName)) {
                    return true;
                }
            }
        } catch (JaxenException ignore) {
        }
        return false;

    }
}
