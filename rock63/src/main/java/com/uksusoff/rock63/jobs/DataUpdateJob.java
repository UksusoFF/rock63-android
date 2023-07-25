package com.uksusoff.rock63.jobs;

import androidx.annotation.NonNull;

import com.evernote.android.job.Job;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.exceptions.NoContentException;
import com.uksusoff.rock63.exceptions.NoInternetException;

import org.androidannotations.annotations.Bean;

public class DataUpdateJob extends Job {

    public static final String TAG = "data_update_job";

    @Bean
    DataSource source;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            source.venuesRefresh();
            source.eventsRefresh();
            source.newsRefresh();
        } catch (NoInternetException | NoContentException e) {
            //Just not this time
        }

        return Result.SUCCESS;
    }

}
