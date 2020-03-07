package com.uksusoff.rock63.data

import org.androidannotations.annotations.sharedpreferences.SharedPref

@SharedPref
interface InternalPrefs {
    fun lastUpdatedPlaces(): Long
}