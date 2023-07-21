package com.uksusoff.rock63

import android.app.Application
import androidx.work.*
import com.uksusoff.rock63.utils.DateUtils
import com.uksusoff.rock63.workers.DataUpdateWorker
import com.uksusoff.rock63.workers.NotificationsWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Created by User on 17.09.2016.
 */
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        scheduleWorks()
    }

    private fun scheduleWorks() {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "data_update",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<DataUpdateWorker>( 1, TimeUnit.DAYS )
                        .setConstraints(
                            Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .setRequiresBatteryNotLow(true)
                                    .build()
                        )
                        .build()
        )

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "notifications",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<NotificationsWorker>( 1, TimeUnit.DAYS )
                        .setInitialDelay(DateUtils.getDelayToHour(18), TimeUnit.MILLISECONDS)
                        .build()
        )
    }
}