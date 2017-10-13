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

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

/**
 * @author changle.lq
 * @date 2017/04/01
 */
public class SpiLoader {
    private final static ConcurrentHashMap<Class<?>, Object> INSTANCE_CACHE = new ConcurrentHashMap<Class<?>, Object>();

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> classType) {
        T instance = (T)INSTANCE_CACHE.get(classType);

        if (instance != null) {
            return instance;
        }
        try {
            instance = ServiceLoader.load(classType, NameListConfig.class.getClassLoader()).iterator().next();
            if (instance == null) {
                return null;
            }
            INSTANCE_CACHE.putIfAbsent(classType, instance);
            return instance;
        } catch (Throwable e) {
            return null;
        }
    }
}
