package com.uksusoff.rock63.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static long delayToHour(int hour) {
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

    public static Date fromTimestamp(int ts) {

        if (ts == -1)
            return null;

        return new Date(((long) ts) * 1000);
    }
}
