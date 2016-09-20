package com.uksusoff.rock63.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.jobs.RockJobCreator;
import com.uksusoff.rock63.ui.BaseActivity;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;

import java.util.Calendar;

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

    protected abstract String getJobTag();
    protected abstract long repeatInterval();

    private void createAlarmIfNeeded() {
        if (JobManager.create(context).getAllJobRequestsForTag(getJobTag()).size() == 0) {
            new JobRequest.Builder(getJobTag())
                    .setPeriodic(repeatInterval())
                    .setPersisted(true)
                    .build()
                    .schedule();
        }
    }

}
