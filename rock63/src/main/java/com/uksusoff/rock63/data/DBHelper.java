package com.uksusoff.rock63.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.uksusoff.rock63.data.entities.EventItem;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.data.entities.VenueItem;

import java.sql.SQLException;

public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "rock63androidclient";

    private static final int DATABASE_VERSION = 8;

    private Dao<EventItem, Integer> eventItemsDao;
    private Dao<NewsItem, Integer> newsItemsDao;
    private Dao<VenueItem, Integer> venueItemsDao;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, EventItem.class);
            TableUtils.createTable(connectionSource, NewsItem.class);
            TableUtils.createTable(connectionSource, VenueItem.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, EventItem.class, true);
            TableUtils.dropTable(connectionSource, NewsItem.class, true);
            TableUtils.dropTable(connectionSource, VenueItem.class, true);
            onCreate(db);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dao<EventItem, Integer> getEventItemsDao() throws SQLException {
        if (eventItemsDao == null) {
            eventItemsDao = getDao(EventItem.class);
        }
        return eventItemsDao;
    }

    public Dao<NewsItem, Integer> getNewsItemsDao() throws SQLException {
        if (newsItemsDao == null) {
            newsItemsDao = getDao(NewsItem.class);
        }
        return newsItemsDao;
    }

    public Dao<VenueItem, Integer> getVenueItemsDao() throws SQLException {
        if (venueItemsDao == null) {
            venueItemsDao = getDao(VenueItem.class);
        }
        return venueItemsDao;
    }
}
