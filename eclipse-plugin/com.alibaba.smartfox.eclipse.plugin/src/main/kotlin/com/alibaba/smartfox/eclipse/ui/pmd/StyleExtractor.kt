package com.alibaba.smartfox.eclipse.ui.pmd

import net.sourceforge.pmd.util.StringUtil
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.widgets.Display
import java.util.ArrayList
import java.util.LinkedList

/**
 * @author Brian Remedios
 */
open class StyleExtractor(private var syntaxData: SyntaxData?) {
    private val commentOffsets: MutableList<IntArray>

    init {
        commentOffsets = LinkedList<IntArray>()
    }

    fun syntax(theSyntax: SyntaxData?) {
        syntaxData = theSyntax
    }

    /**
     * Refreshes the offsets for all multiline comments in the parent
     * StyledText. The parent StyledText should call this whenever its text is
     * modified. Note that this code doesn't ignore comment markers inside
     * strings.

     * @param text the text from the StyledText
     */
    fun refreshMultilineComments(text: String) {
        // Clear any stored offsets
        commentOffsets.clear()

        if (syntaxData != null) {
            // Go through all the instances of COMMENT_START
            var pos = text.indexOf(syntaxData!!.multiLineCommentStart!!)
            while (pos > -1) {
                // offsets[0] holds the COMMENT_START offset
                // and COMMENT_END holds the ending offset
                val offsets = IntArray(2)
                offsets[0] = pos

                // Find the corresponding end comment.
                pos = text.indexOf(syntaxData!!.multiLineCommentEnd!!, pos)

                // If no corresponding end comment, use the end of the text
                offsets[1] = if (pos == -1) text.length - 1 else pos + syntaxData!!.multiLineCommentEnd!!.length - 1
                pos = offsets[1]
                // Add the offsets to the collection
                commentOffsets.add(offsets)
                pos = text.indexOf(syntaxData!!.multiLineCommentStart!!, pos)
            }
        }
    }

    /**
     * Checks to see if the specified section of text begins inside a multiline
     * comment. Returns the index of the closing comment, or the end of the line
     * if the whole line is inside the comment. Returns -1 if the line doesn't
     * begin inside a comment.

     * @param start the starting offset of the text
     * @param length the length of the text
     * @return int
     */
    private fun getBeginsInsideComment(start: Int, length: Int): Int {
        // Assume section doesn't being inside a comment
        var index = -1

        // Go through the multiline comment ranges
        var i = 0
        val n = commentOffsets.size
        while (i < n) {
            val offsets = commentOffsets[i]

            // If starting offset is past range, quit
            if (offsets[0] > start + length) {
                break
            }
            // Check to see if section begins inside a comment
            if (offsets[0] <= start && offsets[1] >= start) {
                // It does; determine if the closing comment marker is inside
                // this section
                index = if (offsets[1] > start + length) start + length
                else offsets[1] + syntaxData!!.multiLineCommentEnd!!.length - 1
            }
            i++
        }
        return index
    }

    private fun isDefinedVariable(text: String): Boolean {
        return StringUtil.isNotEmpty(text)
    }

    private fun atMultiLineCommentStart(text: String, position: Int): Boolean {
        return text.indexOf(syntaxData!!.multiLineCommentStart!!, position) == position
    }

    private fun atStringStart(text: String, position: Int): Boolean {
        return text.indexOf(syntaxData!!.stringStart!!, position) == position
    }

    private fun atVarnameReference(text: String, position: Int): Boolean {
        return syntaxData!!.varnameReference != null && text.indexOf(syntaxData!!.varnameReference!!,
                position) == position
    }

    private fun atSingleLineComment(text: String, position: Int): Boolean {
        return syntaxData!!.comment != null && text.indexOf(syntaxData!!.comment!!, position) == position
    }

    private fun getKeywordEnd(lineText: String, start: Int): Int {

        val length = lineText.length

        val buf = StringBuilder(length)
        var i = start

        // Call any consecutive letters a word
        while (i < length && Character.isLetter(lineText[i])) {
            buf.append(lineText[i])
            i++
        }

        return if (syntaxData!!.isKeyword(buf.toString())) i else 0 - i
    }

