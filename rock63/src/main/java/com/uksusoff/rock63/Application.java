package com.uksusoff.rock63;

import com.bugsnag.android.Bugsnag;
import com.evernote.android.job.JobManager;
import com.uksusoff.rock63.jobs.JobCreator;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Bugsnag.start(this);

        JobManager.create(this).addJobCreator(new JobCreator());
    }

}
