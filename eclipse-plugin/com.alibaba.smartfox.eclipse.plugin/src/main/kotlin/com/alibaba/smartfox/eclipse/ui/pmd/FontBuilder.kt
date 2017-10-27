package com.alibaba.smartfox.eclipse.ui.pmd

import org.eclipse.swt.graphics.Font
import org.eclipse.swt.graphics.TextStyle
import org.eclipse.swt.widgets.Display

/**
 * @author Brian Remedios
 */
class FontBuilder(val name: String, val size: Int, val style: Int, val colorIdx: Int = -1) {

    fun build(display: Display): Font {
        return Font(display, name, size, style)
    }

    fun style(display: Display): TextStyle {
        return TextStyle(build(display), if (colorIdx < 0) null else display.getSystemColor(colorIdx), null)
    }
}
