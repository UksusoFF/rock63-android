package com.uksusoff.rock63.data

import android.content.Context
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.uksusoff.rock63.data.entities.Event
import com.uksusoff.rock63.data.entities.NewsItem
import com.uksusoff.rock63.data.entities.Place
import com.uksusoff.rock63.utils.CommonUtils
import org.androidannotations.annotations.AfterInject
import org.androidannotations.annotations.EBean
import org.androidannotations.annotations.RootContext
import org.androidannotations.annotations.sharedpreferences.Pref
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

@EBean(scope = EBean.Scope.Singleton)
open class DataSource {

    private lateinit var database: DBHelper

    @RootContext
    protected lateinit var context: Context
    @Pref
    protected lateinit var intPrefs: InternalPrefs_

    @AfterInject
    fun init() {
        database = OpenHelperManager.getHelper(context, DBHelper::class.java)
    }

    @get:Throws(SQLException::class)
    val allNews: List<NewsItem>
        get() = database.newsItemDao!!.queryBuilder().orderBy("date", false).query()

    @Throws(SQLException::class)
    fun clearOldNews() {
        val before = Date()
        before.time = before.time - NEWS_LIFETIME_DAYS * 1000 * 60 * 60 * 24
        val builder = database.newsItemDao!!.deleteBuilder()
        builder.where().le("date", before)
        builder.delete()
    }

    @Throws(NoInternetException::class)
    fun refreshNews() {
        val optstore = context.getSharedPreferences(ROCK63_OPTIONS_STORE, 0)
        val lastNewsUpdate = optstore.getInt(ROCK63_OPTION_LAST_NEWS_UPDATE, 0)
        val contents: String
        val conn: URLConnection
        try {
            val url: URL
            url = if (lastNewsUpdate != 0) {
                val d = CommonUtils.getDateFromTimestamp(lastNewsUpdate)
                URL(String.format(NEWS_SOURCE_URL_FROM_DATE, SimpleDateFormat("yyyyy/MM/dd", Locale.getDefault()).format(d)))
            } else {
                URL(NEWS_SOURCE_URL)
            }
            conn = url.openConnection()
            val `in` = conn.getInputStream()
            contents = CommonUtils.convertStreamToString(`in`)
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw NoInternetException()
        }
        if (!contents.isEmpty()) {
            val db = database.writableDatabase
            db.beginTransaction()
            try {
                val news = JSONArray(contents)
                clearOldNews()
                val ids: MutableList<Int> = LinkedList()
                for (item in database.newsItemDao!!.queryForAll()) {
                    ids.add(item.id)
                }
                for (i in 0 until news.length()) {
                    val newsItemJson = news.getJSONObject(i)
                    val id = newsItemJson.getInt("id")
                    var newsItem: NewsItem
                    newsItem = if (ids.contains(id)) {
                        database.newsItemDao!!.queryForId(id)
                    } else {
                        NewsItem()
                    }
                    newsItem.id = id
                    newsItem.date = CommonUtils.getDateFromTimestamp(newsItemJson.getInt("date_p"))
                    if (newsItemJson.has("img")) {
                        newsItem.smallThumbUrl = newsItemJson.getJSONObject("img").getString("img_s")
                        newsItem.mediumThumbUrl = newsItemJson.getJSONObject("img").getString("img_m")
                    }
                    newsItem.title = newsItemJson.getString("title")
                    var body: String? = null
                    if (newsItemJson.has("desc")) {
                        body = newsItemJson.getString("desc")
                    }
                    if (newsItemJson.has("ext_url")) {
                        body = if (body == null) "" else "$body "
                        body += newsItemJson.getString("ext_url")
                    }
                    newsItem.body = body
                    if (newsItemJson.has("url")) {
                        newsItem.url = newsItemJson.getString("url")
                    }
                    newsItem.isNew = true
                    database.newsItemDao!!.createOrUpdate(newsItem)
                }
                db.setTransactionSuccessful()
            } catch (e: JSONException) {
                throw RuntimeException(e)
            } catch (e: SQLException) {
                throw RuntimeException(e)
            } finally {
                db.endTransaction()
            }
            val editor = optstore.edit()
            editor.putInt(ROCK63_OPTION_LAST_NEWS_UPDATE, lastNewsUpdate)
            editor.apply()
        }
    }

