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

import com.alibaba.p3c.pmd.lang.java.util.SpiLoader;

/**
 * @author changle.lq
 * @date 2017/03/27
 */
public class NameListConfig {
    public static final NameListService NAME_LIST_SERVICE = getNameListService();

    private static NameListService getNameListService() {
        NameListService instance  = SpiLoader.getInstance(NameListService.class);
        if (instance == null) {
            instance = new NameListServiceImpl();
        }
        return instance;
    }
}
