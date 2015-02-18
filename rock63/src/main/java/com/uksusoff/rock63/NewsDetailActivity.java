package com.uksusoff.rock63;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.data.entities.NewsItem;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

@EActivity (R.layout.news_detail)
@OptionsMenu(R.menu.menu_event)
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @OptionsItem(R.id.menu_share)
    void menuShare() {
        shareNews();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void shareNews() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        String body = Html.fromHtml(newsItem.getBody()).toString();
        if (newsItem.getUrl() != null) {
            body += "\n\n" + newsItem.getUrl();
        }
        
        intent.putExtra(Intent.EXTRA_SUBJECT, newsItem.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, body);

        startActivity(intent);
    }

}
