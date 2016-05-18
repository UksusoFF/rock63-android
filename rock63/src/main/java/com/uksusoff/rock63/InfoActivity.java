package com.uksusoff.rock63;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.RadioGroup;
import android.widget.TextView;

@EActivity(R.layout.a_settings)
public class InfoActivity extends BaseMenuActivity {

    @ViewById(R.id.info_body_text)
    TextView bodyText;

    @Override
    protected void init() {
        super.init();

        bodyText.setMovementMethod(LinkMovementMethod.getInstance());
        bodyText.setText(Html.fromHtml(getString(R.string.about_body)));
    }

}
