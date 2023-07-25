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

        ((TextView) findViewById(R.id.event_detail_title)).setText(event.title);
        ((TextView) findViewById(R.id.event_detail_date_and_time)).setText(event.getDateRange());

        if (event.getVenueItem() != null) {
            ((TextView) findViewById(R.id.event_detail_venue_name)).setText(event.getVenueItem().title);
            ((TextView) findViewById(R.id.event_detail_venue_address)).setText(event.getVenueItem().address);

            if (event.getVenueItem().phone != null) {
                venuePhone.setVisibility(View.VISIBLE);
                venuePhone.setMovementMethod(LinkMovementMethod.getInstance());
                venuePhone.setText(StringUtils.fromHtml(event.getVenueItem().phone));
            } else {
                venuePhone.setVisibility(View.GONE);
            }

            if (event.getVenueItem().url != null) {
                venueLink.setVisibility(View.VISIBLE);
                venueLink.setMovementMethod(LinkMovementMethod.getInstance());
                venueLink.setText(StringUtils.fromHtml(event.getVenueItem().url));
            } else {
                venueLink.setVisibility(View.GONE);
            }

            if (event.getVenueItem().vk != null) {
                venueVk.setVisibility(View.VISIBLE);
                venueVk.setMovementMethod(LinkMovementMethod.getInstance());
                venueVk.setText(StringUtils.fromHtml(event.getVenueItem().vk));
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
        ((TextView) findViewById(R.id.event_detail_description)).setText(StringUtils.fromHtml(event.getDescriptionText()));

        if (event.thumbnailMiddle != null) {
            UrlImageViewHelper.setUrlDrawable(
                    (ImageView) findViewById(R.id.event_detail_image),
                    event.thumbnailMiddle,
                    R.drawable.news_medium_placeholder
            );
        } else {
            ((ImageView) findViewById(R.id.event_detail_image)).setVisibility(View.GONE);
        }
    }

    @OptionsItem(R.id.menu_share)
    void menuShare() {
        startActivity(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setSubject(event.getShareText())
                .setText(event.url)
                .getIntent()
        );
    }
}
