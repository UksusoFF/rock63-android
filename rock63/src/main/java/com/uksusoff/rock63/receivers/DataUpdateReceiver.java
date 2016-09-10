package com.uksusoff.rock63.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.jobs.DataUpdateJob;

import org.androidannotations.annotations.EReceiver;

import java.util.Calendar;

/**
 * Created by User on 18.06.2016.
 */
@EReceiver
public class DataUpdateReceiver extends BaseScheduledReceiver {

    @Override
    protected String getJobTag() {
        return DataUpdateJob.TAG;
    }

    @Override
    protected long repeatInterval() {
        return AlarmManager.INTERVAL_DAY;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
