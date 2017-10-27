package com.alibaba.smartfox.eclipse.ui.pmd

import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.ModifyListener
import java.util.HashSet
import java.util.Hashtable
import java.util.MissingResourceException
import java.util.ResourceBundle
import java.util.StringTokenizer

/**
 * This class manages the syntax coloring and styling data
 */
object SyntaxManager {

    private val syntaxByExtension = Hashtable<String, SyntaxData>()

    fun adapt(codeField: StyledText, languageCode: String, oldListener: ModifyListener?): ModifyListener? {

        if (oldListener != null) {
            codeField.removeModifyListener(oldListener)
        }

        val sd = SyntaxManager.getSyntaxData(languageCode) ?: return null

        val blsl = BasicLineStyleListener(sd)
        codeField.addLineStyleListener(blsl)

        val ml = ModifyListener {
            blsl.refreshMultilineComments(codeField.text)
            codeField.redraw()
        }
        codeField.addModifyListener(ml)

        return ml
    }

    /**
     * Gets the syntax data for an extension
     */
    @Synchronized fun getSyntaxData(extension: String): SyntaxData? {
        // Check in cache
        var sd: SyntaxData? = syntaxByExtension[extension]
        if (sd == null) {
            // Not in cache; load it and put in cache
            sd = loadSyntaxData(extension)
            if (sd != null) {
                syntaxByExtension.put(sd.extension, sd)
            }
        }
        return sd
    }

    /**
     * Loads the syntax data for an extension
     * @return SyntaxData
     */
    private fun loadSyntaxData(filename: String): SyntaxData? {
        var sd: SyntaxData? = null
        try {
            val rb = ResourceBundle.getBundle("/syntax/$filename")
            sd = SyntaxData(filename)

            sd.stringStart = rb.getString("stringstart")
            sd.stringEnd = rb.getString("stringend")
            sd.multiLineCommentStart = rb.getString("multilinecommentstart")
            sd.multiLineCommentEnd = rb.getString("multilinecommentend")

            // Load the keywords
            val keywords = HashSet<String>()
            val st = StringTokenizer(rb.getString("keywords"), " ")
            while (st.hasMoreTokens()) {
                keywords.add(st.nextToken())
            }
            sd.setKeywords(keywords)

            // Load the punctuation
            sd.setPunctuation(rb.getString("punctuation"))

            if (rb.containsKey("comment")) {
                sd.comment = rb.getString("comment")
            }

            if (rb.containsKey("varnamedelimiter")) {
                sd.varnameReference = rb.getString("varnamedelimiter")
            }

        } catch (e: MissingResourceException) {
            // Ignore
        }

        return sd
    }
}
