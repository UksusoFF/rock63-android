package com.uksusoff.rock63.receivers;

import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.uksusoff.rock63.jobs.NotificationJob;

import org.androidannotations.annotations.EReceiver;

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
