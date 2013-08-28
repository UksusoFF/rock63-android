package com.uksusoff.rock63;

import android.app.Activity;

public abstract class BaseActivity extends Activity {
    
    @Override
    protected void onStart() {
        super.onStart();
        
        Flurry.startSession(this);
        
        Flurry.pageView();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        Flurry.endSession(this);
    }
    
}
