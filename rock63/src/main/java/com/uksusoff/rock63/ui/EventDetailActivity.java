package com.uksusoff.rock63.ui;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ShareCompat;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.entities.Event;

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

    @ViewById(R.id.event_detail_placephone)
    TextView placePhone;

    @ViewById(R.id.event_detail_placelink)
    TextView placeLink;

    @ViewById(R.id.event_detail_placevklink)
    TextView placeVkLink;

    private Event event;

    @Override
    protected void init() {
        super.init();

        try {
            event = getHelper().getEventDao().queryForId(eventId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ((TextView) findViewById(R.id.event_detail_title)).setText(event.getTitle());
        SimpleDateFormat fDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat fTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (event.getEnd() != null) {
            ((TextView) findViewById(R.id.event_detail_datentime)).setText(String.format("%s %s - %s %s", fDate.format(event.getStart()), fTime.format(event.getStart()), fDate.format(event.getEnd()),
                    fTime.format(event.getEnd())));
        } else {
            ((TextView) findViewById(R.id.event_detail_datentime)).setText(String.format("%s %s", fDate.format(event.getStart()), fTime.format(event.getStart())));
        }

        if (event.getPlace() != null) {
            ((TextView) findViewById(R.id.event_detail_placename)).setText(event.getPlace().getName());
            ((TextView) findViewById(R.id.event_detail_placeaddr)).setText(event.getPlace().getAddress());

            if (event.getPlace().getPhone() != null && !event.getPlace().getPhone().equals("")) {
                placePhone.setVisibility(View.VISIBLE);
                placePhone.setMovementMethod(LinkMovementMethod.getInstance());
                placePhone.setText(Html.fromHtml(event.getPlace().getPhone()));
            } else {
                placePhone.setVisibility(View.GONE);
            }

            if (event.getPlace().getUrl() != null && !event.getPlace().getUrl().equals("")) {
                placeLink.setVisibility(View.VISIBLE);
                placeLink.setMovementMethod(LinkMovementMethod.getInstance());
                placeLink.setText(Html.fromHtml(event.getPlace().getUrl()));
            } else {
                placeLink.setVisibility(View.GONE);
            }

            if (event.getPlace().getVkUrl() != null && !event.getPlace().getVkUrl().equals("")) {
                placeVkLink.setVisibility(View.VISIBLE);
                placeVkLink.setMovementMethod(LinkMovementMethod.getInstance());
                placeVkLink.setText(Html.fromHtml(event.getPlace().getVkUrl()));
            } else {
                placeVkLink.setVisibility(View.GONE);
            }

            ((Button) findViewById(R.id.event_detail_infdetailbtn)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    View inf = EventDetailActivity.this.findViewById(R.id.event_detail_placeinf);
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
            ((TextView) findViewById(R.id.event_detail_placename)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.event_detail_infdetailbtn)).setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.event_detail_description)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.event_detail_description)).setText(Html.fromHtml(event.getBody()));

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
        if (event.getPlace() != null) {
            subject = String.format("%s @ %s", event.getTitle(), event.getPlace().getName());
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
