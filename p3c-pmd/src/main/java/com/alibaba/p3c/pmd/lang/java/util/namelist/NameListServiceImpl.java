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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author changle.lq
 * @date 2017/03/27
 */
public class NameListServiceImpl implements NameListService {

    private static final String NAME_LIST_PROPERTY_FILE_NAME = "namelist.properties";
    private static final Properties PROPERTIES = initProperties();
    private static final String SEPARATOR = "_";

    private static Properties initProperties() {
        LinkedProperties props = new LinkedProperties();
        ClassLoader classLoader = NameListServiceImpl.class.getClassLoader();
        try {
            props.load(classLoader.getResourceAsStream(NAME_LIST_PROPERTY_FILE_NAME));
        } catch (IOException ex) {
            throw new IllegalStateException("Load namelist.properties fail", ex);
        }
        return props;
    }

    @Override
    public List<String> getNameList(String className, String name) {
        Gson gson = new Gson();
        return gson.fromJson((String)PROPERTIES.get(className + SEPARATOR + name),
            new TypeToken<List<String>>() {}.getType());
    }

    @Override
    public <K, V> Map<K, V> getNameMap(String className, String name, Class<K> kClass, Class<V> vClass) {
        Gson gson = new Gson();
        return gson.fromJson((String)PROPERTIES.get(className + SEPARATOR + name),
            new TypeToken<Map<K, V>>() {
            }.getType());
    }

    private static class LinkedProperties extends Properties {
        private LinkedHashSet<Object> linkedKeys = new LinkedHashSet<>();

        @Override
        public Object put(Object key, Object value) {
            linkedKeys.add(key);
            return super.put(key, value);
        }

        public int getSize() {
            return linkedKeys.size();
        }
    }
}
