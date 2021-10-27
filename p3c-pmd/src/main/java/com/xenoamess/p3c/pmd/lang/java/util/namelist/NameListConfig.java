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

import com.xenoamess.p3c.pmd.lang.java.util.SpiLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author changle.lq
 * @date 2017/03/27
 */
public class NameListConfig {
    private static @NotNull
    NameListService nameListService = createNameListService();

    public static synchronized @NotNull
    NameListService renewNameListService() {
        setNameListService(createNameListService());
        return getNameListService();
    }

    public static synchronized @NotNull
    NameListService renewNameListService(@NotNull File patchConfigFile) {
        setNameListService(createNameListService());
        getNameListService().loadPatchConfigFile(patchConfigFile);
        return getNameListService();
    }

    private static @NotNull
    NameListService createNameListService() {
        NameListService instance = SpiLoader.getInstance(NameListService.class);
        if (instance == null) {
            instance = new NameListServiceImpl();
        }
        return instance;
    }

    public static synchronized @NotNull
    NameListService getNameListService() {
        return nameListService;
    }

    public static synchronized void setNameListService(@NotNull NameListService nameListService) {
        NameListConfig.nameListService = nameListService;
    }
}
