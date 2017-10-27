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
package com.alibaba.smartfox.eclipse.pmd

enum class RulePriority(val priority: Int, val title: String) {

    Blocker(1, "Blocker"), Critical(2, "Critical"), Major(3, "Major");

    override fun toString(): String {
        return title
    }

    companion object {
        fun valueOf(priority: Int): RulePriority {
            try {
                return RulePriority.values()[priority - 1]
            } catch (e: ArrayIndexOutOfBoundsException) {
                return Major
            }
        }
    }
}
