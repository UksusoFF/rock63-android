package com.uksusoff.rock63.jobs;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by User on 28.08.2016.
 */
public class RockJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case NotificationJob.TAG:
                return new NotificationJob();
            case DataUpdateJob.TAG:
                return new DataUpdateJob();
            default:
                return null;
        }
    }

}
