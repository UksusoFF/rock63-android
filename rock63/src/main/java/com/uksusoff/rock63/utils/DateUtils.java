package com.uksusoff.rock63.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by User on 10.09.2016.
 */
public class DateUtils {

    public static int getDateMonthDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static long getDelayToHour(int hour) {
        long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        while (c.getTimeInMillis() < now) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return c.getTimeInMillis() - now;
    }

}
