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

    private long getDelayToLaunchTime() {
        long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 18);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        while (c.getTimeInMillis() < now) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return c.getTimeInMillis() - now;
    }

    @Override
    protected void createAlarmIfNeeded() {
        if (JobManager.create(context).getAllJobRequestsForTag(NotificationJob.TAG).size() == 0) {
            new JobRequest.Builder(NotificationJob.TAG)
                    .setExact(getDelayToLaunchTime())
                    .setPersisted(true)
                    .build()
                    .schedule();
        }
    }

}
