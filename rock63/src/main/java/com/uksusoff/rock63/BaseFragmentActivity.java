package com.uksusoff.rock63;

import android.os.AsyncTask;

import android.support.v7.app.ActionBarActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.uksusoff.rock63.data.DBHelper;

public abstract class BaseFragmentActivity extends ActionBarActivity {
    
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
