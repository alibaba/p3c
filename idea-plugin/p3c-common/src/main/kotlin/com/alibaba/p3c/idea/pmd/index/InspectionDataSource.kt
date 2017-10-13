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
package com.alibaba.p3c.idea.pmd.index

import com.intellij.util.indexing.FileContent
import net.sourceforge.pmd.util.datasource.DataSource
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * @author caikang
 * @date 2016/12/11
 */
class InspectionDataSource(private val fileContent: FileContent) : DataSource {

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {
        return ByteArrayInputStream(fileContent.content)
    }

    override fun getNiceFileName(shortNames: Boolean, inputFileName: String?): String {
        return fileContent.fileName
    }
}
