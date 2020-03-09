package com.uksusoff.rock63.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.uksusoff.rock63.data.DataProviderComponent
import com.uksusoff.rock63.data.DataProviderComponent_
import com.uksusoff.rock63.exceptions.NoInternetException

/**
 * Created by Vyacheslav Vodyanov on 28.08.2016.
 */
class DataUpdateWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val dataProviderComponent: DataProviderComponent =
                DataProviderComponent_.getInstance_(this.applicationContext)

        try {
            dataProviderComponent.refreshEvents()
            dataProviderComponent.refreshNews()
        } catch (e: NoInternetException) {
            //Just not this time
        }

        return Result.success()
    }
}