package com.uksusoff.rock63.data.entities

import com.j256.ormlite.field.DatabaseField
import java.util.*

class NewsItem {
    companion object {
        const val FIELD_DATE = "date"
    }

    @DatabaseField(id = true)
    var id = 0
    @DatabaseField
    var title: String? = null
    @DatabaseField
    var body: String? = null
    @DatabaseField
    var date: Date? = null
    @DatabaseField
    var smallThumbUrl: String? = null
    @DatabaseField
    var mediumThumbUrl: String? = null
    @DatabaseField
    var isNew = false
    @DatabaseField
    var url: String? = null

    val imageCacheName: String
        get() = "news_" + Integer.toString(id)

}