package com.uksusoff.rock63.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.core.app.ShareCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.koushikdutta.ion.Ion
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.entities.Event
import org.androidannotations.annotations.*
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("Registered")
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
    @ViewById(R.id.event_detail_placename)
    protected lateinit var placeName: TextView
    @ViewById(R.id.event_detail_placeaddr)
    protected lateinit var placeAddress: TextView
    @ViewById(R.id.event_detail_placeinf)
    protected lateinit var placeInfoContainer: View
    @ViewById(R.id.event_detail_infdetailbtn)
    protected lateinit var placeInfoButton: Button

    @ViewById(R.id.event_detail_title)
    protected lateinit var titleView:TextView
    @ViewById(R.id.event_detail_datentime)
    protected lateinit var dateTimeView:TextView
    @ViewById(R.id.event_detail_description)
    protected lateinit var description:TextView
    @ViewById(R.id.event_detail_image)
    protected lateinit var imageView:ImageView

    private lateinit var event: Event

    override fun init() {
        super.init()
        event = database.events.queryForId(eventId)
        titleView.text = event.title
        val fDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val fTime = SimpleDateFormat("HH:mm", Locale.getDefault())

        event.start?.let { start ->
            event.end?.let { end ->
                dateTimeView.text = String.format(
                        "%s %s - %s %s",
                        fDate.format(start),
                        fTime.format(start),
                        fDate.format(end),
                        fTime.format(end)
                )
            } ?: run {
                dateTimeView.text = String.format(
                        "%s %s",
                        fDate.format(start),
                        fTime.format(start)
                )
            }
        }

        event.place?.let { place ->
            placeName.text = place.name
            placeAddress.text = place.address
            place.phone?.takeIf { it != "" }?.let {phone ->
                placePhone.visibility = View.VISIBLE
                placePhone.movementMethod = LinkMovementMethod.getInstance()
                placePhone.text = HtmlCompat.fromHtml(phone, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } ?: run {
                placePhone.visibility = View.GONE
            }

            place.url?.takeIf { it != "" }?.let { url ->
                placeLink.visibility = View.VISIBLE
                placeLink.movementMethod = LinkMovementMethod.getInstance()
                placeLink.text = HtmlCompat.fromHtml(url, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } ?: run {
                placeLink.visibility = View.GONE
            }

            place.vkUrl?.takeIf { it != "" }?.let { vkUrl ->
                placeVkLink.visibility = View.VISIBLE
                placeVkLink.movementMethod = LinkMovementMethod.getInstance()
                placeVkLink.text = HtmlCompat.fromHtml(vkUrl, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } ?: run {
                placeVkLink.visibility = View.GONE
            }

            placeInfoButton.setText(R.string.event_show_info_button_title)
        } ?: run {
            placeName.visibility = View.GONE
            placeInfoButton.visibility = View.GONE
        }

        description.movementMethod = LinkMovementMethod.getInstance()
        description.text = HtmlCompat.fromHtml(event.body, HtmlCompat.FROM_HTML_MODE_LEGACY)

        event.mediumThumbUrl?.let { url ->
            Ion.with(this)
                    .load(url)
                    .withBitmap()
                    .placeholder(R.drawable.news_medium_placeholder)
                    .intoImageView(imageView)
        } ?: run {
            imageView.visibility = View.GONE
        }
    }

    @Click(R.id.event_detail_infdetailbtn)
    open fun onPlaceInfoButtonClick() {
        if (placeInfoContainer.visibility == View.VISIBLE) {
            placeInfoContainer.visibility = View.GONE
            placeInfoButton.setText(R.string.event_show_info_button_title)
        } else {
            placeInfoContainer.visibility = View.VISIBLE
            placeInfoButton.setText(R.string.event_hide_info_button_title)
        }
    }

    @OptionsItem(R.id.menu_share)
    fun menuShare() {
        shareEvent()
    }

    private fun shareEvent() {
        val subject: String?
        subject = event.place?.let { place ->
            String.format("%s @ %s", event.title, place.name)
        } ?: event.title

        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(subject)
                .setText(event.url)
                .intent
        )
    }

    companion object {
        const val EXTRA_ITEM_ID = "eventItem"
    }
}