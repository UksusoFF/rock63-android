package com.uksusoff.rock63;

import com.flurry.android.FlurryAgent;

import android.content.Context;

public class Flurry {

    public static final String KEY = "Q6B8W886GPHFHNNMM2KQ";
    
    public static void startSession(Context context) {
        FlurryAgent.onStartSession(context, KEY);
    }
    
    public static void endSession(Context context) {
        FlurryAgent.onEndSession(context);
    }
    
    public static void pageView() {
        FlurryAgent.onPageView();
    }
    
    public static void logEvent(String event) {
        FlurryAgent.logEvent(event);
    }
    
    public static void endEvent(String event) {
        FlurryAgent.endTimedEvent(event);
    }
    
}
