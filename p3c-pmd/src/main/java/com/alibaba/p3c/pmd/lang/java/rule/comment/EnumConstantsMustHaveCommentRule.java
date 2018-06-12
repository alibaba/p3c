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
import net.sourceforge.pmd.lang.java.ast.ASTEnumDeclaration;

/**
 * [Mandatory] All enumeration type fields should be commented as Javadoc style.
 *
 * @author keriezhang
 * @date 2016/12/14
 */
public class EnumConstantsMustHaveCommentRule extends AbstractAliCommentRule {

    @Override
    public Object visit(ASTCompilationUnit cUnit, Object data) {
        SortedMap<Integer, Node> itemsByLineNumber = this.orderedCommentsAndEnumDeclarations(cUnit);

        // Check comments between ASTEnumDeclaration and ASTEnumConstant.
        boolean isPreviousEnumDecl = false;

        for (Entry<Integer, Node> entry : itemsByLineNumber.entrySet()) {
            Node value = entry.getValue();

            if (value instanceof ASTEnumDeclaration) {
                isPreviousEnumDecl = true;
            } else if (value instanceof ASTEnumConstant && isPreviousEnumDecl) {
                Node enumBody = value.jjtGetParent();
                Node enumDeclaration = enumBody.jjtGetParent();
                addViolationWithMessage(data, enumBody,
                    I18nResources.getMessage("java.comment.EnumConstantsMustHaveCommentRule.violation.msg",
                        enumDeclaration.getImage()));
                isPreviousEnumDecl = false;
            } else {
                isPreviousEnumDecl = false;
            }
        }

        return super.visit(cUnit, data);
    }

    private SortedMap<Integer, Node> orderedCommentsAndEnumDeclarations(ASTCompilationUnit cUnit) {
        SortedMap<Integer, Node> itemsByLineNumber = new TreeMap<>();

        List<ASTEnumDeclaration> enumDecl = cUnit.findDescendantsOfType(ASTEnumDeclaration.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, enumDecl);

        List<ASTEnumConstant> contantDecl = cUnit.findDescendantsOfType(ASTEnumConstant.class);
        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, contantDecl);

        NodeSortUtils.addNodesToSortedMap(itemsByLineNumber, cUnit.getComments());

        return itemsByLineNumber;
    }

}
