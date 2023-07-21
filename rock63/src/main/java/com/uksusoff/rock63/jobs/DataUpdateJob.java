package com.uksusoff.rock63.jobs;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.DataSource_;
import com.uksusoff.rock63.ui.EventDetailActivity_;
import com.uksusoff.rock63.ui.NewsListActivity;
import com.uksusoff.rock63.ui.NewsListActivity_;

/**
 * Created by User on 28.08.2016.
 */
public class DataUpdateJob extends Job {

    public static final String TAG = "data_update_job";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {

        DataSource dataSource = DataSource_.getInstance_(getContext());;

        try {
            dataSource.refreshEvents();
            dataSource.refreshNews();
        } catch (DataSource.NoInternetException e) {
            //Just not this time
        }

        return Result.SUCCESS;
    }

}
