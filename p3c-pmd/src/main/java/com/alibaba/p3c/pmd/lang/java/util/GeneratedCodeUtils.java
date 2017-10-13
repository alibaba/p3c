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
package com.alibaba.p3c.pmd.lang.java.util;

import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;

/**
 * @author caikang
 * @date 2017/06/21
 */
public class GeneratedCodeUtils {
    private static final String ANNOTATION_NAME = "javax.annotation.Generated";

    private static final String CLASS = "class";

    public static boolean isGenerated(ASTCompilationUnit compilationUnit) {
        List<ASTImportDeclaration> importDeclarationList
            = compilationUnit.findChildrenOfType(ASTImportDeclaration.class);
        if (importDeclarationList.isEmpty()) {
            return false;
        }

        for (ASTImportDeclaration importDeclaration : importDeclarationList) {
            if (ANNOTATION_NAME.equals(importDeclaration.getImportedName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGenerated(String content) {
        int classIndex = content.indexOf(CLASS);
        if (classIndex <= 1) {
            return false;
        }
        //most of file is not generated
        String importHeader = content.substring(0, classIndex);
        return importHeader.contains(ANNOTATION_NAME);
    }
}

