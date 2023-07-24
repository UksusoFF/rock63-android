package com.uksusoff.rock63.receivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uksusoff.rock63.ui.BaseActivity;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;
import org.androidannotations.annotations.SystemService;

/**
 * Created by User on 18.06.2016.
 */
@EReceiver
public abstract class BaseScheduledReceiver extends BroadcastReceiver {

    @SystemService
    AlarmManager alarmManager;

    protected Context context;

    @ReceiverAction(actions = Intent.ACTION_MY_PACKAGE_REPLACED)
    public void onUpdate(Context context) {
        this.context = context;
        createAlarmIfNeeded();
    }

    @ReceiverAction(actions = Intent.ACTION_BOOT_COMPLETED)
    public void onBoot(Context context) {
        this.context = context;
        createAlarmIfNeeded();
    }

    @ReceiverAction(actions = BaseActivity.ACTION_CHECK_ALARM)
    public void firstLaunch(Context context) {
        this.context = context;
        createAlarmIfNeeded();
    }

    protected abstract void createAlarmIfNeeded();

}
