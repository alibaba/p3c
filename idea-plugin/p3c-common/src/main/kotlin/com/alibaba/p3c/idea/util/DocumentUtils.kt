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
package com.alibaba.p3c.idea.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

/**
 *
 *
 * @author caikang
 * @date 2017/03/16
6
 */
object DocumentUtils {
    private val PMD_TAB_SIZE = 8
    fun calculateRealOffset(document: Document, line: Int, pmdColumn: Int): Int {
        val maxLine = document.lineCount
        if (maxLine < line) {
            return -1
        }
        val lineOffset = document.getLineStartOffset(line - 1)
        return lineOffset + calculateRealColumn(document, line, pmdColumn)
    }

    fun calculateLineStart(document: Document, line: Int): Int {
        val maxLine = document.lineCount
        if (maxLine < line) {
            return -1
        }
        return document.getLineStartOffset(line - 1)
    }

    fun calculateRealColumn(document: Document, line: Int, pmdColumn: Int): Int {
        var realColumn = pmdColumn - 1
        val minusSize = PMD_TAB_SIZE - 1
        val docLine = line - 1
        val lineStartOffset = document.getLineStartOffset(docLine)
        val lineEndOffset = document.getLineEndOffset(docLine)
        val text = document.getText(TextRange(lineStartOffset, lineEndOffset))

        text.forEachIndexed { i, c ->
            if (c == '\t') {
                realColumn -= minusSize
            }
            if (i >= realColumn) {
                return@forEachIndexed
            }
        }

        return realColumn
    }
}