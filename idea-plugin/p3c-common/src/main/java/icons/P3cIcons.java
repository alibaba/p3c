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
package icons;

import javax.swing.Icon;

import com.intellij.openapi.util.IconLoader;

/**
 * @author caikang
 * @date 2016/12/28
 */
public final class P3cIcons {
    private P3cIcons() {
        throw new AssertionError("icons.P3cIcons"
            + " instances for you!");
    }

    public static final Icon ANALYSIS_ACTION = IconLoader.getIcon("/icons/ali-ide-run.png");

    public static final Icon PROJECT_INSPECTION_ON = IconLoader.getIcon("/icons/qiyong.png");
    public static final Icon PROJECT_INSPECTION_OFF = IconLoader.getIcon("/icons/tingyong.png");
    public static final Icon LANGUAGE = IconLoader.getIcon("/icons/language.png");
    public static final Icon ALIBABA = IconLoader.getIcon("/icons/alibaba.png");
}
