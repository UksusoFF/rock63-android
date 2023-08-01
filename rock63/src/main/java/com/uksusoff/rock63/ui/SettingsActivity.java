package com.uksusoff.rock63.ui;

import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.UserPrefs_;
import com.uksusoff.rock63.utils.StringUtils;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.a_settings)
public class SettingsActivity extends AbstractMenuActivity {

    @ViewById(R.id.info_body_text)
    TextView bodyText;

    @ViewById(R.id.dailyReminder)
    SwitchCompat dailyReminder;

    @ViewById(R.id.weeklyReminder)
    SwitchCompat weeklyReminder;

    @Pref
    UserPrefs_ userPrefs;

    @Override
    protected void init() {
        super.init();

        bodyText.setMovementMethod(LinkMovementMethod.getInstance());
        bodyText.setText(StringUtils.fromHtml(getString(R.string.about_body)));

        dailyReminder.setChecked(userPrefs.remindDayBefore().get());
        weeklyReminder.setChecked(userPrefs.remindWeekBefore().get());

        dailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> userPrefs.edit().remindDayBefore().put(isChecked).apply());
        weeklyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> userPrefs.edit().remindWeekBefore().put(isChecked).apply());
    }

}
