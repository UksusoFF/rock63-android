package com.uksusoff.rock63;

import com.evernote.android.job.JobManager;
import com.uksusoff.rock63.jobs.RockJobCreator;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        JobManager.create(this).addJobCreator(new RockJobCreator());
    }

}
