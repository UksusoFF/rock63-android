package com.uksusoff.rock63.jobs

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.uksusoff.rock63.R
import com.uksusoff.rock63.data.DataSource
import com.uksusoff.rock63.data.DataSource.NoInternetException
import com.uksusoff.rock63.data.DataSource_
import com.uksusoff.rock63.data.UserPrefs_
import com.uksusoff.rock63.data.entities.Event
import com.uksusoff.rock63.ui.EventDetailActivity_
import com.uksusoff.rock63.ui.NewsListActivity_
import com.uksusoff.rock63.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by User on 28.08.2016.
 */
class NotificationJob : Job() {
    private enum class ReminderType {
        WEEKLY, DAILY
    }

    private lateinit var userPrefs: UserPrefs_
    private lateinit var notificationManager: NotificationManager
    private lateinit var dataSource: DataSource

    override fun onRunJob(params: Params): Result {
        init()
        checkScheduledJob()
        scheduleTask()
        return Result.SUCCESS
    }

    private fun scheduleTask() {
        val executionWindow = nextExecutionWindow
        JobRequest.Builder(TAG)
                .setExecutionWindow(executionWindow[0], executionWindow[1])
                .setPersisted(true)
                .build()
                .schedule()
    }

    private fun init() {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        userPrefs = UserPrefs_(context)
        dataSource = DataSource_.getInstance_(context)
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
        var events = dataSource.getAllEvents(false)
        if (events.isEmpty()) {
            events = try {
                dataSource.refreshEvents()
                dataSource.getAllEvents(false)
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
            val diff = event.start!!.time - start.time
            if (weeklyReminder && diff < weekInterval && diff > weekInterval - INTERVAL) {
                showReminderNotification(event, ReminderType.WEEKLY)
            } else if (dailyReminder && diff < AlarmManager.INTERVAL_DAY && diff > AlarmManager.INTERVAL_DAY - INTERVAL) {
                showReminderNotification(event, ReminderType.DAILY)
            }
        }
    }

    private fun showReminderNotification(event: Event, type: ReminderType) {
        val contentMap: MutableMap<ReminderType, Int> = EnumMap(com.uksusoff.rock63.jobs.NotificationJob.ReminderType::class.java)
        contentMap[ReminderType.DAILY] = R.string.notification_dayly
        contentMap[ReminderType.WEEKLY] = R.string.notification_weekly
        var place: String? = ""
        val contentResId = contentMap[type]!!
        if (event.place != null) {
            place = event.place!!.name + " "
        }
        place += SimpleDateFormat("HH:mm", Locale.getDefault()).format(event.start)
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(context.getString(contentResId, event.title))
                .setContentText(place) as NotificationCompat.Builder
        val resultIntent: Intent = EventDetailActivity_.intent(context).eventId(event.id).get()
        val resultPendingIntent = PendingIntent.getActivity(
                context,
                event.id,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        notificationManager.notify(event.id, mBuilder.build())
    }

    companion object {
        const val TAG = "event_notification_job"
        private const val INTERVAL = AlarmManager.INTERVAL_DAY
        @JvmStatic
        val nextExecutionWindow: LongArray
            get() {
                val start = DateUtils.getDelayToHour(17)
                var end = DateUtils.getDelayToHour(19)
                if (start > end) {
                    end += AlarmManager.INTERVAL_DAY
                }
                return longArrayOf(start, end)
            }

        private val weekInterval: Long
            get() = AlarmManager.INTERVAL_DAY * 7
    }
}