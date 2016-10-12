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

}
