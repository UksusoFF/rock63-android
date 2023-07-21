package com.uksusoff.rock63.jobs

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

/**
 * Created by User on 28.08.2016.
 */
class RockJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            NotificationJob.TAG -> NotificationJob()
            DataUpdateJob.TAG -> DataUpdateJob()
            else -> null
        }
    }
}