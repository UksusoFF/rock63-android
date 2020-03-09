package com.uksusoff.rock63.utils

import java.io.*
import java.nio.CharBuffer
import java.util.*

object CommonUtils {
    fun getDateFromTimestamp(ts: Int): Date {
        return Date(ts.toLong() * 1000)
    }

    fun getCroppedString(source: String, places: Int, addDots: Boolean): String {
        val postfix = if (addDots) "..." else ""
        return if (source.length < places - postfix.length) source else source.substring(0, places - postfix.length) + postfix
    }

    fun getTextFromHtml(s: String): String {
        var noHTMLString = s.replace("<(.|\n)*?>".toRegex(), "")
        noHTMLString = noHTMLString.replace("&.*?;".toRegex(), "")
        return noHTMLString
    }
}