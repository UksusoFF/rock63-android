package com.uksusoff.rock63.jobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.DataSource_;

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
