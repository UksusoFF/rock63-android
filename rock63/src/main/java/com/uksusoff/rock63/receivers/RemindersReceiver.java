package com.uksusoff.rock63.receivers;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.uksusoff.rock63.jobs.NotificationJob;

import org.androidannotations.annotations.EReceiver;

import java.util.Calendar;

/**
 * Created by User on 18.06.2016.
 */
@EReceiver
public class RemindersReceiver extends BaseScheduledReceiver {

    public static long getRepeatInterval() {
        return AlarmManager.INTERVAL_DAY;
    }

    @Override
    protected String getJobTag() {
        return NotificationJob.TAG;
    }

    @Override
    protected long repeatInterval() {
        return getRepeatInterval();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }

}
