package com.uksusoff.rock63.ui

import androidx.core.app.ShareCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.koushikdutta.ion.Ion
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.entities.NewsItem
import org.androidannotations.annotations.*
import java.sql.SQLException

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

    private var newsItem: NewsItem? = null

    override fun init() {
        super.init()
        val extras = intent.extras ?: return
        newsItem = try {
            helper!!.newsItemDao!!.queryForId(newsItemId)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
        title!!.text = newsItem!!.title
        body!!.movementMethod = LinkMovementMethod.getInstance()
        body!!.text = Html.fromHtml(newsItem!!.body)

        Ion.with(this)
                .load(newsItem!!.mediumThumbUrl)
                .withBitmap()
                .placeholder(R.drawable.news_medium_placeholder)
                .intoImageView(image)
    }

    @OptionsItem(R.id.menu_share)
    fun menuShare() {
        shareNews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun shareNews() {
        var body = Html.fromHtml(newsItem!!.body).toString()
        if (newsItem!!.url != null) {
            body += "\n\n" + newsItem!!.url
        }
        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(newsItem!!.title)
                .setText(body)
                .intent
        )
    }

    companion object {
        const val EXTRA_ITEM_ID = "newsItem"
    }
}