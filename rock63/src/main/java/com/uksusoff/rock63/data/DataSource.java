package com.uksusoff.rock63.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.data.entities.Place;
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
            return database.getNewsItemDao().queryBuilder().orderBy("date", false).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void newsCleanUp() throws SQLException {
        Date before = new Date();

        before.setTime(before.getTime() - NEWS_LIFETIME_DAYS * 1000 * 60 * 60 * 24);

        DeleteBuilder<NewsItem, Integer> builder = database.getNewsItemDao().deleteBuilder();

        builder.where().le("date", before);
        builder.delete();
    }

    public void newsRefresh() throws NoInternetException {
        String contents;
        URLConnection conn;

        try {
            conn = new URL(BASE_API_URL + "/news").openConnection();

            InputStream in = conn.getInputStream();
            contents = StringUtils.fromStream(in);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new NoInternetException();
        }

        if (!contents.isEmpty()) {

            final SQLiteDatabase db = database.getWritableDatabase();
            db.beginTransaction();

            try {
                JSONArray news = new JSONArray(contents);

                newsCleanUp();

                List<Integer> ids = new LinkedList<>();
                for (NewsItem item : database.getNewsItemDao().queryForAll()) {
                    ids.add(item.getId());
                }

                for (int i = 0; i < news.length(); i++) {
                    JSONObject newsItemJson = news.getJSONObject(i);

                    int id = newsItemJson.getInt("id");

                    NewsItem newsItem;
                    if (ids.contains(id)) {
                        newsItem = database.getNewsItemDao().queryForId(id);
                    } else {
                        newsItem = new NewsItem();
                    }

                    newsItem.setId(id);
                    newsItem.setDate(DateUtils.fromTimestamp(newsItemJson.getInt("date_p")));
                    if (newsItemJson.has("img")) {
                        newsItem.setSmallThumbUrl(newsItemJson.getJSONObject("img").getString("img_s"));
                        newsItem.setMediumThumbUrl(newsItemJson.getJSONObject("img").getString("img_m"));
                    }
                    newsItem.setTitle(newsItemJson.getString("title"));
                    String body = null;
                    if (newsItemJson.has("desc")) {
                        body = newsItemJson.getString("desc");
                    }
                    if (newsItemJson.has("ext_url")) {
                        body = body == null ? "" : body + " ";
                        body += newsItemJson.getString("ext_url");
                    }
                    newsItem.setBody(body);
                    if (newsItemJson.has("url")) {
                        newsItem.setUrl(newsItemJson.getString("url"));
                    }
                    newsItem.setNew(true);

                    database.getNewsItemDao().createOrUpdate(newsItem);
                }

                db.setTransactionSuccessful();

            } catch (JSONException | SQLException e) {
                throw new RuntimeException(e);
            } finally {
                db.endTransaction();
            }
        }
    }

    public List<Event> eventsGetAll(boolean ascending) {
        try {
            return database.getEventDao().queryBuilder().orderBy("start", ascending).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void eventsRefresh() throws NoInternetException {

        String contents;
        URLConnection conn;

        try {
            conn = new URL(BASE_API_URL + "/events").openConnection();

            InputStream in = conn.getInputStream();
            contents = StringUtils.fromStream(in);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new NoInternetException();
        }

        if (!contents.isEmpty()) {

            final SQLiteDatabase db = database.getWritableDatabase();
            db.beginTransaction();

            try {

                database.getEventDao().deleteBuilder().delete();

                JSONArray events = new JSONArray(contents);

                for (int i = 0; i < events.length(); i++) {
                    JSONObject eventJson = events.getJSONObject(i);

                    if (eventJson.has("venues_up")) {
                        long lastPlacesUpdate = eventJson.getLong("venues_up");
                        if (intPrefs.lastUpdatedPlaces().get() != lastPlacesUpdate) {
                            venuesRefresh();
                            intPrefs.lastUpdatedPlaces().put(lastPlacesUpdate);
                        }
                    }

                    Event e = new Event();
                    e.setId(eventJson.getInt("id"));
                    e.setTitle(eventJson.getString("title"));
                    if (eventJson.has("desc")) {
                        e.setBody(eventJson.getString("desc"));
                    } else {
                        e.setBody("");
                    }
                    if (eventJson.has("ext_url")) {
                        e.setBody(e.getBody() + eventJson.getString("ext_url"));
                    }
                    e.setStart(DateUtils.fromTimestamp(eventJson.getJSONObject("date").getInt("s")));
                    if (eventJson.has("img")) {
                        e.setMediumThumbUrl(eventJson.getJSONObject("img").getString("img_m"));
                    }
                    if (eventJson.getJSONObject("date").has("e"))
                        e.setEnd(DateUtils.fromTimestamp(eventJson.getJSONObject("date").getInt("e")));
                    if (eventJson.has("v_id")) {
                        e.setPlace(database.getPlaceDao().queryForId(eventJson.getInt("v_id")));
                    } else {
                        e.setPlace(null);
                    }
                    e.setUrl(eventJson.getString("url"));
                    e.setNotify(eventJson.has("notify"));

                    database.getEventDao().create(e);
                }

                db.setTransactionSuccessful();

            } catch (JSONException | SQLException e) {
                throw new RuntimeException(e);
            } finally {
                db.endTransaction();
            }
        }
    }

    public Event eventGetRelated(NewsItem item) {
        try {
            return database.getEventDao().queryForId(item.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void venuesRefresh() throws NoInternetException {
        String contents;
        URLConnection conn;

        try {
            conn = new URL(BASE_API_URL + "/venues").openConnection();

            InputStream in = conn.getInputStream();
            contents = StringUtils.fromStream(in);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new NoInternetException();
        }

        if (!contents.isEmpty()) {

            try {
                JSONArray places = new JSONArray(contents);

                database.getPlaceDao().deleteBuilder().delete();

                for (int i = 0; i < places.length(); i++) {
                    JSONObject placeJson = places.getJSONObject(i);

                    Place place = new Place();
                    place.setId(placeJson.getInt("id"));
                    place.setName(placeJson.getString("title"));
                    place.setAddress(placeJson.getString("address"));
                    if (placeJson.has("site")) {
                        place.setUrl(placeJson.getString("site"));
                    }
                    if (placeJson.has("phone")) {
                        place.setPhone(placeJson.getString("phone"));
                    }
                    if (placeJson.has("vk")) {
                        place.setVkUrl(placeJson.getString("vk"));
                    }

                    database.getPlaceDao().create(place);
                }

            } catch (JSONException | SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
