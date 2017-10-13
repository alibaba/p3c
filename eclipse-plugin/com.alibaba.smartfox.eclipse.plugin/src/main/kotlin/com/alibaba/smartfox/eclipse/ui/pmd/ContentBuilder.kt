package com.alibaba.smartfox.eclipse.ui.pmd

import net.sourceforge.pmd.lang.Language
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.PlatformUI
import java.net.URL
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

/**
 *
 * @author caikang
 * @date 2017/07/20
 */
class ContentBuilder {
    private val CR = '\n'
    private val buffer = StringBuilder()
    private val headingSpans = ArrayList<IntArray>()
    private val codeSpans = ArrayList<IntArray>()
    private val linksBySpan = HashMap<IntArray, String>()

    private val indentDepth: Int = 3

    private val codeStyleExtractor = StyleExtractor(SyntaxManager.getSyntaxData("java"))

    private val background = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE)

    private val headingColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE)

    private val codeStyle = FontBuilder("Courier", 11, SWT.NORMAL).style(Display.getCurrent())

    fun clear() {
        buffer.setLength(0)
        headingSpans.clear()
        codeSpans.clear()
        linksBySpan.clear()
    }

    fun addHeading(heading: String) {
        var length = buffer.length
        if (length > 0) {
            buffer.append(CR)
            length += 1
        }

        headingSpans.add(intArrayOf(length, length + heading.length))
        buffer.append(heading).append(CR)
    }

    fun addText(text: String) {
        for (i in 0 until indentDepth) {
            buffer.append(' ')
        }
        buffer.append(text).append(CR)
    }

    fun addRawText(text: String) {
        buffer.append(text)
    }

    fun addCode(code: String) {
        val length = buffer.length
        codeSpans.add(intArrayOf(length, length + code.length))
        buffer.append(code)
    }

    fun setLanguage(language: Language) {
        val syntax = SyntaxManager.getSyntaxData(language.terseName)
        codeStyleExtractor.syntax(syntax)
    }

    fun hasLinks(): Boolean {
        return linksBySpan.isNotEmpty()
    }

    fun addLinkHandler(widget: StyledText) {

        widget.addListener(SWT.MouseDown) { event ->
            // It is up to the application to determine when and how a link
            // should be activated.
            // In this snippet links are activated on mouse down when the
            // control key is held down
            // if ((event.stateMask & SWT.MOD1) != 0) {
            try {
                val offset = widget.getOffsetAtLocation(Point(event.x, event.y))
                val link = linkAt(offset)
                if (link != null) {
                    launchBrowser(link)
                }

            } catch (e: IllegalArgumentException) {
                // no character under event.x, event.y
            }
        }
    }

    private fun linkAt(textIndex: Int): String? {
        var span: IntArray
        for ((key, value) in linksBySpan) {
            span = key
            if (span[0] <= textIndex && textIndex <= span[1]) {
                return value
            }
        }
        return null
    }

    private fun launchBrowser(link: String) {
        try {
            val browser = PlatformUI.getWorkbench().browserSupport.externalBrowser
            browser.openURL(URL(link))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun showOn(widget: StyledText) {
        val text = buffer.toString()
        widget.text = text
        val ranges = ArrayList<StyleRange>()
        var span: IntArray
        for (i in headingSpans.indices) {
            span = headingSpans[i]
            ranges.add(StyleRange(span[0], span[1] - span[0], headingColor, background, SWT.BOLD))
        }
        for (spn in linksBySpan.keys) {
            val style = StyleRange(spn[0], spn[1] - spn[0], headingColor, background, SWT.UNDERLINE_LINK)
            style.underline = true
            ranges.add(style)
        }
        val crStr = Character.toString(CR)
        var sr: StyleRange

        for (i in codeSpans.indices) {
            span = codeSpans[i]
            sr = StyleRange(codeStyle)
            sr.start = span[0]
            sr.length = span[1] - span[0]

            val colorRanges = codeStyleExtractor.stylesFor(text, sr.start, sr.length, crStr)
            ranges += colorRanges
        }
        // must be in order!
        val styles = sort(ranges)
        widget.styleRanges = styles
    }

    private fun sort(ranges: List<StyleRange>): Array<StyleRange> {
        val styles = ranges.toTypedArray()
        Arrays.sort(styles) { sr1, sr2 -> sr1.start - sr2.start }
        return styles
    }
}