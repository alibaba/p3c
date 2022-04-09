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
package com.xenoamess.p3c.pmd.lang.java.util.namelist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author changle.lq
 * @date 2017/03/23
 */
public interface NameListService {

    /**
     * get name list
     * @param className class name
     * @param name type name
     * @return name list
     */
    List<String> getNameList(String className, String name);

    /**
     * get config
     * @param className class name
     * @param name type name
     * @return name list
     */
    Map<String, String> getNameMap(String className, String name);

    /**
     * patch config from a patch file.
     * @param file additional config file
     */
    void loadPatchConfigFile(
            @NotNull File file
    );

    /**
     * check if a rule is in rule black list.
     * @param ruleClass rule class
     * @return true if in rule black list
     */
    boolean ifRuleClassInRuleBlackList(Class ruleClass);

    /**
     * check if class name is in class black list.
     * @param className class name
     * @return true if in class black list
     */
    boolean ifClassNameInClassBlackList(String className);

    /**
     * check if package name is in package black list.
     * @param packageName package name
     * @return true if in package black list
     */
    boolean ifPackageNameInPackageBlackList(@Nullable String packageName);

    /**
     * check if class name is in class name pair black list.
     * @param ruleClass rule class
     * @param className class name
     * @return true if in class black list
     */
    boolean ifRuleClassNameClassNamePairInPairIgnoreList(Class ruleClass, String className);
}
