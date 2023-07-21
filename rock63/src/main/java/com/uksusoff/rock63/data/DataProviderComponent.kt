package com.uksusoff.rock63.data

import android.content.Context
import android.util.JsonReader
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.uksusoff.rock63.data.entities.Event
import com.uksusoff.rock63.data.entities.NewsItem
import com.uksusoff.rock63.data.entities.Place
import com.uksusoff.rock63.utils.CommonUtils
import org.androidannotations.annotations.AfterInject
import org.androidannotations.annotations.EBean
import org.androidannotations.annotations.RootContext
import org.androidannotations.annotations.sharedpreferences.Pref
import java.io.InputStreamReader
import java.lang.Exception
import java.net.URL
import java.util.*

@EBean(scope = EBean.Scope.Singleton)
open class DataProviderComponent {
    class NoInternetException : Exception() {}

    companion object {
        private const val NEWS_SOURCE_URL = "http://rock63.ru/api/news"
        private const val EVENTS_SOURCE_URL = "http://rock63.ru/api/events"
        private const val PLACES_SOURCE_URL = "http://rock63.ru/api/venues"

        private const val NEWS_LIFETIME_DAYS: Long = 60
    }

    private lateinit var database: DatabaseComponent

    @RootContext
    protected lateinit var context: Context
    @Pref
    protected lateinit var intPrefs: InternalPrefs_

    @AfterInject
    fun init() {
        database = OpenHelperManager.getHelper(context, DatabaseComponent::class.java)
    }

    val allNews: List<NewsItem>
        get() = database.news
                .queryBuilder()
                .orderBy(NewsItem.FIELD_DATE, false)
                .query()

    val allEvents: List<Event>
        get() = getAllEvents(true)

    private fun clearOldNews() {
        val before = Date()
        before.time -= NEWS_LIFETIME_DAYS * 1000 * 60 * 60 * 24

        database.news.deleteBuilder()
                .apply { this.where().le(NewsItem.FIELD_DATE, before) }
                .delete()
    }

    fun refreshNews() {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            clearOldNews()

            val reader = JsonReader(InputStreamReader(
                    URL(NEWS_SOURCE_URL).openConnection().getInputStream()
            ))

            reader.beginArray()
            while (reader.hasNext()) {
                val newsItem = NewsItem()
                var desc = ""
                var extUrl:String? = null

                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "id" -> newsItem.id = reader.nextString().toInt()
                        "date_p" -> newsItem.date = CommonUtils.getDateFromTimestamp(
                                reader.nextString().toInt()
                        )
                        "title" -> newsItem.title = reader.nextString()
                        "img" -> run {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "img_s" -> newsItem.smallThumbUrl = reader.nextString()
                                    "img_m" -> newsItem.mediumThumbUrl = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                        }
                        "desc" -> desc = reader.nextString()
                        "ext_url" -> extUrl = reader.nextString()
                        "url" -> newsItem.url = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()

                newsItem.body = extUrl?.let { "$desc $extUrl" } ?: desc
                newsItem.isNew = true

                database.news.createOrUpdate(newsItem)
            }
            reader.endArray()
            reader.close()

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllEvents(ascending: Boolean): List<Event> {
        return database.events.queryBuilder().orderBy(Event.FIELD_START, ascending).query()
    }

    fun refreshEvents() {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            database.events.deleteBuilder().delete()

            val reader = JsonReader(InputStreamReader(
                    URL(EVENTS_SOURCE_URL).openConnection().getInputStream()
            ))

            reader.beginArray()
            while (reader.hasNext()) {
                val event = Event()

                event.isNotify = false

                var venueId = 0
                var desc = ""
                var extUrl:String? = null
                var venuesUpdated:Long? = null

                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "id" -> event.id = reader.nextString().toInt()
                        "title" -> event.title = reader.nextString()
                        "v_id" -> venueId = reader.nextString().toInt()
                        "desc" -> desc = reader.nextString()
                        "ext_url" -> extUrl = reader.nextString()
                        "url" -> event.url = reader.nextString()
                        "venues_up" -> venuesUpdated = reader.nextLong()
                        "img" -> run {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "img_m" -> event.mediumThumbUrl = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                        }
                        "notify" -> run {
                            reader.skipValue()
                            event.isNotify = true
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()

                event.body = extUrl?.let { "$desc $extUrl" } ?: desc

                venuesUpdated?.let {
                    if (intPrefs.lastUpdatedPlaces().get() != it) {
                        refreshPlacesSync()
                        intPrefs.lastUpdatedPlaces().put(it)
                    }
                }

                event.place = database.places.queryForId(venueId)

                database.events.create(event)
            }
            reader.endArray()
            reader.close()

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getRelatedEvent(item: NewsItem): Event? {
        return database.events.queryForId(item.id)
    }

    private fun refreshPlacesSync() {
        database.places.deleteBuilder().delete()

        val reader = JsonReader(InputStreamReader(
                URL(PLACES_SOURCE_URL).openConnection().getInputStream()
        ))

        reader.beginArray()
        while (reader.hasNext()) {
            val place = Place()

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> place.id = reader.nextString().toInt()
                    "title" -> place.name = reader.nextString()
                    "address" -> place.address = reader.nextString()
                    "site" -> place.url = reader.nextString()
                    "phone" -> place.phone = reader.nextString()
                    "vk" -> place.vkUrl = reader.nextString()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            database.places.create(place)
        }
        reader.endArray()
        reader.close()
    }
}