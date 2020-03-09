package com.uksusoff.rock63.ui

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import com.koushikdutta.ion.Ion
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.DataProviderComponent
import com.uksusoff.rock63.data.entities.NewsItem
import com.uksusoff.rock63.utils.CommonUtils
import org.androidannotations.annotations.Background
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.ItemClick
import java.util.*

/**
 * Created by Vyacheslav Vodyanov on 13.05.2016.
 */
@SuppressLint("Registered")
@EActivity(R.layout.a_list)
open class NewsListActivity : ItemListActivity() {

    @Bean
    protected lateinit var providerComponent: DataProviderComponent

    override val emptyListTextResId: Int
        get() = R.string.news_no_item_text

    @Background
    override fun createAdapterFromStorageItems(handler: (ListAdapter) -> Unit) {
        val adapter = SimpleAdapter(this,
                providerComponent.allNews.map { item ->
                    val datum: MutableMap<String, Any?> = HashMap(4)
                    datum["title"] = item.title
                    datum["text"] = CommonUtils.getCroppedString(
                            CommonUtils.getTextFromHtml(item.body),
                            50,
                            true
                    )
                    datum["imageUrl"] = item.smallThumbUrl
                    datum["obj"] = item
                    datum
                },
                R.layout.i_news_item,
                arrayOf("title", "text", "imageUrl"),
                intArrayOf(R.id.newsTitle, R.id.newsDescription, R.id.newsImageView)
        )

        adapter.viewBinder = SimpleAdapter.ViewBinder { view, vData, _ ->
            val imView: ImageView = view as? ImageView ?: return@ViewBinder false
            val url: String = vData as? String ?: return@ViewBinder false

            Ion.with(this)
                    .load(url)
                    .withBitmap()
                    .placeholder(R.drawable.news_no_image)
                    .intoImageView(imView)

            true
        }

        handler(adapter)
    }

    override fun refreshItemStorage() {
        providerComponent.refreshNews()
    }

    @ItemClick(R.id.list)
    fun newsItemClicked(item: Map<String?, Any?>) {
        val newsItem = item["obj"] as NewsItem
        providerComponent.getRelatedEvent(newsItem)?.let {
            EventDetailActivity_.intent(this).eventId(it.id).start()
        } ?: run {
            NewsDetailActivity_.intent(this).newsItemId(newsItem.id).start()
        }
    }
}