package com.uksusoff.rock63.utils

import java.util.*

/**
 * Created by Vyacheslav Vodyanov on 10.09.2016.
 */
object DateUtils {
    fun getDateMonthDay(date: Date?): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal[Calendar.DAY_OF_MONTH]
    }

    fun getDelayToHour(hour: Int): Long {
        val now = System.currentTimeMillis()
        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = hour
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        while (c.timeInMillis < now) {
            c.add(Calendar.DAY_OF_MONTH, 1)
        }
        return c.timeInMillis - now
    }
}