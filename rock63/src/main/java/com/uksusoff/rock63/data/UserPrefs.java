package com.uksusoff.rock63.data;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface UserPrefs {

    @DefaultBoolean(true)
    boolean remindWeekBefore();

    @DefaultBoolean(true)
    boolean remindDayBefore();

}
