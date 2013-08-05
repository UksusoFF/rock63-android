package com.uksusoff.rock63;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.uksusoff.rock63.ISharedPrefs_;

@EActivity(R.layout.activity_info)
public class Info extends Activity {

    @Pref
    ISharedPrefs_ sharedPrefs;
    
    @ViewById(R.id.info_body_text)
    TextView bodyText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        String theme = sharedPrefs.theme().get(); //getSharedPreferences(SettingsActivity.ROCK63_PREFS, 0).getString(SettingsActivity.ROCK63_PREFS_THEME, SettingsActivity.ROCK63_PREFS_THEME_OPT_DARK);

        if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_DARK)) {
            setTheme(R.style.AppDarkTheme);
        } else if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_LIGHT)) {
            setTheme(R.style.AppLightTheme);
        }
        
        super.onCreate(savedInstanceState);
    }
    
    @AfterViews
    void init() {
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());
        bodyText.setText(Html.fromHtml(getString(R.string.about_body)));
    }

}
