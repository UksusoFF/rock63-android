package com.uksusoff.rock63.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.utils.StringUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EActivity(R.layout.a_list)
public class NewsListActivity extends ItemListActivity {

    @Bean
    DataSource source;

    private static boolean isRefreshing = false;
    private static NewsListActivity activeActivity;

    @Override
    protected boolean isRefreshing() {
        return isRefreshing;
    }

    @Override
    protected void setRefreshing(boolean refreshing) {
        isRefreshing = refreshing;
    }

    @Override
    protected ItemListActivity getActiveActivity() {
        return activeActivity;
    }

    @Override
    protected void setActiveActivity(ItemListActivity activity) {
        activeActivity = (NewsListActivity) activity;
    }

    @Override
    protected int getEmptyListTextResId() {
        return R.string.news_no_item_text;
    }

    @Override
    protected ListAdapter createAdapterFromStorageItems() {

        List<NewsItem> news;
        try {
            news = source.getAllNews();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < news.size(); i++) {
            NewsItem item = news.get(i);
            Map<String, Object> datum = new HashMap<>(3);
            datum.put("title", item.getTitle());
            datum.put("text", StringUtils.crop(StringUtils.fromHtml(item.getBody()), 50, true));
            datum.put("imageUrl", item.getSmallThumbUrl());
            datum.put("obj", item);

            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                com.uksusoff.rock63.R.layout.i_news_item,
                new String[]{"title", "text", "imageUrl"},
                new int[]{com.uksusoff.rock63.R.id.newsTitle,
                        com.uksusoff.rock63.R.id.newsDescription,
                        com.uksusoff.rock63.R.id.newsImageView});

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if ((view instanceof ImageView) && (data instanceof String)) {
                    UrlImageViewHelper.setUrlDrawable(
                            (ImageView) view,
                            (String) data,
                            R.drawable.news_no_image
                    );

                    return true;
                }
                return false;
            }
        });

        return adapter;
    }

    @Override
    protected void refreshItemStorage() throws DataSource.NoInternetException {
        source.refreshNews();
    }

    @ItemClick(R.id.list)
    public void newsItemClicked(Map<String, Object> item) {
        NewsItem newsItem = (NewsItem) item.get("obj");
        Event related = source.getRelatedEvent(newsItem);
        if (related == null) {
            NewsDetailActivity_.intent(this).newsItemId(newsItem.getId()).start();
        } else {
            EventDetailActivity_.intent(this).eventId(related.getId()).start();
        }
    }

}
