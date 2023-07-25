package com.uksusoff.rock63.jobs;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.DataSource_;
import com.uksusoff.rock63.data.UserPrefs_;
import com.uksusoff.rock63.data.entities.EventItem;
import com.uksusoff.rock63.exceptions.NoContentException;
import com.uksusoff.rock63.exceptions.NoInternetException;
import com.uksusoff.rock63.ui.EventDetailActivity_;
import com.uksusoff.rock63.utils.DateUtils;

import org.androidannotations.annotations.EBean;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EBean()
public class NotificationJob extends Job {

    public static final String TAG = "event_notification_job";

    private static final long INTERVAL = AlarmManager.INTERVAL_DAY;

    private enum ReminderType {
        WEEKLY,
        DAILY
    }

    private UserPrefs_ userPrefs;
    private NotificationManager notificationManager;
    private DataSource dataSource;

    public static long[] getNextExecutionWindow() {
        long start = DateUtils.delayToHour(17);
        long end = DateUtils.delayToHour(19);

        if (start > end) {
            end += AlarmManager.INTERVAL_DAY;
        }

        return new long[]{start, end};
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        init();
        checkScheduledJob();
        scheduleTask();
        return Result.SUCCESS;
    }

    private void scheduleTask() {
        long[] executionWindow = getNextExecutionWindow();

        new JobRequest.Builder(TAG)
                .setExecutionWindow(executionWindow[0], executionWindow[1])
                .setPersisted(true)
                .build()
                .schedule();
    }

    private void init() {
        notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        userPrefs = new UserPrefs_(getContext());
        dataSource = DataSource_.getInstance_(getContext());
    }

    private static long getWeekInterval() {
        return AlarmManager.INTERVAL_DAY * 7;
    }

    private Date getTodayMidnight() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private void checkScheduledJob() {
        List<EventItem> events = dataSource.eventsGetAll(false);

        if (events.isEmpty()) {
            try {
                dataSource.eventsRefresh();
                events = dataSource.eventsGetAll(false);
            } catch (NoInternetException | NoContentException e) {
                //Well, till next time
                return;
            }
        }

        boolean weeklyReminder = userPrefs.remindWeekBefore().get();
        boolean dailyReminder = userPrefs.remindDayBefore().get();

        if (!weeklyReminder && !dailyReminder) {
            return;
        }

        Date start = getTodayMidnight();
        for (EventItem event : events) {
            if (!event.notify) {
                continue;
            }

            long diff = event.start.getTime() - start.getTime();
            if (weeklyReminder &&
                    diff < getWeekInterval() &&
                    diff > getWeekInterval() - INTERVAL) {

                showReminderNotification(event, ReminderType.WEEKLY);
            } else if (dailyReminder &&
                    diff < AlarmManager.INTERVAL_DAY &&
                    diff > AlarmManager.INTERVAL_DAY - INTERVAL) {

                showReminderNotification(event, ReminderType.DAILY);
            }
        }
    }

    private void showReminderNotification(EventItem event, ReminderType type) {

        Map<ReminderType, Integer> contentMap = new HashMap<>();
        contentMap.put(ReminderType.DAILY, R.string.notification_daily);
        contentMap.put(ReminderType.WEEKLY, R.string.notification_weekly);

        String place = "";
        int contentResId = contentMap.get(type);

        if (event.getVenueItem() != null) {
            place = event.getVenueItem().title + " ";
        }

        place += (new SimpleDateFormat("HH:mm", Locale.getDefault())).format(event.start);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true)
                        .setContentTitle(getContext().getString(contentResId, event.title))
                        .setContentText(place);

        Intent resultIntent = EventDetailActivity_.intent(getContext()).eventId(event.id).get();

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getContext(),
                        event.id,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(event.id, mBuilder.build());
    }

}
