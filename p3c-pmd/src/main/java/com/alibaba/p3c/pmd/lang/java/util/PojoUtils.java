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

import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;

/**
 * POJO Utils
 *
 * @author zenghou.fw
 * @date 2016/11/25
 */
public class PojoUtils {
    private static final List<String> POJO_SUFFIX_SET =
        NameListConfig.NAME_LIST_SERVICE.getNameList("PojoMustOverrideToStringRule", "POJO_SUFFIX_SET");

    private PojoUtils() {
    }

    public static boolean isPojo(String klass) {
        if (klass == null) {
            return false;
        }
        for (String suffix : POJO_SUFFIX_SET) {
            if (klass.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPojo(ASTClassOrInterfaceDeclaration node) {
        return node != null && isPojo(node.getImage());
    }

}