    /**
     * Chop up the text into individual lines starting from offset and then
     * determine the required styles for each. Ensures the offset is properly
     * accounted for in each.

     * @param text
     * @param offset
     * @param length
     * @return
     */
    fun stylesFor(text: String, offset: Int, length: Int, lineSeparator: String): List<StyleRange> {

        if (syntaxData == null) {
            return emptyList()
        }

        val content = text.substring(offset, offset + length)
        val lines = content.split(lineSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val styles = ArrayList<StyleRange>()

        val separatorLength = lineSeparator.length

        var currentOffset = offset

        for (line in lines) {
            val lineLength = line.length
            val lineStyles = lineStylesFor(line, 0, lineLength)

            for (sr in lineStyles) {
                sr.start += currentOffset
            }
            styles.addAll(lineStyles)
            currentOffset += lineLength + separatorLength
        }

        return styles
    }

    fun lineStylesFor(lineText: String, lineOffset: Int, length: Int): List<StyleRange> {

        val styles = ArrayList<StyleRange>()

        var start = 0

        // Check if line begins inside a multiline comment
        val mlIndex = getBeginsInsideComment(lineOffset, lineText.length)
        if (mlIndex > -1) {
            // Line begins inside multiline comment; create the range
            styles.add(StyleRange(lineOffset, mlIndex - lineOffset, COMMENT_COLOR, COMMENT_BACKGROUND))
            start = mlIndex
        }
        // Do punctuation, single-line comments, and keywords
        while (start < length) {
            // Check for multiline comments that begin inside this line
            if (atMultiLineCommentStart(lineText, start)) {
                // Determine where comment ends
                var endComment = lineText.indexOf(syntaxData!!.multiLineCommentEnd!!, start)

                // If comment doesn't end on this line, extend range to end of
                // line
                if (endComment == -1) {
                    endComment = length
                } else {
                    endComment += syntaxData!!.multiLineCommentEnd!!.length
                }
                styles.add(StyleRange(lineOffset + start, endComment - start, COMMENT_COLOR, COMMENT_BACKGROUND))

                start = endComment
            } else if (atStringStart(lineText, start)) {
                // Determine where comment ends
                var endString = lineText.indexOf(syntaxData!!.stringEnd!!, start + 1)

                // If string doesn't end on this line, extend range to end of
                // line
                if (endString == -1) {
                    endString = length
                } else {
                    endString += syntaxData!!.stringEnd!!.length
                }
                styles.add(StyleRange(lineOffset + start, endString - start, STRING_COLOR, COMMENT_BACKGROUND))

                start = endString
            } else if (atSingleLineComment(lineText, start)) {
                // line comments

                styles.add(StyleRange(lineOffset + start, length - start, COMMENT_COLOR, COMMENT_BACKGROUND))
                start = length
            } else if (atVarnameReference(lineText, start)) {
                // variable
                // references

                val buf = StringBuilder()
                var i = start + syntaxData!!.varnameReference!!.length
                // Call any consecutive letters a word
                while (i < length && Character.isLetter(lineText[i])) {
                    buf.append(lineText[i])
                    i++
                }

                // See if the word is a variable
                if (isDefinedVariable(buf.toString())) {
                    // It's a keyword; create the StyleRange
                    styles.add(StyleRange(lineOffset + start, i - start, REFERENCED_VAR_COLOR, null, SWT.BOLD))
                }
                // Move the marker to the last char (the one that wasn't a
                // letter)
                // so it can be retested in the next iteration through the loop
                start = i
            } else if (syntaxData!!.isPunctuation(lineText[start])) {
                // Add range for punctuation
                styles.add(StyleRange(lineOffset + start, 1, PUNCTUATION_COLOR, null))
                ++start
            } else if (Character.isLetter(lineText[start])) {

                val kwEnd = getKeywordEnd(lineText, start)
                // is a keyword

                if (kwEnd > start) {
                    styles.add(StyleRange(lineOffset + start, kwEnd - start, KEYWORD_COLOR, null))
                }

                // Move the marker to the last char (the one that wasn't a
                // letter)
                // so it can be retested in the next iteration through the loop
                start = Math.abs(kwEnd)
            } else {
                ++start // It's nothing we're interested in; advance the marker
            }// Check for punctuation
        }

        return styles
    }

    companion object {

        private val COMMENT_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN)
        private val REFERENCED_VAR_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN)
        private val UNREFERENCED_VAR_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW)
        private val COMMENT_BACKGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE)
        private val PUNCTUATION_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK)
        private val KEYWORD_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA)
        private val STRING_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE)
    }
}
