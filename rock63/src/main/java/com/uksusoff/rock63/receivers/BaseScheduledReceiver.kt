package com.uksusoff.rock63.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.uksusoff.rock63.ui.BaseActivity
import org.androidannotations.annotations.EReceiver
import org.androidannotations.annotations.ReceiverAction
import org.androidannotations.annotations.SystemService

/**
 * Created by User on 18.06.2016.
 */
@EReceiver
abstract class BaseScheduledReceiver : BroadcastReceiver() {

    @SystemService
    protected lateinit var alarmManager: AlarmManager

    protected lateinit var context: Context

    @ReceiverAction(actions = [Intent.ACTION_MY_PACKAGE_REPLACED])
    fun onUpdate(context: Context) {
        this.context = context
        createAlarmIfNeeded()
    }

    @ReceiverAction(actions = [Intent.ACTION_BOOT_COMPLETED])
    fun onBoot(context: Context) {
        this.context = context
        createAlarmIfNeeded()
    }

    @ReceiverAction(actions = [BaseActivity.ACTION_CHECK_ALARM])
    fun firstLaunch(context: Context) {
        this.context = context
        createAlarmIfNeeded()
    }

    protected abstract fun createAlarmIfNeeded()
}