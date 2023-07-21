package com.uksusoff.rock63.jobs

import com.evernote.android.job.Job
import com.uksusoff.rock63.data.DataSource
import com.uksusoff.rock63.data.DataSource.NoInternetException
import com.uksusoff.rock63.data.DataSource_
import com.uksusoff.rock63.ui.EventDetailActivity_
import com.uksusoff.rock63.ui.NewsListActivity_

/**
 * Created by User on 28.08.2016.
 */
class DataUpdateJob : Job() {
    override fun onRunJob(params: Params): Result {
        val dataSource: DataSource = DataSource_.getInstance_(context)
        try {
            dataSource.refreshEvents()
            dataSource.refreshNews()
        } catch (e: NoInternetException) { //Just not this time
        }
        return Result.SUCCESS
    }

    companion object {
        const val TAG = "data_update_job"
    }
}