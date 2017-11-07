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
package com.alibaba.smartfox.eclipse

import com.alibaba.p3c.pmd.I18nResources
import net.sourceforge.pmd.Rule
import net.sourceforge.pmd.RuleSetFactory
import net.sourceforge.pmd.RuleSets
import org.apache.log4j.Logger
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext
import java.util.Locale

/**
 * @author caikang
 * @date 2017/06/14
 */
class SmartfoxActivator : AbstractUIPlugin() {

    init {
        aDefault = this
    }

    private val logger = Logger.getLogger(javaClass)!!
    lateinit var ruleSets: RuleSets
    private val localeKey = "p3c.locale"

    lateinit var ruleMap: Map<String, Rule>

    @Throws(Exception::class) override fun start(context: BundleContext) {
        super.start(context)
        I18nResources.changeLanguage(locale)
        ruleSets = createRuleSets()
        ruleMap = ruleSets.allRules.associateBy {
            it.name
        }
    }

    @Throws(Exception::class) override fun stop(context: BundleContext?) {
        aDefault = null
        super.stop(context)
    }


    fun getImage(key: String, iconPath: String = key): Image {
        val registry = imageRegistry
        var image: Image? = registry.get(key)
        if (image == null) {
            val descriptor = getImageDescriptor(iconPath)
            registry.put(key, descriptor)
            image = registry.get(key)
        }

        return image!!
    }

    val locale: String
        get() {
            val language = preferenceStore.getString(localeKey)
            if (language.isNullOrBlank()) {
                val lang = Locale.getDefault().language
                return if (lang != Locale.ENGLISH.language && lang != Locale.CHINESE.language) {
                    Locale.ENGLISH.language
                } else Locale.getDefault().language
            }

            return language
        }

    fun toggleLocale() {
        val lang = if (Locale.ENGLISH.language == locale) Locale.CHINESE.language else Locale.ENGLISH.language
        preferenceStore.setValue(localeKey, lang)
    }

    fun getRule(rule: String): Rule {
        return ruleMap[rule]!!
    }

    fun showError(message: String, t: Throwable) {
        logError(message, t)
        Display.getDefault().syncExec {
            MessageDialog.openError(Display.getCurrent().activeShell, "错误", message + "\n" + t.toString())
        }
    }

    fun logError(message: String, t: Throwable) {
        log.log(Status(IStatus.ERROR, bundle.symbolicName, 0, message + t.message, t))
        logger.error(message, t)
    }

    fun logError(status: IStatus) {
        log.log(status)
        logger.error(status.message, status.exception)
    }

    fun logInformation(message: String) {
        log.log(Status(IStatus.INFO, bundle.symbolicName, 0, message, null))
    }

    fun logWarn(message: String) {
        log.log(Status(IStatus.WARNING, bundle.symbolicName, 0, message, null))
    }

    companion object {
        // The plug-in ID
        val PLUGIN_ID = "com.alibaba.smartfox.eclipse.plugin"

        var aDefault: SmartfoxActivator? = null
            private set

        val instance: SmartfoxActivator get() = aDefault!!

        fun getImageDescriptor(path: String): ImageDescriptor {
            return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path)
        }

        fun createRuleSets(): RuleSets {
            val ruleSetFactory = RuleSetFactory()
            return ruleSetFactory.createRuleSets("java-ali-pmd,vm-ali-other")
        }
    }
}
