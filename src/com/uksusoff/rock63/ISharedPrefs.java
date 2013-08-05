package com.uksusoff.rock63;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultString;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref.Scope;

@SharedPref(value=Scope.UNIQUE)
public interface ISharedPrefs {

    @DefaultString(Settings.ROCK63_PREFS_THEME_OPT_DARK)
    String theme();
    
}
