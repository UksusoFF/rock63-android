package com.uksusoff.rock63;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
    
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