    fun getAllEvents(ascending: Boolean): List<Event> {
        return try {
            database.eventDao!!.queryBuilder().orderBy("start", ascending).query()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    val allEvents: List<Event>
        get() = getAllEvents(true)

    @Throws(NoInternetException::class)
    fun refreshEvents() {
        val contents: String
        val conn: URLConnection
        try {
            conn = URL(EVENTS_SOURCE_URL).openConnection()
            val `in` = conn.getInputStream()
            contents = CommonUtils.convertStreamToString(`in`)
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw NoInternetException()
        }
        if (!contents.isEmpty()) {
            val db = database.writableDatabase
            db.beginTransaction()
            try {
                database.eventDao!!.deleteBuilder().delete()
                val events = JSONArray(contents)
                for (i in 0 until events.length()) {
                    val eventJson = events.getJSONObject(i)
                    if (eventJson.has("venues_up")) {
                        val lastPlacesUpdate = eventJson.getLong("venues_up")
                        if (intPrefs.lastUpdatedPlaces().get() != lastPlacesUpdate) {
                            refreshPlacesSync()
                            intPrefs.lastUpdatedPlaces().put(lastPlacesUpdate)
                        }
                    }
                    val e = Event()
                    e.id = eventJson.getInt("id")
                    e.title = eventJson.getString("title")
                    if (eventJson.has("desc")) {
                        e.body = eventJson.getString("desc")
                    } else {
                        e.body = ""
                    }
                    if (eventJson.has("ext_url")) {
                        e.body = e.body + eventJson.getString("ext_url")
                    }
                    e.start = CommonUtils.getDateFromTimestamp(eventJson.getJSONObject("date").getInt("s"))
                    if (eventJson.has("img")) {
                        e.mediumThumbUrl = eventJson.getJSONObject("img").getString("img_m")
                    }
                    if (eventJson.getJSONObject("date").has("e")) e.end = CommonUtils.getDateFromTimestamp(eventJson.getJSONObject("date").getInt("e"))
                    if (eventJson.has("v_id")) {
                        e.place = database.placeDao!!.queryForId(eventJson.getInt("v_id"))
                    } else {
                        e.place = null
                    }
                    e.url = eventJson.getString("url")
                    e.isNotify = eventJson.has("notify")
                    database.eventDao!!.create(e)
                }
                db.setTransactionSuccessful()
            } catch (e: JSONException) {
                throw RuntimeException(e)
            } catch (e: SQLException) {
                throw RuntimeException(e)
            } finally {
                db.endTransaction()
            }
        }
    }

    fun getRelatedEvent(item: NewsItem): Event {
        return try {
            database.eventDao!!.queryForId(item.id)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    @Throws(NoInternetException::class)
    fun refreshPlacesSync(): Boolean {
        val contents: String
        val conn: URLConnection
        try {
            conn = URL(PLACES_SOURCE_URL).openConnection()
            val `in` = conn.getInputStream()
            contents = CommonUtils.convertStreamToString(`in`)
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw NoInternetException()
        }
        return if (!contents.isEmpty()) {
            try {
                val places = JSONArray(contents)
                database.placeDao!!.deleteBuilder().delete()
                for (i in 0 until places.length()) {
                    val placeJson = places.getJSONObject(i)
                    val place = Place()
                    place.id = placeJson.getInt("id")
                    place.name = placeJson.getString("title")
                    place.address = placeJson.getString("address")
                    if (placeJson.has("site")) {
                        place.url = placeJson.getString("site")
                    }
                    if (placeJson.has("phone")) {
                        place.phone = placeJson.getString("phone")
                    }
                    if (placeJson.has("vk")) {
                        place.vkUrl = placeJson.getString("vk")
                    }
                    database.placeDao!!.create(place)
                }
            } catch (e: JSONException) {
                throw RuntimeException(e)
            } catch (e: SQLException) {
                throw RuntimeException(e)
            }
            true
        } else false
    }

    class NoInternetException : Exception()
    companion object {
        private const val TAG = "DataSource"
        private const val NEWS_SOURCE_URL = "http://rock63.ru/api/news"
        private const val NEWS_SOURCE_URL_FROM_DATE = "http://rock63.ru/api/news"
        private const val EVENTS_SOURCE_URL = "http://rock63.ru/api/events"
        private const val PLACES_SOURCE_URL = "http://rock63.ru/api/venues"
        const val NO_PLACE_ID = -1
        private const val NEWS_LIFETIME_DAYS: Long = 60
        const val ROCK63_OPTIONS_STORE = "rock63_options_store"
        const val ROCK63_OPTION_LAST_NEWS_UPDATE = "rock63_options_last_news_update"
    }
}