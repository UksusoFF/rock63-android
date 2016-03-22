package com.uksusoff.rock63;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

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
                    
                    break;
                case R.id.appThemeRadioLight:
                    
                    theme = Settings.ROCK63_PREFS_THEME_OPT_LIGHT;
                    
                    SettingsActivity.this.getApplication().setTheme(R.style.AppLightTheme);
                    
                    break;
                }
                
                sharedPrefs.theme().put(theme);
                
            }
            
        });
        
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());
        bodyText.setText(Html.fromHtml(getString(R.string.about_body)));
    }

}
