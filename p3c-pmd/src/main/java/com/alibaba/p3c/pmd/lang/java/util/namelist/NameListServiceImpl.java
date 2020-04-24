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
import com.xenoamess.x8l.X8lGrammarException;
import com.xenoamess.x8l.X8lTree;
import com.xenoamess.x8l.databind.X8lDataBeanFieldScheme;
import com.xenoamess.x8l.dealers.X8lDealer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.xenoamess.x8l.databind.X8lDataBeanDefaultParser.getLastFromList;
import static java.util.logging.Level.WARNING;

/**
 * @author changle.lq
 * @date 2017/03/27
 */
public class NameListServiceImpl implements NameListService {
    private static final Logger LOGGER = Logger.getLogger(NameListServiceImpl.class.getName());

    private static final String P3C_CONFIG_FILE_NAME = "p3c_config.x8l";
    private static final String DEFAULT_P3C_CONFIG_FILE_NAME = "p3c_config.default.x8l";
    private final P3cConfigDataBean p3cConfigDataBean;

    private boolean ifLoadCustomerConfigX8lTree;

    public NameListServiceImpl() {
        this(true);
    }

    public NameListServiceImpl(boolean ifLoadCustomerConfigX8lTree) {
        this.ifLoadCustomerConfigX8lTree = ifLoadCustomerConfigX8lTree;
        p3cConfigDataBean = initP3cConfigDataBean(this.ifLoadCustomerConfigX8lTree);
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
            File customerConfigFile = new File(P3C_CONFIG_FILE_NAME);
            if (customerConfigFile.exists() && customerConfigFile.isFile()) {
                try {
                    X8lTree customerConfigX8lTree = X8lTree.load(
                            customerConfigFile, X8lDealer.INSTANCE
                    );
                    p3cConfigDataBeanLocal.getP3cConfigX8lTree().append(customerConfigX8lTree);
                } catch (IOException e) {
                    LOGGER.log(WARNING, "reading customer config file fails, IO fails.", e);
                } catch (X8lGrammarException e) {
                    LOGGER.log(WARNING, "reading customer config file fails, grammar wrong.", e);
                }
            }
        }

        p3cConfigDataBeanLocal.loadFromX8lTree(p3cConfigDataBeanLocal.getP3cConfigX8lTree());
        return p3cConfigDataBeanLocal;
    }

    @Override
    public List<String> getNameList(String className, String name) {
        return getContentNode(className, name).asStringListTrimmed();
    }

    @Override
    public Map<String, String> getNameMap(String className, String name) {
        return getContentNode(className, name).asStringMapTrimmed();
    }

    public ContentNode getContentNode(String className, String name) {
        return getLastFromList(
                getP3cConfigDataBean().getP3cConfigX8lTree().fetch(
                        X8lDataBeanFieldScheme.X8LPATH,
                        "com.alibaba.p3c.pmd.config>rule_config>" + className + ">CONTENT_NODE(" + name + ")",
                        ContentNode.class
                )
        );
    }

    public P3cConfigDataBean getP3cConfigDataBean() {
        return p3cConfigDataBean;
    }
}
