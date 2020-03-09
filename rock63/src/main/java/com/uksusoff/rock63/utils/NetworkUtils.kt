package com.uksusoff.rock63.utils

import android.util.JsonReader
import com.uksusoff.rock63.exceptions.NoInternetException
import java.io.IOException
import java.net.URL

fun readJsonFromUrl(url:String, handler: (s: JsonReader) -> Unit) {
    try {
        JsonReader(URL(url).openConnection().getInputStream().reader()).use {
            handler(it)
        }
    } catch (e: IOException) {
        //probably it's some kind of internet failure
        throw NoInternetException()
    }
}