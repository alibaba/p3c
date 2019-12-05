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
package com.alibaba.p3c.pmd.lang.java.rule.constant;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTLiteral;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;

/**
 * [Mandatory] Magic values, except for predefined, are forbidden in coding.
 *
 * @author shengfang.gsf
 * @date 2016/12/13
 */
public class UndefineMagicConstantRule extends AbstractAliRule {

    /**
     * white list for undefined variable, may be added
     */
    private final static List<String> LITERAL_WHITE_LIST = NameListConfig.NAME_LIST_SERVICE.getNameList(
        UndefineMagicConstantRule.class.getSimpleName(), "LITERAL_WHITE_LIST");

    private final static String XPATH = "//Literal/../../../../..[not(VariableInitializer)]";

    /**
     * An undefined that belongs to non-looped if statements
     *
     * @param node compilation unit
     * @param data rule context
     */
    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        // removed repeat magic value , to prevent the parent class to find sub-variable nodes when there is a repeat
        List<ASTLiteral> currentLiterals = new ArrayList<ASTLiteral>();
        try {
            // Find the parent node of the undefined variable
            List<Node> parentNodes = node.findChildNodesWithXPath(XPATH);

            for (Node parentItem : parentNodes) {
                List<ASTLiteral> literals = parentItem.findDescendantsOfType(ASTLiteral.class);
                for (ASTLiteral literal : literals) {
                    if (inBlackList(literal) && !currentLiterals.contains(literal)) {
                        currentLiterals.add(literal);
                        String imageReplace = StringUtils.replace(literal.getImage(), "{", "'{");
                        addViolationWithMessage(data, literal,
                            "java.constant.UndefineMagicConstantRule.violation.msg", new Object[] {imageReplace});
                    }
                }
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }
        return super.visit(node, data);
    }

    /**
     * Undefined variables are in the blacklist
     *
     * @param literal
     * @return
     */
    private boolean inBlackList(ASTLiteral literal) {
        String name = literal.getImage();
        int lineNum = literal.getBeginLine();
        // name is null,bool literal，belongs to white list
        if (name == null) {
            return false;
        }
        // filter white list
        for (String whiteItem : LITERAL_WHITE_LIST) {
            if (whiteItem.equals(name)) {
                return false;
            }
        }
        ASTIfStatement ifStatement = literal.getFirstParentOfType(ASTIfStatement.class);
        if (ifStatement != null && lineNum == ifStatement.getBeginLine()) {
            ASTForStatement forStatement = ifStatement.getFirstParentOfType(ASTForStatement.class);
            ASTWhileStatement whileStatement = ifStatement.getFirstParentOfType(ASTWhileStatement.class);
            return forStatement == null && whileStatement == null;
        }

        // judge magic value belongs to  for statement 
        ASTForStatement blackForStatement = literal.getFirstParentOfType(ASTForStatement.class);
        if (blackForStatement != null && lineNum == blackForStatement.getBeginLine()) {
            return true;
        }

        // judge magic value belongs to while statement
        ASTWhileStatement blackWhileStatement = literal.getFirstParentOfType(ASTWhileStatement.class);
        return blackWhileStatement != null && lineNum == blackWhileStatement.getBeginLine();
    }
}
