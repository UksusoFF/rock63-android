package com.uksusoff.rock63;

import android.os.AsyncTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
    
    @Override
    protected void onStart() {
        
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }
            
        }).execute(null, null, null);
        
        super.onStart();
        
        Flurry.startSession(BaseFragmentActivity.this);
        
        Flurry.pageView();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        Flurry.endSession(this);
    }
    
}
