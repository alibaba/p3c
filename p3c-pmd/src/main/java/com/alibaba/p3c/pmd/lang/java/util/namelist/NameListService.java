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
     * @return  name list
     */
    List<String> getNameList(String className,String name);

    /**
     * get config
     * @param className class name
     * @param name type name
     * @param kClass  type of key
     * @param vClass  type of value
     * @param <K> type of key
     * @param <V> type of value
     * @return  name list
     */
    <K, V> Map<K, V> getNameMap(String className,String name,Class<K> kClass,Class<V> vClass);
}
