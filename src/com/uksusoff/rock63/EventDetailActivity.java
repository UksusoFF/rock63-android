package com.uksusoff.rock63;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uksusoff.rock63.data.entities.Event;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

@EActivity(R.layout.activity_event_detail)
@OptionsMenu(R.menu.menu_event)
public class EventDetailActivity extends BaseActivity {

    public static final String EXTRA_ITEM_ID = "eventItem";

    @Extra(EXTRA_ITEM_ID)
    int eventId;

    @Pref
    ISharedPrefs_ sharedPrefs;
    
    @ViewById(R.id.event_detail_placephone)
    TextView placePhone;
    
    @ViewById(R.id.event_detail_placelink)
    TextView placeLink;
    
    @ViewById(R.id.event_detail_placevklink)
    TextView placeVkLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String theme = sharedPrefs.theme().get();

        if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_DARK)) {
            setTheme(R.style.AppDarkTheme);
        } else if (theme.equalsIgnoreCase(Settings.ROCK63_PREFS_THEME_OPT_LIGHT)) {
            setTheme(R.style.AppLightTheme);
        }

        super.onCreate(savedInstanceState);

    }

    private Event event;

    @AfterViews
    void init() {

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
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        if (event.getPlace() != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, String.format("%s @ %s", event.getTitle(), event.getPlace().getName()));
        } else {
            intent.putExtra(Intent.EXTRA_SUBJECT, event.getTitle());
        }
        intent.putExtra(Intent.EXTRA_TEXT, event.getUrl());

        startActivity(intent);
    }

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
     * menu; this adds items to the action bar if it is present.
     * getMenuInflater().inflate(R.menu.activity_event_detail, menu); return
     * true; }
     */

}
