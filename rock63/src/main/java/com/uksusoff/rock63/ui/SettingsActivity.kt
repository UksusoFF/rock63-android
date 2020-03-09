package com.uksusoff.rock63.ui

import android.annotation.SuppressLint
import android.text.method.LinkMovementMethod
import android.widget.Switch
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.UserPrefs_
import org.androidannotations.annotations.CheckedChange
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.ViewById
import org.androidannotations.annotations.sharedpreferences.Pref

@SuppressLint("Registered")
@EActivity(R.layout.a_settings)
open class SettingsActivity : BaseMenuActivity() {

    @ViewById(R.id.info_body_text)
    protected lateinit var bodyText: TextView
    @ViewById(R.id.daily_reminder)
    protected lateinit var dailyReminder: Switch
    @ViewById(R.id.weekly_reminder)
    protected lateinit var weeklyReminder: Switch
    @Pref
    protected lateinit var userPrefs: UserPrefs_

    override fun init() {
        super.init()

        bodyText.movementMethod = LinkMovementMethod.getInstance()
        bodyText.text = HtmlCompat.fromHtml(
                getString(R.string.about_body),
                HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        dailyReminder.isChecked = userPrefs.remindDayBefore().get()
        weeklyReminder.isChecked = userPrefs.remindWeekBefore().get()
    }

    @CheckedChange(R.id.daily_reminder)
    protected fun onDailyReminderCheckedChanged(isChecked: Boolean) {
        userPrefs.edit().remindDayBefore().put(isChecked).apply()
    }

    @CheckedChange(R.id.weekly_reminder)
    protected fun onWeeklyReminderCheckedChanged(isChecked: Boolean) {
        userPrefs.edit().remindWeekBefore().put(isChecked).apply()
    }
}