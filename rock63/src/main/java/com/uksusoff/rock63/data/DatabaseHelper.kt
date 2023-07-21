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

class DatabaseHelper(context: Context?) : OrmLiteSqliteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val events: Dao<Event, Int> by lazy(fun():Dao<Event, Int> {
        return getDao(Event::class.java)
    })

    val news: Dao<NewsItem, Int> by lazy(fun():Dao<NewsItem, Int>{
        return getDao(NewsItem::class.java)
    })

    val places: Dao<Place, Int> by lazy(fun():Dao<Place, Int> {
        return getDao(Place::class.java)
    })

    public fun executeInTransaction(handler: (database: DatabaseHelper) -> Unit) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            handler(this)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
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