package com.uksusoff.rock63;

import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface InternalPrefs {

    long lastUpdatedPlaces();
    
}
