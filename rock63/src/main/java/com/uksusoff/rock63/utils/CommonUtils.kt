package com.uksusoff.rock63.utils

import java.io.*
import java.nio.CharBuffer
import java.util.*

object CommonUtils {
    fun getDateFromTimestamp(ts: Int): Date {
        return Date(ts.toLong() * 1000)
    }

    fun getTimestampFromDate(d: Date?): Int {
        return if (d == null) -1 else (d.time / 1000).toInt()
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

    @Throws(UnsupportedEncodingException::class)
    fun convertStreamToString(`is`: InputStream): String {
        val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
        val sb = StringBuilder()
        var line: String? = null
        try {
            while (true) {
                line = reader.readLine()
                if (line == null) {
                    break
                }

                sb.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }
}