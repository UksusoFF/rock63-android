package com.uksusoff.rock63;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.uksusoff.rock63.data.DBHelper;

import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

@EActivity
public abstract class BaseActivity extends AppCompatActivity {
    
    private DBHelper dbHelper = null;
    
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

    @UiThread
    public void showWarning(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showWarning(@StringRes int resId) {
        showWarning(getString(resId));
    }
    
}
