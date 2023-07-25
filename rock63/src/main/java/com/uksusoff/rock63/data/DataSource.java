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

    public void newsRefresh() throws NoInternetException, NoContentException {
        final SQLiteDatabase db = database.getWritableDatabase();

        db.beginTransaction();

        try {
            JSONArray news = getEntitiesArray("/news");

            newsCleanUp();

            List<Integer> ids = new LinkedList<>();
            for (NewsItem item : database.getNewsItemsDao().queryForAll()) {
                ids.add(item.getId());
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

    public void eventsRefresh() throws NoInternetException, NoContentException {
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
                        venuesRefresh();
                        intPrefs.lastUpdatedPlaces().put(lastPlacesUpdate);
                    }
                }

                EventItem eventItem = new EventItem();
                eventItem.setId(eventJson.getInt("id"));
                eventItem.setTitle(eventJson.getString("title"));
                if (eventJson.has("desc")) {
                    eventItem.setBody(eventJson.getString("desc"));
                } else {
                    eventItem.setBody("");
                }
                if (eventJson.has("ext_url")) {
                    eventItem.setBody(eventItem.getBody() + eventJson.getString("ext_url"));
                }
                eventItem.setStart(DateUtils.fromTimestamp(eventJson.getJSONObject("date").getInt("s")));
                if (eventJson.has("img")) {
                    eventItem.setMediumThumbUrl(eventJson.getJSONObject("img").getString("img_m"));
                }
                if (eventJson.getJSONObject("date").has("e"))
                    eventItem.setEnd(DateUtils.fromTimestamp(eventJson.getJSONObject("date").getInt("e")));
                if (eventJson.has("v_id")) {
                    eventItem.setVenueItem(database.getVenueItemsDao().queryForId(eventJson.getInt("v_id")));
                } else {
                    eventItem.setVenueItem(null);
                }
                eventItem.setUrl(eventJson.getString("url"));
                eventItem.setNotify(eventJson.has("notify"));

                database.getEventItemsDao().create(eventItem);
            }

            db.setTransactionSuccessful();
        } catch (JSONException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    public EventItem eventGetRelated(NewsItem item) {
        try {
            return database.getEventItemsDao().queryForId(item.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void venuesRefresh() throws NoInternetException, NoContentException {
        try {
            JSONArray venues = getEntitiesArray("/venues");

            database.getVenueItemsDao().deleteBuilder().delete();

            for (int i = 0; i < venues.length(); i++) {
                JSONObject venueJson = venues.getJSONObject(i);

                VenueItem venueItem = new VenueItem();
                venueItem.setId(venueJson.getInt("id"));
                venueItem.setName(venueJson.getString("title"));
                venueItem.setAddress(venueJson.getString("address"));
                if (venueJson.has("site")) {
                    venueItem.setUrl(venueJson.getString("site"));
                }
                if (venueJson.has("phone")) {
                    venueItem.setPhone(venueJson.getString("phone"));
                }
                if (venueJson.has("vk")) {
                    venueItem.setVkUrl(venueJson.getString("vk"));
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
}
