package com.uksusoff.rock63.ui;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ShareCompat;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.entities.EventItem;
import com.uksusoff.rock63.utils.StringUtils;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@EActivity(R.layout.a_event_detail)
@OptionsMenu(R.menu.menu_detail)
public class EventDetailActivity extends BaseMenuActivity {

    public static final String EXTRA_ITEM_ID = "eventItem";

    @Extra(EXTRA_ITEM_ID)
    int eventId;

    @ViewById(R.id.event_detail_venue_phone)
    TextView venuePhone;

    @ViewById(R.id.event_detail_venue_link)
    TextView venueLink;

    @ViewById(R.id.event_detail_venue_vk)
    TextView venueVk;

    private EventItem event;

    @Override
    protected void init() {
        super.init();

        try {
            event = getHelper().getEventItemsDao().queryForId(eventId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ((TextView) findViewById(R.id.event_detail_title)).setText(event.getTitle());
        SimpleDateFormat fDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat fTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (event.getEnd() != null) {
            ((TextView) findViewById(R.id.event_detail_date_and_time)).setText(String.format("%s %s - %s %s", fDate.format(event.getStart()), fTime.format(event.getStart()), fDate.format(event.getEnd()),
                    fTime.format(event.getEnd())));
        } else {
            ((TextView) findViewById(R.id.event_detail_date_and_time)).setText(String.format("%s %s", fDate.format(event.getStart()), fTime.format(event.getStart())));
        }

        if (event.getVenueItem() != null) {
            ((TextView) findViewById(R.id.event_detail_venue_name)).setText(event.getVenueItem().getName());
            ((TextView) findViewById(R.id.event_detail_venue_address)).setText(event.getVenueItem().getAddress());

            if (event.getVenueItem().getPhone() != null && !event.getVenueItem().getPhone().equals("")) {
                venuePhone.setVisibility(View.VISIBLE);
                venuePhone.setMovementMethod(LinkMovementMethod.getInstance());
                venuePhone.setText(StringUtils.fromHtml(event.getVenueItem().getPhone()));
            } else {
                venuePhone.setVisibility(View.GONE);
            }

            if (event.getVenueItem().getUrl() != null && !event.getVenueItem().getUrl().equals("")) {
                venueLink.setVisibility(View.VISIBLE);
                venueLink.setMovementMethod(LinkMovementMethod.getInstance());
                venueLink.setText(StringUtils.fromHtml(event.getVenueItem().getUrl()));
            } else {
                venueLink.setVisibility(View.GONE);
            }

            if (event.getVenueItem().getVkUrl() != null && !event.getVenueItem().getVkUrl().equals("")) {
                venueVk.setVisibility(View.VISIBLE);
                venueVk.setMovementMethod(LinkMovementMethod.getInstance());
                venueVk.setText(StringUtils.fromHtml(event.getVenueItem().getVkUrl()));
            } else {
                venueVk.setVisibility(View.GONE);
            }

            ((Button) findViewById(R.id.event_detail_infdetailbtn)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    View inf = EventDetailActivity.this.findViewById(R.id.event_detail_venue_description);
                    if (inf.getVisibility() == View.VISIBLE) {
                        inf.setVisibility(View.GONE);
                        ((Button) findViewById(R.id.event_detail_infdetailbtn)).setText(R.string.event_show_info_button_title);
                    } else {
                        inf.setVisibility(View.VISIBLE);
                        ((Button) findViewById(R.id.event_detail_infdetailbtn)).setText(R.string.event_hide_info_button_title);
                    }
                }

            });

            ((Button) findViewById(R.id.event_detail_infdetailbtn)).setText(R.string.event_show_info_button_title);

        } else {
            ((TextView) findViewById(R.id.event_detail_venue_name)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.event_detail_infdetailbtn)).setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.event_detail_description)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.event_detail_description)).setText(StringUtils.fromHtml(event.getBody()));

        if (event.getMediumThumbUrl() != null) {
            UrlImageViewHelper.setUrlDrawable((ImageView) findViewById(R.id.event_detail_image), event.getMediumThumbUrl(), R.drawable.news_medium_placeholder);
        } else {
            ((ImageView) findViewById(R.id.event_detail_image)).setVisibility(View.GONE);
        }
    }

    @OptionsItem(R.id.menu_share)
    void menuShare() {
        shareEvent();
    }

    private void shareEvent() {
        String subject;
        if (event.getVenueItem() != null) {
            subject = String.format("%s @ %s", event.getTitle(), event.getVenueItem().getName());
        } else {
            subject = event.getTitle();
        }

        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(subject)
                .setText(event.getUrl())
                .getIntent()
        );
    }

}
