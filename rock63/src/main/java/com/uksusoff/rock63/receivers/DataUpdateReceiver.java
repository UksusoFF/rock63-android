package com.uksusoff.rock63.receivers;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.uksusoff.rock63.jobs.DataUpdateJob;

import org.androidannotations.annotations.EReceiver;

/**
 * Created by User on 18.06.2016.
 */
@EReceiver
public class DataUpdateReceiver extends BaseScheduledReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    @Override
    protected void createAlarmIfNeeded() {
        if (JobManager.create(context).getAllJobRequestsForTag(DataUpdateJob.TAG).size() == 0) {
            new JobRequest.Builder(DataUpdateJob.TAG)
                    .setPeriodic(AlarmManager.INTERVAL_DAY)
                    .setPersisted(true)
                    .build()
                    .schedule();
        }
    }
}
