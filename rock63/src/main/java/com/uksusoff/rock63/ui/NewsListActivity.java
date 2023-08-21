package com.uksusoff.rock63.ui;

import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.entities.EventItem;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.exceptions.NoContentException;
import com.uksusoff.rock63.exceptions.NoInternetException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EActivity(R.layout.a_list)
public class NewsListActivity extends AbstractListActivity {

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
    protected AbstractListActivity getActiveActivity() {
        return activeActivity;
    }

    @Override
    protected void setActiveActivity(AbstractListActivity activity) {
        activeActivity = (NewsListActivity) activity;
    }

    @Override
    protected int getEmptyListTextResId() {
        return R.string.news_no_item_text;
    }

    @Override
    protected ListAdapter createAdapterFromStorageItems() {
        List<Map<String, Object>> data = new ArrayList<>();

        for (NewsItem newsItem : source.getAllNews()) {
            Map<String, Object> datum = new HashMap<>(3);

            datum.put("title", newsItem.title);
            datum.put("text", newsItem.getShortDescriptionText());
            datum.put("imageUrl", newsItem.thumbnailSmall);
            datum.put("obj", newsItem);

            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                com.uksusoff.rock63.R.layout.i_news_item,
                new String[]{"title", "text", "imageUrl"},
                new int[]{
                        com.uksusoff.rock63.R.id.newsTitle,
                        com.uksusoff.rock63.R.id.newsDescription,
                        com.uksusoff.rock63.R.id.newsImageView
                }
        );

        adapter.setViewBinder((view, viewData, textRepresentation) -> {
            if ((view instanceof ImageView) && (viewData instanceof String)) {
                UrlImageViewHelper.setUrlDrawable(
                        (ImageView) view,
                        (String) viewData,
                        R.drawable.news_no_image
                );

                return true;
            }

            return false;
        });

        return adapter;
    }

    @Override
    protected void refreshItemStorage() throws NoInternetException, NoContentException {
        source.sourcesRefresh();
    }

    @ItemClick(R.id.list)
    public void newsItemClicked(Map<String, Object> item) {
        NewsItem newsItem = (NewsItem) item.get("obj");
        EventItem related = source.eventGetRelated(newsItem);
        if (related == null) {
            NewsDetailActivity_.intent(this).newsItemId(newsItem.id).start();
        } else {
            EventDetailActivity_.intent(this).eventId(related.id).start();
        }
    }

}
