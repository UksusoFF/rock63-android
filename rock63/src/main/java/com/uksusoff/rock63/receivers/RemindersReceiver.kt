package com.uksusoff.rock63.receivers

import android.content.Context
import android.content.Intent
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.uksusoff.rock63.jobs.NotificationJob
import com.uksusoff.rock63.jobs.NotificationJob.Companion.nextExecutionWindow
import com.uksusoff.rock63.ui.NewsListActivity_
import org.androidannotations.annotations.EReceiver

/**
 * Created by User on 18.06.2016.
 */
@EReceiver
open class RemindersReceiver : BaseScheduledReceiver() {
    override fun onReceive(context: Context, intent: Intent) {}
    override fun createAlarmIfNeeded() {
        if (JobManager.create(context!!).getAllJobRequestsForTag(NotificationJob.TAG).size == 0) {
            val executionWindow = nextExecutionWindow
            JobRequest.Builder(NotificationJob.TAG)
                    .setExecutionWindow(executionWindow[0], executionWindow[1])
                    .setPersisted(true)
                    .build()
                    .schedule()
        }
    }
}