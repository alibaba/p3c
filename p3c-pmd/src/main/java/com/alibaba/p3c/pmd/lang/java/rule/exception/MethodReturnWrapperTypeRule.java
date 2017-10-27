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
package com.alibaba.p3c.pmd.lang.java.rule.exception;

import java.util.List;
import java.util.Map;

import com.alibaba.p3c.pmd.I18nResources;
import com.alibaba.p3c.pmd.lang.java.rule.AbstractAliRule;
import com.alibaba.p3c.pmd.lang.java.util.ViolationUtils;
import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import org.jaxen.JaxenException;

/**
 * [Recommended] If the return type is primitive, return a value of wrapper class may cause NullPointerException.
 *
 * @author changle.lq
 * @date 2017/04/16
 */
public class MethodReturnWrapperTypeRule extends AbstractAliRule {
    private static final Map<String, String> PRIMITIVE_TYPE_TO_WAPPER_TYPE = NameListConfig.NAME_LIST_SERVICE
        .getNameMap("MethodReturnWrapperTypeRule", "PRIMITIVE_TYPE_TO_WAPPER_TYPE", String.class, String.class);
    private static final String METHOD_RETURN_TYPE_XPATH = "ResultType/Type/PrimitiveType";
    private static final String METHOD_RETURN_OBJECT_XPATH
        = "Block/BlockStatement/Statement/ReturnStatement/Expression/PrimaryExpression/PrimaryPrefix/Name";
    private static final String METHOD_VARIABLE_TYPE_XPATH = "Type/ReferenceType/ClassOrInterfaceType";
    private static final String METHOD_VARIABLE_NAME_XPATH
        = "Block/BlockStatement/LocalVariableDeclaration/VariableDeclarator/VariableDeclaratorId";

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        try {
            List<Node> astPrimitiveTypeList = node.findChildNodesWithXPath(METHOD_RETURN_TYPE_XPATH);
            //check the method return type
            if (!(astPrimitiveTypeList != null && astPrimitiveTypeList.size() == 1)) {
                return super.visit(node, data);
            }
            ASTPrimitiveType astPrimitiveType = (ASTPrimitiveType)astPrimitiveTypeList.get(0);
            //If the return type is not a basic types,skip
            if (!(astPrimitiveType.getType() != null && astPrimitiveType.getType().isPrimitive())) {
                return super.visit(node, data);
            }
            //the return type
            String primitiveTypeName = astPrimitiveType.getType().getName();
            //the return node
            List<Node> nameList = node.findChildNodesWithXPath(METHOD_RETURN_OBJECT_XPATH);
            if (nameList == null || nameList.size() != 1) {
                return super.visit(node, data);
            }
            //if the local variable is empty,skip
            List<Node> methodVariableNameList = node.findChildNodesWithXPath(METHOD_VARIABLE_NAME_XPATH);
            if (methodVariableNameList == null || methodVariableNameList.size() == 0) {
                return super.visit(node, data);
            }
            ASTName astName = (ASTName)nameList.get(0);
            String variableName = astName.getImage();
            //iterate all the method of variable nodes
            for (Node methodVariableNameNode : methodVariableNameList) {
                ASTVariableDeclaratorId astVariableDeclaratorId
                    = (ASTVariableDeclaratorId)methodVariableNameNode;
                //find out the variable named the same with return node
                if (!variableName.equals(astVariableDeclaratorId.getImage())) {
                    continue;
                }
                ASTLocalVariableDeclaration astLocalVariableDeclaration = astVariableDeclaratorId
                    .getFirstParentOfType(ASTLocalVariableDeclaration.class);
                //check local variables type
                List<Node> nodeList = astLocalVariableDeclaration.findChildNodesWithXPath(
                    METHOD_VARIABLE_TYPE_XPATH);

                if (nodeList != null && nodeList.size() == 1) {
                    ASTClassOrInterfaceType astClassOrInterfaceType = (ASTClassOrInterfaceType)nodeList.get(
                        0);
                    //if variable type is a value of wrapper
                    if (PRIMITIVE_TYPE_TO_WAPPER_TYPE.get(primitiveTypeName) != null
                        && PRIMITIVE_TYPE_TO_WAPPER_TYPE.get(primitiveTypeName).equals(
                        astClassOrInterfaceType.getType().getSimpleName())) {
                        ViolationUtils.addViolationWithPrecisePosition(this, node, data,
                            I18nResources.getMessage("java.exception.MethodReturnWrapperTypeRule.violation.msg",
                                primitiveTypeName, astClassOrInterfaceType.getType().getSimpleName()));
                    }
                }
            }
        } catch (JaxenException e) {
            return super.visit(node, data);
        }
        return super.visit(node, data);
    }
}
