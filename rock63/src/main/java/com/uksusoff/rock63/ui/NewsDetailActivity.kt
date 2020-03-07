package com.uksusoff.rock63.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.core.app.ShareCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.koushikdutta.ion.Ion
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.entities.NewsItem
import org.androidannotations.annotations.*
import java.sql.SQLException

@SuppressLint("Registered")
@EActivity(R.layout.a_news_detail)
@OptionsMenu(R.menu.menu_detail)
open class NewsDetailActivity : BaseMenuActivity() {

    @Extra(EXTRA_ITEM_ID)
    @JvmField
    protected final var newsItemId:Int = 0

    @ViewById(R.id.news_detail_title)
    protected lateinit var title: TextView
    @ViewById(R.id.news_detail_body)
    protected lateinit var body: TextView
    @ViewById(R.id.news_detail_image)
    protected lateinit var image: ImageView

    private lateinit var newsItem: NewsItem

    override fun init() {
        super.init()
        val extras = intent.extras ?: return
        database.news.queryForId(newsItemId)
        title.text = newsItem.title
        body.movementMethod = LinkMovementMethod.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            body.text = Html.fromHtml(newsItem.body, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            body.text = Html.fromHtml(newsItem.body)
        }

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
        var body = Html.fromHtml(newsItem.body).toString()
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
    }
}