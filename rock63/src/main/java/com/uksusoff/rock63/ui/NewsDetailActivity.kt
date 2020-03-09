package com.uksusoff.rock63.ui

import android.annotation.SuppressLint
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.text.HtmlCompat
import com.koushikdutta.ion.Ion
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.entities.NewsItem
import org.androidannotations.annotations.*

@SuppressLint("Registered")
@EActivity(R.layout.a_news_detail)
@OptionsMenu(R.menu.menu_detail)
open class NewsDetailActivity : BaseMenuActivity() {

    @Extra(EXTRA_ITEM_ID)
    @JvmField
    protected final var newsItemId:Int = NO_NEWS_ITEM_ID

    @ViewById(R.id.news_detail_title)
    protected lateinit var title: TextView
    @ViewById(R.id.news_detail_body)
    protected lateinit var body: TextView
    @ViewById(R.id.news_detail_image)
    protected lateinit var image: ImageView

    private lateinit var newsItem: NewsItem

    override fun init() {
        super.init()

        if (newsItemId == NO_NEWS_ITEM_ID) {
            return
        }

        newsItem = database.news.queryForId(newsItemId)

        title.text = newsItem.title
        body.movementMethod = LinkMovementMethod.getInstance()
        body.text = HtmlCompat.fromHtml(newsItem.body, HtmlCompat.FROM_HTML_MODE_LEGACY)

        Ion.with(this)
                .load(newsItem.mediumThumbUrl)
                .withBitmap()
                .placeholder(R.drawable.news_medium_placeholder)
                .intoImageView(image)
    }

    @OptionsItem(R.id.menu_share)
    fun menuShare() {
        shareNews()
    }

    private fun shareNews() {
        var body = HtmlCompat.fromHtml(newsItem.body, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        if (newsItem.url != null) {
            body += "\n\n" + newsItem.url
        }
        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(newsItem.title)
                .setText(body)
                .intent
        )
    }

    companion object {
        const val EXTRA_ITEM_ID = "newsItem"
        private const val NO_NEWS_ITEM_ID = -1
    }
}