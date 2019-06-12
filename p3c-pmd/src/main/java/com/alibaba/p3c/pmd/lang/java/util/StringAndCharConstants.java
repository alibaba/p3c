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

/**
 * @author caikang
 * @date 2017/03/28
 */
public final class StringAndCharConstants {
    private StringAndCharConstants(){
        throw new AssertionError("com.alibaba.p3c.pmd.lang.java.util.StringAndCharConstants"
            + " instances for you!");
    }

    public static final char DOT = '.';
    public static final String DOLLAR = "$";
    public static final String UNDERSCORE = "_";
}
