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
package com.alibaba.p3c.pmd.lang.java.rule;

import java.util.List;

import com.alibaba.p3c.pmd.lang.java.util.PojoUtils;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;

/**
 * Base class for POJO
 *
 * @author zenghou.fw
 * @date 2016/11/25
 */
public abstract class AbstractPojoRule extends AbstractAliRule {

    /**
     * filter for all POJO class,skip if no POJO.
     * consider inner class
     *
     * @param node compilation unit
     * @param data rule context
     * @return result
     */
    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        // proceed if contains POJO
        if (hasPojoInJavaFile(node)) {
            return super.visit(node, data);
        }
        return data;
    }

    /**
     * check contains POJO
     * @param node compilation unit
     * @return
     */
    private boolean hasPojoInJavaFile(ASTCompilationUnit node) {
        List<ASTClassOrInterfaceDeclaration> klasses = node.findDescendantsOfType(
                ASTClassOrInterfaceDeclaration.class);
        for (ASTClassOrInterfaceDeclaration klass : klasses) {
            if (isPojo(klass)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isPojo(ASTClassOrInterfaceDeclaration node) {
        return PojoUtils.isPojo(node);
    }
}
