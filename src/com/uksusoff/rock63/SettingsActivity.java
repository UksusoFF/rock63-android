package com.uksusoff.rock63;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.RadioGroup;
import android.widget.TextView;

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends BaseActivity {
    
    @Pref
    ISharedPrefs_ sharedPrefs;
    
    @ViewById(R.id.appThemeRadio)
    RadioGroup themeRadio;
    
    @ViewById(R.id.info_body_text)
    TextView bodyText;
    
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
        
        if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_DARK))
            ((RadioGroup)findViewById(R.id.appThemeRadio)).check(R.id.appThemeRadioDark);
        else if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_LIGHT))
            ((RadioGroup)findViewById(R.id.appThemeRadio)).check(R.id.appThemeRadioLight);
        
        themeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                
                String theme = "";
                
                switch (checkedId) {
                case R.id.appThemeRadioDark:
                    
                    theme = Settings.ROCK63_PREFS_THEME_OPT_DARK;
                    
                    SettingsActivity.this.getApplication().setTheme(R.style.AppDarkTheme);
                    
                    Flurry.logEvent(getString(R.string.flurry_change_theme_to_dark));
                    
                    break;
                case R.id.appThemeRadioLight:
                    
                    theme = Settings.ROCK63_PREFS_THEME_OPT_LIGHT;
                    
                    SettingsActivity.this.getApplication().setTheme(R.style.AppLightTheme);
                    
                    Flurry.logEvent(getString(R.string.flurry_change_theme_to_light));
                    
                    break;
                }
                
                sharedPrefs.theme().put(theme);
                
            }
            
        });
        
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());
        bodyText.setText(Html.fromHtml(getString(R.string.about_body)));
    }

}
