package com.uksusoff.rock63.jobs;

import com.evernote.android.job.Job;

public class JobCreator implements com.evernote.android.job.JobCreator {

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
