package com.alibaba.smartfox.eclipse.ui.pmd

import org.eclipse.swt.custom.LineStyleEvent
import org.eclipse.swt.custom.LineStyleListener

/**
 * This class performs the syntax highlighting and styling for Pmpe
 *  * PmpeLineStyleListener constructor

 * @param theSyntaxData the syntax data to use
 */
class BasicLineStyleListener(theSyntaxData: SyntaxData) : StyleExtractor(theSyntaxData), LineStyleListener {
    /**
     * Called by StyledText to get styles for a line
     */
    override fun lineGetStyle(event: LineStyleEvent) {
        val styles = lineStylesFor(event.lineText, event.lineOffset, event.lineText.length)
        event.styles = styles.toTypedArray()
    }
}
