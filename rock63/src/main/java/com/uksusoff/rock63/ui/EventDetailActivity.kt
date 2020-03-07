package com.uksusoff.rock63.ui

import androidx.core.app.ShareCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.koushikdutta.ion.Ion
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.entities.Event
import org.androidannotations.annotations.*
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

@EActivity(R.layout.a_event_detail)
@OptionsMenu(R.menu.menu_detail)
open class EventDetailActivity : BaseMenuActivity() {

    @Extra(EXTRA_ITEM_ID)
    @JvmField
    protected final var eventId:Int = 0

    @ViewById(R.id.event_detail_placephone)
    protected lateinit var placePhone: TextView
    @ViewById(R.id.event_detail_placelink)
    protected lateinit var placeLink: TextView
    @ViewById(R.id.event_detail_placevklink)
    protected lateinit var placeVkLink: TextView

    private var event: Event? = null

    override fun init() {
        super.init()
        event = try {
            helper!!.eventDao!!.queryForId(eventId)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
        (findViewById<View>(R.id.event_detail_title) as TextView).text = event!!.title
        val fDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val fTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        if (event!!.end != null) {
            (findViewById<View>(R.id.event_detail_datentime) as TextView).text = String.format("%s %s - %s %s", fDate.format(event!!.start), fTime.format(event!!.start), fDate.format(event!!.end),
                    fTime.format(event!!.end))
        } else {
            (findViewById<View>(R.id.event_detail_datentime) as TextView).text = String.format("%s %s", fDate.format(event!!.start), fTime.format(event!!.start))
        }
        if (event!!.place != null) {
            (findViewById<View>(R.id.event_detail_placename) as TextView).text = event!!.place!!.name
            (findViewById<View>(R.id.event_detail_placeaddr) as TextView).text = event!!.place!!.address
            if (event!!.place!!.phone != null && event!!.place!!.phone != "") {
                placePhone!!.visibility = View.VISIBLE
                placePhone!!.movementMethod = LinkMovementMethod.getInstance()
                placePhone!!.text = Html.fromHtml(event!!.place!!.phone)
            } else {
                placePhone!!.visibility = View.GONE
            }
            if (event!!.place!!.url != null && event!!.place!!.url != "") {
                placeLink!!.visibility = View.VISIBLE
                placeLink!!.movementMethod = LinkMovementMethod.getInstance()
                placeLink!!.text = Html.fromHtml(event!!.place!!.url)
            } else {
                placeLink!!.visibility = View.GONE
            }
            if (event!!.place!!.vkUrl != null && event!!.place!!.vkUrl != "") {
                placeVkLink!!.visibility = View.VISIBLE
                placeVkLink!!.movementMethod = LinkMovementMethod.getInstance()
                placeVkLink!!.text = Html.fromHtml(event!!.place!!.vkUrl)
            } else {
                placeVkLink!!.visibility = View.GONE
            }
            (findViewById<View>(R.id.event_detail_infdetailbtn) as Button).setOnClickListener {
                val inf = findViewById<View>(R.id.event_detail_placeinf)
                if (inf.visibility == View.VISIBLE) {
                    inf.visibility = View.GONE
                    (findViewById<View>(R.id.event_detail_infdetailbtn) as Button).setText(R.string.event_show_info_button_title)
                } else {
                    inf.visibility = View.VISIBLE
                    (findViewById<View>(R.id.event_detail_infdetailbtn) as Button).setText(R.string.event_hide_info_button_title)
                }
            }
            (findViewById<View>(R.id.event_detail_infdetailbtn) as Button).setText(R.string.event_show_info_button_title)
        } else {
            (findViewById<View>(R.id.event_detail_placename) as TextView).visibility = View.GONE
            (findViewById<View>(R.id.event_detail_infdetailbtn) as Button).visibility = View.GONE
        }
        (findViewById<View>(R.id.event_detail_description) as TextView).movementMethod = LinkMovementMethod.getInstance()
        (findViewById<View>(R.id.event_detail_description) as TextView).text = Html.fromHtml(event!!.body)
        if (event!!.mediumThumbUrl != null) {
            Ion.with(this)
                    .load(event!!.mediumThumbUrl)
                    .withBitmap()
                    .placeholder(R.drawable.news_medium_placeholder)
                    .intoImageView(findViewById<View>(R.id.event_detail_image) as ImageView)
        } else {
            (findViewById<View>(R.id.event_detail_image) as ImageView).visibility = View.GONE
        }
    }

    @OptionsItem(R.id.menu_share)
    fun menuShare() {
        shareEvent()
    }

    private fun shareEvent() {
        val subject: String?
        subject = if (event!!.place != null) {
            String.format("%s @ %s", event!!.title, event!!.place!!.name)
        } else {
            event!!.title
        }
        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(subject)
                .setText(event!!.url)
                .intent
        )
    }

    companion object {
        const val EXTRA_ITEM_ID = "eventItem"
    }
}