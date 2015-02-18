package com.uksusoff.rock63;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.uksusoff.rock63.data.DBHelper;

import android.support.v7.app.ActionBarActivity;

public abstract class BaseActivity extends ActionBarActivity {
    
    private DBHelper dbHelper = null;
    
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
    }

    protected DBHelper getHelper() {
        if (dbHelper == null) {
            dbHelper = (DBHelper)OpenHelperManager.getHelper(this, DBHelper.class);
        }
        return dbHelper;
    }
    
}
