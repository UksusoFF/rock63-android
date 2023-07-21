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

private const val DATABASE_NAME = "rock63androidclient"
private const val DATABASE_VERSION = 6

class DatabaseComponent(context: Context?) : OrmLiteSqliteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val events: Dao<Event, Int> by lazy {
        getDao(Event::class.java) as Dao<Event, Int>
    }

    val news: Dao<NewsItem, Int> by lazy {
        getDao(NewsItem::class.java) as Dao<NewsItem, Int>
    }

    val places: Dao<Place, Int> by lazy {
        getDao(Place::class.java) as Dao<Place, Int>
    }

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
}