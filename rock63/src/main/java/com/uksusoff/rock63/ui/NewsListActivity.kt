package com.uksusoff.rock63.ui

import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import com.koushikdutta.ion.Ion
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.DataSource
import com.uksusoff.rock63.data.DataSource.NoInternetException
import com.uksusoff.rock63.data.entities.NewsItem
import com.uksusoff.rock63.utils.CommonUtils
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.ItemClick
import java.sql.SQLException
import java.util.*

/**
 * Created by User on 13.05.2016.
 */
@EActivity(R.layout.a_list)
open class NewsListActivity : ItemListActivity() {

    @Bean
    protected lateinit var source: DataSource

    override var isRefreshing: Boolean
        protected get() = Companion.isRefreshing
        set(refreshing) {
            Companion.isRefreshing = refreshing
        }

    override var activeActivity: ItemListActivity?
        protected get() = Companion.activeActivity
        set(activity) {
            Companion.activeActivity = activity as NewsListActivity?
        }

    override val emptyListTextResId: Int
        protected get() = R.string.news_no_item_text

    override fun createAdapterFromStorageItems(): ListAdapter? {
        val news: List<NewsItem>
        news = try {
            source!!.allNews
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
        val data: MutableList<Map<String, Any?>> = ArrayList()
        for (i in news.indices) {
            val item = news[i]
            val datum: MutableMap<String, Any?> = HashMap(3)
            datum["title"] = item.title
            datum["text"] = CommonUtils.getCroppedString(CommonUtils.getTextFromHtml(item.body!!), 50, true)
            datum["imageUrl"] = item.smallThumbUrl
            datum["obj"] = item
            data.add(datum)
        }
        val adapter = SimpleAdapter(this, data,
                R.layout.i_news_item, arrayOf("title", "text", "imageUrl"), intArrayOf(R.id.newsTitle,
                R.id.newsDescription,
                R.id.newsImageView))
        adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, textRepresentation ->
            if (view is ImageView && data is String) {
                Ion.with(this)
                        .load(data)
                        .withBitmap()
                        .placeholder(R.drawable.news_no_image)
                        .intoImageView(view)

                return@ViewBinder true
            }
            false
        }
        return adapter
    }

    @Throws(NoInternetException::class)
    override fun refreshItemStorage() {
        source!!.refreshNews()
    }

    @ItemClick(R.id.list)
    fun newsItemClicked(item: Map<String?, Any?>) {
        val newsItem = item["obj"] as NewsItem?
        val related = source!!.getRelatedEvent(newsItem!!)
        if (related == null) {
            NewsDetailActivity_.intent(this).newsItemId(newsItem.id).start()
        } else {
            EventDetailActivity_.intent(this).eventId(related.id).start()
        }
    }

    companion object {
        private var isRefreshing = false
        private var activeActivity: NewsListActivity? = null
    }
}