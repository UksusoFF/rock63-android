package com.uksusoff.rock63;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RadioGroup;

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends SherlockActivity {
    
    @Pref
    ISharedPrefs_ sharedPrefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        String theme = sharedPrefs.theme().get(); //preferenceStore.getString(SettingsActivity.ROCK63_PREFS_THEME, SettingsActivity.ROCK63_PREFS_THEME_OPT_DARK);

        if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_DARK)) {
            setTheme(R.style.AppDarkTheme);
        } else if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_LIGHT)) {
            setTheme(R.style.AppLightTheme);
        }
        
        super.onCreate(savedInstanceState);
        
    }
    
    @AfterViews
    void init() {
        
        String theme = sharedPrefs.theme().get();
        
        if (theme==Settings.ROCK63_PREFS_THEME_OPT_DARK)
            ((RadioGroup)findViewById(R.id.appThemeRadio)).check(R.id.appThemeRadioDark);
        else if (theme==Settings.ROCK63_PREFS_THEME_OPT_LIGHT)
            ((RadioGroup)findViewById(R.id.appThemeRadio)).check(R.id.appThemeRadioLight);
        
        ((RadioGroup)findViewById(R.id.appThemeRadio)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                
                String theme = "";
                
                switch (checkedId) {
                case R.id.appThemeRadioDark:
                    
                    theme = Settings.ROCK63_PREFS_THEME_OPT_DARK;
                    
                    SettingsActivity.this.getApplication().setTheme(R.style.AppDarkTheme);
                                        
                    break;
                case R.id.appThemeRadioLight:
                    
                    theme = Settings.ROCK63_PREFS_THEME_OPT_LIGHT;
                    
                    SettingsActivity.this.getApplication().setTheme(R.style.AppLightTheme);
                                        
                    break;
                }
                
                sharedPrefs.theme().put(theme);
                
            }
            
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
                
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
        case R.id.menuAbout:
            
            Info_.intent(this).start();
            
            break;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }*/

}
