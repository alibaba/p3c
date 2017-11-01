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
package com.alibaba.p3c.idea.i18n

import com.alibaba.p3c.idea.config.P3cConfig
import com.alibaba.p3c.pmd.I18nResources
import com.alibaba.smartfox.idea.common.util.getService
import com.intellij.CommonBundle
import java.util.Locale
import java.util.ResourceBundle

/**
 *
 *
 * @author caikang
 * @date 2017/06/20
 */
object P3cBundle {
    private val p3cConfig = P3cConfig::class.java.getService()
    private val resourceBundle = ResourceBundle.getBundle("messages.P3cBundle",
            Locale(p3cConfig.locale), I18nResources.XmlControl())

    fun getMessage(key: String): String {
        return resourceBundle.getString(key).trim()
    }

    fun message(key: String, vararg params: Any): String {
        return CommonBundle.message(resourceBundle, key, *params).trim()
    }
}
