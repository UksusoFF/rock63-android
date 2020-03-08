package com.uksusoff.rock63.workers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.DataProviderComponent
import com.uksusoff.rock63.data.DataProviderComponent.NoInternetException
import com.uksusoff.rock63.data.DataProviderComponent_
import com.uksusoff.rock63.data.UserPrefs_
import com.uksusoff.rock63.data.entities.Event
import com.uksusoff.rock63.ui.EventDetailActivity_
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by User on 28.08.2016.
 */
class NotificationsWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    private enum class ReminderType {
        WEEKLY, DAILY
    }

    private lateinit var userPrefs: UserPrefs_
    private lateinit var notificationManager: NotificationManager
    private lateinit var dataProviderComponent: DataProviderComponent

    override fun doWork(): Result {
        init()
        checkScheduledJob()
        return Result.success()
    }

    private fun init() {
        notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        userPrefs = UserPrefs_(applicationContext)
        dataProviderComponent = DataProviderComponent_.getInstance_(applicationContext)
    }

    private val todayMidnight: Date
        get() {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_MONTH, 1)
            c[Calendar.HOUR_OF_DAY] = 0
            c[Calendar.MINUTE] = 0
            c[Calendar.SECOND] = 0
            c[Calendar.MILLISECOND] = 0
            return c.time
        }

    private fun checkScheduledJob() {
        var events = dataProviderComponent.getAllEvents(false)
        if (events.isEmpty()) {
            events = try {
                dataProviderComponent.refreshEvents()
                dataProviderComponent.getAllEvents(false)
            } catch (e: NoInternetException) { //Well, till next time
                return
            }
        }
        val weeklyReminder: Boolean = userPrefs.remindWeekBefore().get()
        val dailyReminder: Boolean = userPrefs.remindDayBefore().get()
        if (!weeklyReminder && !dailyReminder) {
            return
        }
        val start = todayMidnight
        for (event in events) {
            if (!event.isNotify) {
                continue
            }

            val startTime = event.start ?: continue
            val diff = startTime.time - start.time
            if (weeklyReminder && diff < weekInterval && diff > weekInterval - INTERVAL) {
                showReminderNotification(event, ReminderType.WEEKLY)
            } else if (dailyReminder && diff < AlarmManager.INTERVAL_DAY &&
                    diff > AlarmManager.INTERVAL_DAY - INTERVAL) {
                showReminderNotification(event, ReminderType.DAILY)
            }
        }
    }

    private fun showReminderNotification(event: Event, type: ReminderType) {
        val contentMap: MutableMap<ReminderType, Int> = EnumMap(ReminderType::class.java)
        contentMap[ReminderType.DAILY] = R.string.notification_dayly
        contentMap[ReminderType.WEEKLY] = R.string.notification_weekly

        val contentResId = contentMap[type]!!

        val place = event.place?.let { "${it.name} " } ?: "" + SimpleDateFormat(
                "HH:mm", 
                Locale.getDefault()
        ).format(event.start)

        val mBuilder: NotificationCompat.Builder = NotificationCompat
                .Builder(applicationContext, provideChannel())
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(applicationContext.getString(contentResId, event.title))
                .setContentText(place) as NotificationCompat.Builder

        val resultIntent: Intent = EventDetailActivity_
                .intent(applicationContext)
                .eventId(event.id)
                .get()

        val resultPendingIntent = PendingIntent.getActivity(
                applicationContext,
                event.id,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        mBuilder.setContentIntent(resultPendingIntent)
        notificationManager.notify(event.id, mBuilder.build())
    }

    private fun provideChannel(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return ""

        val mNotificationManager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = "notification_channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(id, "Reminders Channel", importance)
        mNotificationManager.createNotificationChannel(mChannel)
        return id
    }

    companion object {
        private const val INTERVAL = AlarmManager.INTERVAL_DAY
        private val weekInterval: Long
            get() = AlarmManager.INTERVAL_DAY * 7
    }
}