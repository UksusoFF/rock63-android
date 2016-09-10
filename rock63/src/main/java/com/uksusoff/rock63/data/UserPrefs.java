package com.uksusoff.rock63.data;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by User on 18.06.2016.
 */
@SharedPref
public interface UserPrefs {

    @DefaultBoolean(true)
    boolean remindWeekBefore();

    @DefaultBoolean(true)
    boolean remindDayBefore();

}
