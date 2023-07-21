package com.uksusoff.rock63.ui;

import java.sql.SQLException;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.entities.NewsItem;

import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

@EActivity (R.layout.a_news_detail)
@OptionsMenu(R.menu.menu_detail)
public class NewsDetailActivity extends BaseMenuActivity {
    
    public static final String EXTRA_ITEM_ID = "newsItem";
    
    @Extra(EXTRA_ITEM_ID)
    int newsItemId;

    @ViewById(R.id.news_detail_title)
    TextView title;
    
    @ViewById(R.id.news_detail_body)
    TextView body;
    
    @ViewById(R.id.news_detail_image)
    ImageView image;
    
    private NewsItem newsItem;

    @Override
    protected void init() {
        super.init();
        
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
    
    @OptionsItem(R.id.menu_share)
    void menuShare() {
        shareNews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void shareNews() {
        String body = Html.fromHtml(newsItem.getBody()).toString();
        if (newsItem.getUrl() != null) {
            body += "\n\n" + newsItem.getUrl();
        }

        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(newsItem.getTitle())
                .setText(body)
                .getIntent()
        );
    }

}
