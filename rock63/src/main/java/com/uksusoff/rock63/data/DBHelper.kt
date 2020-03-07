package com.uksusoff.rock63.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.uksusoff.rock63.data.entities.Event
import com.uksusoff.rock63.data.entities.NewsItem
import com.uksusoff.rock63.data.entities.Place
import java.sql.SQLException

class DBHelper(context: Context?) : OrmLiteSqliteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    @get:Throws(SQLException::class)
    var eventDao: Dao<Event, Int>? = null
        get() {
            if (field == null) {
                field = getDao(Event::class.java)
            }
            return field
        }
        private set

    @get:Throws(SQLException::class)
    var newsItemDao: Dao<NewsItem, Int>? = null
        get() {
            if (field == null) {
                field = getDao(NewsItem::class.java)
            }
            return field
        }
        private set

    @get:Throws(SQLException::class)
    var placeDao: Dao<Place, Int>? = null
        get() {
            if (field == null) {
                field = getDao(Place::class.java)
            }
            return field
        }
        private set

    override fun onCreate(db: SQLiteDatabase, connectionSource: ConnectionSource) {
        try {
            TableUtils.createTable(connectionSource, Event::class.java)
            TableUtils.createTable(connectionSource, NewsItem::class.java)
            TableUtils.createTable(connectionSource, Place::class.java)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, connectionSource: ConnectionSource, oldVersion: Int, newVersion: Int) {
        try {
            TableUtils.dropTable<Event, Any>(connectionSource, Event::class.java, true)
            TableUtils.dropTable<NewsItem, Any>(connectionSource, NewsItem::class.java, true)
            TableUtils.dropTable<Place, Any>(connectionSource, Place::class.java, true)
            onCreate(db)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val DATABASE_NAME = "rock63androidclient"
        private const val DATABASE_VERSION = 6
    }
}