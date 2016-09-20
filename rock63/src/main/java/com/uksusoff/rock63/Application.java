package com.uksusoff.rock63;

import com.evernote.android.job.JobManager;
import com.uksusoff.rock63.jobs.RockJobCreator;

/**
 * Created by User on 17.09.2016.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        JobManager.create(this).addJobCreator(new RockJobCreator());
    }

}
