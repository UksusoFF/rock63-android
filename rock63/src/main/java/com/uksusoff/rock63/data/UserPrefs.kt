package com.uksusoff.rock63.data

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean
import org.androidannotations.annotations.sharedpreferences.SharedPref

/**
 * Created by Vyacheslav Vodyanov on 18.06.2016.
 */
@SharedPref
interface UserPrefs {
    @DefaultBoolean(true)
    fun remindWeekBefore(): Boolean

    @DefaultBoolean(true)
    fun remindDayBefore(): Boolean
}