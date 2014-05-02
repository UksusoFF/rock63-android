package com.uksusoff.rock63.data;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.data.entities.NewsItem;
import com.uksusoff.rock63.data.entities.Place;

public class DBHelper extends OrmLiteSqliteOpenHelper {
    
    private static final String DATABASE_NAME = "rock63androidclient";
    private static final int DATABASE_VERSION = 4;
    private Dao<Event, Integer> eventDao;
    private Dao<NewsItem, Integer> newsItemDao;
    private Dao<Place, Integer> placeDao;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Event.class);
            TableUtils.createTable(connectionSource, NewsItem.class);
            TableUtils.createTable(connectionSource, Place.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Event.class, true);
            TableUtils.dropTable(connectionSource, NewsItem.class, true);
            TableUtils.dropTable(connectionSource, Place.class, true);
            onCreate(db);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Dao<Event, Integer> getEventDao() throws SQLException {
        if (eventDao == null) {
            eventDao = getDao(Event.class);
        }
        return eventDao;
    }
    
    public Dao<NewsItem, Integer> getNewsItemDao() throws SQLException {
        if (newsItemDao == null) {
            newsItemDao = getDao(NewsItem.class);
        }
        return newsItemDao;
    }

    public Dao<Place, Integer> getPlaceDao() throws SQLException {
        if (placeDao == null) {
            placeDao = getDao(Place.class);
        }
        return placeDao;
    }

}
