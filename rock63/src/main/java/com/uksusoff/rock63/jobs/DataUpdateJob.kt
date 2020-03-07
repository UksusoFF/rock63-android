package com.uksusoff.rock63.jobs

import com.evernote.android.job.Job
import com.uksusoff.rock63.data.DataProviderComponent
import com.uksusoff.rock63.data.DataProviderComponent.NoInternetException
import com.uksusoff.rock63.data.DataProviderComponent_

/**
 * Created by User on 28.08.2016.
 */
class DataUpdateJob : Job() {
    override fun onRunJob(params: Params): Result {
        val dataProviderComponent: DataProviderComponent = DataProviderComponent_.getInstance_(context)
        try {
            dataProviderComponent.refreshEvents()
            dataProviderComponent.refreshNews()
        } catch (e: NoInternetException) { //Just not this time
        }
        return Result.SUCCESS
    }

    companion object {
        const val TAG = "data_update_job"
    }
}