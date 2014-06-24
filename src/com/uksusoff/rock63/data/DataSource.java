package com.uksusoff.rock63.data;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.*;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.uksusoff.rock63.InternalPrefs_;
import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.data.entities.Place;
import com.uksusoff.rock63.utils.CommonUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

@EBean(scope = com.googlecode.androidannotations.api.Scope.Singleton)
public class DataSource {
    
    private static final String TAG = "DataSource";

    private static final String NEWS_SOURCE_URL = "http://rock63.ru/api/news";
    private static final String NEWS_SOURCE_URL_FROM_DATE = "http://rock63.ru/api/news";

    private static final String EVENTS_SOURCE_URL = "http://rock63.ru/api/events";

    private static final String PLACES_SOURCE_URL = "http://rock63.ru/api/venues";

    public static final int NO_PLACE_ID = -1;

    private static final long NEWS_LIFETIME_DAYS = 60;

    public static final String ROCK63_OPTIONS_STORE = "rock63_options_store";
    public static final String ROCK63_OPTION_LAST_NEWS_UPDATE = "rock63_options_last_news_update";

    private DBHelper database;
    private INewsDataSourceListener newsListener;
    private IEventDataSourceListener eventListener;

    @RootContext
    Context context;
    
    @Pref
    InternalPrefs_ intPrefs;

    @AfterInject
    public void init() {
        database = (DBHelper) OpenHelperManager.getHelper(context, DBHelper.class);
    }

    public INewsDataSourceListener getNewsListener() {
        return newsListener;
    }

    public void setNewsListener(INewsDataSourceListener newsListener) {
        this.newsListener = newsListener;
    }

    public IEventDataSourceListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(IEventDataSourceListener eventListener) {
        this.eventListener = eventListener;
    }

    public List<NewsItem> getAllNews() throws SQLException {
        return database.getNewsItemDao().queryBuilder().orderBy("date", false).query();
    }

    private static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public void saveImageCache(Bitmap bmp, String cacheName) throws IOException {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/rock63/image_cache";
        File dir = new File(file_path);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, cacheName + ".png");
        FileOutputStream fOut = new FileOutputStream(file);

        bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
    }

    public Bitmap loadImageCache(String cacheName) {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/rock63/image_cache/" + cacheName + ".png";

        Bitmap res = null;

        try {
            File f = new File(file_path);
            FileInputStream s = new FileInputStream(f);
            res = BitmapFactory.decodeStream(s);
        } catch (FileNotFoundException e) {
        }

        return res;
    }

    public void clearOldNews() throws SQLException {

        Date before = new Date();
        before.setTime(before.getTime() - NEWS_LIFETIME_DAYS * 1000 * 60 * 60 * 24);

        DeleteBuilder<NewsItem, Integer> builder = database.getNewsItemDao().deleteBuilder();
        builder.where().le("date", before);
        builder.delete();
    }

    public void refreshNews() {

        (new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... v) {

                SharedPreferences optstore = context.getSharedPreferences(ROCK63_OPTIONS_STORE, 0);
                int lastNewsUpdate = optstore.getInt(ROCK63_OPTION_LAST_NEWS_UPDATE, 0);

                String contents = "";
                URLConnection conn;

                try {
                    URL url = null;
                    if (lastNewsUpdate != 0) {
                        Date d = CommonUtils.getDateFromTimestamp(lastNewsUpdate);
                        url = new URL(String.format(NEWS_SOURCE_URL_FROM_DATE, (new SimpleDateFormat("yyyyy/MM/dd", Locale.getDefault())).format(d)));
                    } else {
                        url = new URL(NEWS_SOURCE_URL);
                    }
                    conn = url.openConnection();

                    InputStream in = conn.getInputStream();
                    contents = convertStreamToString(in);

                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

                if (contents != "") {

                    try {
                        JSONArray news = new JSONArray(contents);

                        clearOldNews();

                        List<Integer> ids = new LinkedList<Integer>();
                        for (NewsItem item : database.getNewsItemDao().queryForAll()) {
                            ids.add(item.getId());
                        }

                        for (int i = 0; i < news.length(); i++) {
                            JSONObject newsItemJson = news.getJSONObject(i);

                            int id = newsItemJson.getInt("id");
                            
                            NewsItem newsItem;
                            if (ids.contains(Integer.valueOf(id))) {
                                newsItem = database.getNewsItemDao().queryForId(id);
                            } else {
                                newsItem = new NewsItem();
                            }

                            newsItem.setId(id);
                            newsItem.setDate(CommonUtils.getDateFromTimestamp(newsItemJson.getInt("date_p")));
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
                            newsItem.setNew(true);

                            database.getNewsItemDao().createOrUpdate(newsItem);
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    SharedPreferences.Editor editor = optstore.edit();
                    editor.putInt(ROCK63_OPTION_LAST_NEWS_UPDATE, lastNewsUpdate);
                    editor.commit();

                    return 0;
                } else {
                    return 1;
                }
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (newsListener != null) {
                    if (result == 0) {
                        newsListener.newsRefreshed(DataSource.this);
                    } else {
                        newsListener.newsRefreshFailed(DataSource.this);
                    }
                }
            }

        }).execute();

    }

    public List<Event> getAllEvents() throws SQLException {
        return database.getEventDao().queryBuilder().orderBy("start", true).query();
    }

    public void refreshEvents() {

        (new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... v) {

                String contents = "";
                URLConnection conn;

                try {
                    conn = new URL(EVENTS_SOURCE_URL).openConnection();

                    InputStream in = conn.getInputStream();
                    contents = convertStreamToString(in);

                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

                if (contents != "") {
                    try {
                        List<Integer> locations = new LinkedList<Integer>();
                        for (Place p : database.getPlaceDao().queryForAll()) {
                            locations.add(p.getId());
                        }

                        database.getEventDao().deleteBuilder().delete();

                        JSONArray events = new JSONArray(contents);

                        for (int i = 0; i < events.length(); i++) {
                            JSONObject eventJson = events.getJSONObject(i);
                            
                            if (eventJson.has("venues_up")) {
                                long lastPlacesUpdate = eventJson.getLong("venues_up");
                                if (intPrefs.lastUpdatedPlaces().get() != lastPlacesUpdate) {
                                    refreshPlacesSync();
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
                            e.setStart(CommonUtils.getDateFromTimestamp(eventJson.getJSONObject("date").getInt("s")));
                            if (eventJson.has("img")) {
                                e.setMediumThumbUrl(eventJson.getJSONObject("img").getString("img_m"));
                            }
                            if (eventJson.getJSONObject("date").has("e"))
                                e.setEnd(CommonUtils.getDateFromTimestamp(eventJson.getJSONObject("date").getInt("e")));
                            if (eventJson.has("v_id")) {
                                Place place = database.getPlaceDao().queryForId(eventJson.getInt("v_id"));
                                if (place == null) {
                                    if (!refreshPlacesSync()) {
                                        return 1;
                                    }
                                    place = database.getPlaceDao().queryForId(eventJson.getInt("v_id"));
                                }
                                e.setPlace(place);
                            } else {
                                e.setPlace(null);
                            }
                            e.setUrl(eventJson.getString("url"));

                            database.getEventDao().create(e);
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    return 0;
                } else
                    return 1;

            }

            @Override
            protected void onPostExecute(Integer result) {
                if (eventListener != null) {
                    if (result == 0) {
                        eventListener.eventsRefreshed(DataSource.this);
                    } else {
                        eventListener.eventsRefreshFailed(DataSource.this);
                    }
                }
            }

        }).execute();
    }
    
    public Event getRelatedEvent(NewsItem item) {
        try {
            return database.getEventDao().queryForId(item.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean refreshPlacesSync() throws MalformedURLException, IOException {

        String contents = "";
        URLConnection conn;

        try {
            conn = new URL(PLACES_SOURCE_URL).openConnection();

            InputStream in = conn.getInputStream();
            contents = convertStreamToString(in);

        } catch (MalformedURLException e) {

        } catch (IOException e) {

        }

        if (contents != "") {

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

            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return true;
        } else
            return false;
    }

}
