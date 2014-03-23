package com.uksusoff.rock63;

import android.os.AsyncTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.uksusoff.rock63.data.DBHelper;

public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
    
    private DBHelper dbHelper = null;
    
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
