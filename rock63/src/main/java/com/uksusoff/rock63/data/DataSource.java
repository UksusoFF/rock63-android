package com.uksusoff.rock63.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.uksusoff.rock63.data.entities.EventItem;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.data.entities.VenueItem;
import com.uksusoff.rock63.exceptions.NoContentException;
import com.uksusoff.rock63.exceptions.NoInternetException;
import com.uksusoff.rock63.utils.DateUtils;
import com.uksusoff.rock63.utils.StringUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class DataSource {

    private static final String BASE_API_URL = "https://rock63.ru/api";

    private static final long NEWS_LIFETIME_DAYS = 60;

    private DBHelper database;

    @RootContext
    Context context;

    @Pref
    InternalPrefs_ intPrefs;

    @AfterInject
    public void init() {
        database = OpenHelperManager.getHelper(context, DBHelper.class);
    }

    public List<NewsItem> getAllNews() {
        try {
            return database.getNewsItemsDao().queryBuilder().orderBy("date", false).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void newsCleanUp() throws SQLException {
        Date before = new Date();

        before.setTime(before.getTime() - NEWS_LIFETIME_DAYS * 1000 * 60 * 60 * 24);

        DeleteBuilder<NewsItem, Integer> builder = database.getNewsItemsDao().deleteBuilder();

        builder.where().le("date", before);
        builder.delete();
    }

    private void newsRefresh() throws NoInternetException, NoContentException {
        final SQLiteDatabase db = database.getWritableDatabase();

        db.beginTransaction();

        try {
            JSONArray news = getEntitiesArray("/news");

            newsCleanUp();

            List<Integer> ids = new LinkedList<>();
            for (NewsItem newsItem : database.getNewsItemsDao().queryForAll()) {
                ids.add(newsItem.id);
            }

            for (int i = 0; i < news.length(); i++) {
                JSONObject newsItemJson = news.getJSONObject(i);

                int id = newsItemJson.getInt("id");

                NewsItem newsItem;
                if (ids.contains(id)) {
                    newsItem = database.getNewsItemsDao().queryForId(id);
                } else {
                    newsItem = new NewsItem();
                }

                newsItem.id = newsItemJson.getInt("id");
                newsItem.title = newsItemJson.getString("title");
                newsItem.url = newsItemJson.getString("url");
                newsItem.body = newsItemJson.has("desc") ? newsItemJson.getString("desc") : null;
                newsItem.ext = newsItemJson.has("ext_url") ? newsItemJson.getString("ext_url") : null;
                newsItem.date = DateUtils.fromTimestamp(newsItemJson.getInt("date_p"));
                newsItem.thumbnailSmall = newsItemJson.has("img") ? newsItemJson.getJSONObject("img").getString("img_s") : null;
                newsItem.thumbnailMiddle = newsItemJson.has("img") ? newsItemJson.getJSONObject("img").getString("img_m") : null;
                newsItem.isNew = true;

                database.getNewsItemsDao().createOrUpdate(newsItem);
            }

            db.setTransactionSuccessful();
        } catch (JSONException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public List<EventItem> eventsGetAll(boolean ascending) {
        try {
            return database.getEventItemsDao().queryBuilder().orderBy("start", ascending).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void eventsRefresh() throws NoInternetException, NoContentException {
        final SQLiteDatabase db = database.getWritableDatabase();

        db.beginTransaction();

        try {
            JSONArray events = getEntitiesArray("/events");

            database.getEventItemsDao().deleteBuilder().delete();

            for (int i = 0; i < events.length(); i++) {
                JSONObject eventJson = events.getJSONObject(i);

                if (eventJson.has("venues_up")) {
                    long lastPlacesUpdate = eventJson.getLong("venues_up");
                    if (intPrefs.lastUpdatedPlaces().get() != lastPlacesUpdate) {
                        this.venuesRefresh();
                        intPrefs.lastUpdatedPlaces().put(lastPlacesUpdate);
                    }
                }

                JSONObject date = eventJson.getJSONObject("date");

                EventItem eventItem = new EventItem();
                eventItem.id = eventJson.getInt("id");
                eventItem.title = eventJson.getString("title");
                eventItem.url = eventJson.getString("url");
                eventItem.body = eventJson.has("desc") ? eventJson.getString("desc") : null;
                eventItem.ext = eventJson.has("ext_url") ? eventJson.getString("ext_url") : null;
                eventItem.notify = eventJson.has("notify");
                eventItem.start = DateUtils.fromTimestamp(date.getInt("s"));
                eventItem.end = date.has("e") ? DateUtils.fromTimestamp(date.getInt("e")) : null;
                eventItem.thumbnailSmall = eventJson.has("img") ? eventJson.getJSONObject("img").getString("img_s") : null;
                eventItem.thumbnailMiddle = eventJson.has("img") ? eventJson.getJSONObject("img").getString("img_m") : null;
                if (eventJson.has("v_id")) {
                    eventItem.setVenueItem(database.getVenueItemsDao().queryForId(eventJson.getInt("v_id")));
                } else {
                    eventItem.setVenueItem(null);
                }

                database.getEventItemsDao().create(eventItem);
            }

            db.setTransactionSuccessful();
        } catch (JSONException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public EventItem eventGetRelated(NewsItem newsItem) {
        try {
            return database.getEventItemsDao().queryForId(newsItem.id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void venuesRefresh() throws NoInternetException, NoContentException {
        try {
            JSONArray venues = getEntitiesArray("/venues");

            database.getVenueItemsDao().deleteBuilder().delete();

            for (int i = 0; i < venues.length(); i++) {
                JSONObject venueJson = venues.getJSONObject(i);

                VenueItem venueItem = new VenueItem();
                venueItem.id = venueJson.getInt("id");
                venueItem.title = venueJson.getString("title");
                venueItem.address = venueJson.getString("address");
                if (venueJson.has("site") && !venueJson.getString("site").isEmpty()) {
                    venueItem.url = venueJson.getString("site");
                }
                if (venueJson.has("phone")) {
                    venueItem.phone = venueJson.getString("phone");
                }
                if (venueJson.has("vk") && !venueJson.getString("vk").isEmpty()) {
                    venueItem.vk = venueJson.getString("vk");
                }
                if (venueJson.has("latitude") && venueJson.has("longitude")) {
                    venueItem.latitude = venueJson.getString("latitude");
                    venueItem.longitude = venueJson.getString("longitude");
                }

                database.getVenueItemsDao().create(venueItem);
            }
        } catch (JSONException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONArray getEntitiesArray(String endpoint) throws NoInternetException, NoContentException {
        String contents;

        try {
            URLConnection conn = new URL(BASE_API_URL + endpoint).openConnection();
            InputStream in = conn.getInputStream();
            contents = StringUtils.fromStream(in);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new NoInternetException();
        }

        if (contents.isEmpty()) {
            throw new NoContentException();
        }

        try {
            return new JSONArray(contents);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sourcesRefresh() throws NoInternetException, NoContentException {
        this.venuesRefresh();
        this.eventsRefresh();
        this.newsRefresh();
    }
}
