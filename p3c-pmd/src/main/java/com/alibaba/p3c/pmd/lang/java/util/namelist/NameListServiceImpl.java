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
package com.alibaba.p3c.pmd.lang.java.util.namelist;

import com.alibaba.p3c.pmd.config.P3cConfigDataBean;
import com.xenoamess.x8l.ContentNode;
import com.xenoamess.x8l.X8lTree;
import com.xenoamess.x8l.databind.X8lDataBeanFieldScheme;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.xenoamess.x8l.databind.X8lDataBeanDefaultParser.getLastFromList;

/**
 * @author changle.lq
 * @date 2017/03/27
 */
public class NameListServiceImpl implements NameListService {

    public static final String P3C_CONFIG_FILE_NAME = "p3c_config.x8l";
    public static final String DEFAULT_P3C_CONFIG_FILE_NAME = "p3c_config.default.x8l";
    private final P3cConfigDataBean p3cConfigDataBean;

    public NameListServiceImpl() {
        this(true);
    }

    public NameListServiceImpl(boolean ifLoadCustomerConfigX8lTree) {
        p3cConfigDataBean = initP3cConfigDataBean(ifLoadCustomerConfigX8lTree);
    }

    private static P3cConfigDataBean initP3cConfigDataBean(boolean ifLoadCustomerConfigX8lTreeLocal) {
        P3cConfigDataBean p3cConfigDataBeanLocal = new P3cConfigDataBean();
        try {
            try (InputStream inputStream =
                         NameListServiceImpl.class.getClassLoader()
                                 .getResourceAsStream(DEFAULT_P3C_CONFIG_FILE_NAME);
                 InputStream bufferedInputStream = IOUtils.toBufferedInputStream(inputStream)
            ) {
                p3cConfigDataBeanLocal.setP3cConfigX8lTree(X8lTree.load(bufferedInputStream));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Load p3c_config.default.x8l fail", ex);
        }

        if (ifLoadCustomerConfigX8lTreeLocal) {
            p3cConfigDataBeanLocal.tryPatchP3cConfigDataBean(new File(P3C_CONFIG_FILE_NAME));
        }
        p3cConfigDataBeanLocal.loadFromX8lTree(p3cConfigDataBeanLocal.getP3cConfigX8lTree());
        return p3cConfigDataBeanLocal;
    }

    @Override
    public void loadPatchConfigFile(File file) {
        this.getP3cConfigDataBean().tryPatchP3cConfigDataBean(file);
    }

    @Override
    public List<String> getNameList(String className, String name) {
        return getContentNode(className, name).asStringListTrimmed();
    }

    @Override
    public Map<String, String> getNameMap(String className, String name) {
        return getContentNode(className, name).asStringMapTrimmed();
    }

    @Override
    public boolean ifRuleClassInRuleBlackList(Class ruleClass) {
        return ifStringInRuleBlackList(ruleClass.getSimpleName())
                || ifStringInRuleBlackList(ruleClass.getCanonicalName());
    }

    public boolean ifStringInRuleBlackList(String string) {
        return this.getP3cConfigDataBean().getRuleBlackListSet().contains(string);
    }

    @Override
    public boolean ifClassNameInClassBlackList(String className) {
        return this.getP3cConfigDataBean().getClassBlackListSet().contains(className);
    }

    @Override
    public boolean ifRuleClassNameClassNamePairInPairIgnoreList(Class ruleClass, String className) {
        if (getP3cConfigDataBean().getRuleClassPairBlackListMap().containsKey(className)) {
            Set<String> ruleSet = getP3cConfigDataBean().getRuleClassPairBlackListMap().get(className);
            return ruleSet.contains(ruleClass.getSimpleName()) || ruleSet.contains(ruleClass.getCanonicalName());
        }
        return false;
    }


    public ContentNode getContentNode(String className, String name) {
        return getLastFromList(
                getP3cConfigDataBean().getP3cConfigX8lTree().fetch(
                        X8lDataBeanFieldScheme.X8LPATH,
                        "com.alibaba.p3c.pmd.config>rule_config>CONTENT_NODE(" + className + ")>CONTENT_NODE(" + name + ")",
                        ContentNode.class
                )
        );
    }

    public P3cConfigDataBean getP3cConfigDataBean() {
        return p3cConfigDataBean;
    }
}
