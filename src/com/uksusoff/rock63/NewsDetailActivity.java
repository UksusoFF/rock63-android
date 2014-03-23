package com.uksusoff.rock63;

import java.sql.SQLException;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.data.entities.NewsItem;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

@EActivity (R.layout.news_detail)
public class NewsDetailActivity extends BaseActivity {
    
    public static final String EXTRA_ITEM_ID = "newsItem";
    
    @Extra(EXTRA_ITEM_ID)
    int newsItemId;
    
    @Pref
    ISharedPrefs_ sharedPrefs;
    
    @ViewById(R.id.news_detail_title)
    TextView title;
    
    @ViewById(R.id.news_detail_body)
    TextView body;
    
    @ViewById(R.id.news_detail_image)
    ImageView image;
    
    private NewsItem newsItem;
    
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
        
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
          return;
        }
        
        try {
            newsItem = getHelper().getNewsItemDao().queryForId(newsItemId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
                
        title.setText(newsItem.getTitle());
        body.setMovementMethod(LinkMovementMethod.getInstance());
        body.setText(Html.fromHtml(newsItem.getBody()));
        
        UrlImageViewHelper.setUrlDrawable(image, newsItem.getMediumThumbUrl(), R.drawable.news_medium_placeholder);
    }

}
