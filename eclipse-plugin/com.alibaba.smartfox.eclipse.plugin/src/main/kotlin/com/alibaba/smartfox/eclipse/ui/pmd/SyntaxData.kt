package com.alibaba.smartfox.eclipse.ui.pmd

/**
 * This class contains information for syntax coloring and styling for an
 * extension
 */
class SyntaxData(val extension: String) {

    var varnameReference: String? = null
    var stringStart: String? = null
    var stringEnd: String? = null
    private var keywords: Collection<String>? = null
    private var punctuation: String? = null
    var comment: String? = null
    var multiLineCommentStart: String? = null
    var multiLineCommentEnd: String? = null

    fun matches(otherExtension: String): Boolean {
        return extension == otherExtension
    }

    fun isKeyword(word: String): Boolean {
        return keywords != null && keywords!!.contains(word)
    }

    fun isPunctuation(ch: Char): Boolean {
        return punctuation != null && punctuation!!.indexOf(ch) >= 0
    }

    fun setKeywords(keywords: Collection<String>) {
        this.keywords = keywords
    }

    fun setPunctuation(thePunctuationChars: String) {
        punctuation = thePunctuationChars
    }
}
