package com.uksusoff.rock63.ui

import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.Switch
import android.widget.TextView
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.UserPrefs_
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.ViewById
import org.androidannotations.annotations.sharedpreferences.Pref

@EActivity(R.layout.a_settings)
open class SettingsActivity : BaseMenuActivity() {

    @ViewById(R.id.info_body_text)
    protected lateinit var bodyText: TextView
    @ViewById(R.id.dailyReminder)
    protected lateinit var dailyReminder: Switch
    @ViewById(R.id.weeklyReminder)
    protected lateinit var weeklyReminder: Switch
    @Pref
    protected lateinit var userPrefs: UserPrefs_

    override fun init() {
        super.init()
        bodyText.movementMethod = LinkMovementMethod.getInstance()
        bodyText.text = Html.fromHtml(getString(R.string.about_body))
        dailyReminder.isChecked = userPrefs.remindDayBefore().get()
        weeklyReminder.isChecked = userPrefs.remindWeekBefore().get()
        dailyReminder.setOnCheckedChangeListener { buttonView, isChecked -> userPrefs.edit().remindDayBefore().put(isChecked).apply() }
        weeklyReminder.setOnCheckedChangeListener { buttonView, isChecked -> userPrefs.edit().remindWeekBefore().put(isChecked).apply() }
    }
}