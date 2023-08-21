package com.uksusoff.rock63.jobs;

import androidx.annotation.NonNull;

import com.bugsnag.android.Bugsnag;
import com.evernote.android.job.Job;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.exceptions.NoContentException;
import com.uksusoff.rock63.exceptions.NoInternetException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean()
public class DataUpdateJob extends Job {

    public static final String TAG = "data_update_job";

    @Bean
    DataSource source;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            source.sourcesRefresh();
        } catch (NoInternetException | NoContentException e) {
            //Just not this time
            Bugsnag.notify(e);
        } catch (Exception e) {
            Bugsnag.notify(e);
        }

        return Result.SUCCESS;
    }

}
