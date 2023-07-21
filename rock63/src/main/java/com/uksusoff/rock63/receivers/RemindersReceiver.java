package com.uksusoff.rock63.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.jobs.NotificationJob;
import com.uksusoff.rock63.ui.NewsListActivity_;
import com.uksusoff.rock63.utils.DateUtils;

import org.androidannotations.annotations.EReceiver;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by User on 18.06.2016.
 */
@EReceiver
public class RemindersReceiver extends BaseScheduledReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    @Override
    protected void createAlarmIfNeeded() {
        if (JobManager.create(context).getAllJobRequestsForTag(NotificationJob.TAG).size() == 0) {
            long[] executionWindow = NotificationJob.getNextExecutionWindow();

            new JobRequest.Builder(NotificationJob.TAG)
                    .setExecutionWindow(executionWindow[0], executionWindow[1])
                    .setPersisted(true)
                    .build()
                    .schedule();
        }
    }

}
