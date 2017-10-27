package com.alibaba.smartfox.eclipse.ui.pmd

import net.sourceforge.pmd.util.StringUtil
import java.util.ArrayList

/**
 * @author Brian Remedios
 */
class StringArranger(private val indentString: String) {

    fun withIndent(rawText: String): String {
        return indentString + rawText
    }

    fun format(rawText: String): StringBuilder {

        val sb = StringBuilder()
        for (line in trimmedLinesIn(rawText)) {
            sb.append(indentString)
            sb.append(line).append(CR)
        }

        return sb
    }

    fun trimmedLinesIn(text: String): List<String> {

        val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (lines.isEmpty()) {
            return emptyList()
        }

        val lineSet = ArrayList<String>(lines.size)

        var startLine = 0
        while (startLine < lines.size && StringUtil.isEmpty(lines[startLine])) {
            startLine++
        }

        var endLine = lines.size - 1
        while (endLine >= 0 && StringUtil.isEmpty(lines[endLine])) {
            endLine--
        }

        lines.mapTo(lineSet) {
            it.trim { it <= ' ' }
        }
        return lineSet
    }

    companion object {
        private val CR = '\n'
    }
}
