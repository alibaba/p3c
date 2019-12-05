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
package com.alibaba.p3c.pmd;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * @author caikang
 * @date 2017/05/24
 */
public class I18nResources {
    private static final String XML_LITERAL = "xml";

    private static String lang = System.getProperty("pmd.language", "zh");

    private static Locale currentLocale;

    private static ResourceBundle resourceBundle = changeLanguage(lang);

    public static ResourceBundle changeLanguage(String language) {
        Locale locale = Locale.CHINESE.getLanguage().equals(language) ? Locale.CHINESE : Locale.ENGLISH;
        return changeLanguage(locale);
    }

    public static ResourceBundle changeLanguage(Locale locale) {
        if (currentLocale != null && currentLocale.equals(locale)) {
            return resourceBundle;
        }
        currentLocale = locale;
        resourceBundle = ResourceBundle.getBundle("messages", locale, new XmlControl());
        return resourceBundle;
    }

    public static String getMessage(String key) {
        if (key == null) {
            // 暂时返回空字符串
            return "";
        }
        try {
            return resourceBundle.getString(key).trim();
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static String getMessage(String key, Object... params) {
        String value = getMessage(key);
        if (params == null || params.length == 0) {
            return value;
        }
        return String.format(value, params);
    }

    public static String getMessageWithExceptionHandled(String key) {
        if (key == null) {
            // 暂时返回空字符串
            return "";
        }
        try {
            return resourceBundle.getString(key).trim();
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static class XmlResourceBundle extends ResourceBundle {
        private Properties props;

        XmlResourceBundle(InputStream stream) throws IOException {
            props = new Properties();
            props.loadFromXML(stream);
        }

        @Override
        protected Object handleGetObject(String key) {
            return props.getProperty(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            List<String> keys = new ArrayList<>();
            Enumeration<Object> enumeration = props.keys();
            while (enumeration.hasMoreElements()) {
                keys.add((String)enumeration.nextElement());
            }
            return Collections.enumeration(keys);
        }
    }

    public static class XmlControl extends Control {
        @Override
        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return Collections.singletonList(XML_LITERAL);
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return null;
        }

        @Override
        public ResourceBundle newBundle(String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload)
            throws IllegalAccessException,
            InstantiationException,
            IOException {
            if (baseName == null || locale == null
                || format == null || loader == null) {
                throw new NullPointerException();
            }
            ResourceBundle bundle = null;
            if (XML_LITERAL.equals(format)) {
                String bundleName = toBundleName(baseName, locale);
                String resourceName = toResourceName(bundleName, format);
                InputStream stream;
                if (reload) {
                    stream = getInputStream(loader, resourceName);
                } else {
                    stream = loader.getResourceAsStream(resourceName);
                }
                if (stream != null) {
                    BufferedInputStream bis = new BufferedInputStream(stream);
                    bundle = new XmlResourceBundle(bis);
                    bis.close();
                }
            }
            return bundle;
        }

        private InputStream getInputStream(ClassLoader loader, String resourceName)
            throws IOException {
            URL url = loader.getResource(resourceName);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            if (connection == null) {
                return null;
            }
            // Disable caches to get fresh data for
            // reloading.
            connection.setUseCaches(false);
            return connection.getInputStream();
        }
    }
}
