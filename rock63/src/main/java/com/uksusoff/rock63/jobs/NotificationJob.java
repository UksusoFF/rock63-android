package com.uksusoff.rock63.jobs;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.uksusoff.rock63.R;
import com.uksusoff.rock63.data.DataSource;
import com.uksusoff.rock63.data.DataSource_;
import com.uksusoff.rock63.data.UserPrefs_;
import com.uksusoff.rock63.data.entities.Event;
import com.uksusoff.rock63.receivers.RemindersReceiver;
import com.uksusoff.rock63.ui.EventDetailActivity_;
import com.uksusoff.rock63.ui.NewsListActivity_;
import com.uksusoff.rock63.utils.DateUtils;

import org.androidannotations.annotations.SystemService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by User on 28.08.2016.
 */
public class NotificationJob extends Job {

    public static final String TAG = "event_notification_job";

    private enum ReminderType {
        WEEKLY,
        DAILY,
        TODAY
    }

    UserPrefs_ userPrefs;
    NotificationManager notificationManager;

    DataSource dataSource;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        init();
        checkScheduledJob();
        return Result.SUCCESS;
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

    private void showTestNotification() {
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("checkScheduledJob running...")
                        .setContentText("Test");

        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder.setContentIntent(PendingIntent.getActivity(getContext(), 0,
                new Intent(getContext(), NewsListActivity_.class), 0));

        notificationManager.notify(999, mBuilder.build());
    }

    private void checkScheduledJob() {
        showTestNotification();

        List<Event> events = dataSource.getAllEvents(false);

        if (events.isEmpty()) {
            try {
                dataSource.refreshEvents();
                events = dataSource.getAllEvents(false);
            } catch (DataSource.NoInternetException e) {
                //Well, till next time
                return;
            }
        }

        boolean weeklyReminder = userPrefs.remindWeekBefore().get();
        boolean dailyReminder = userPrefs.remindDayBefore().get();

        if (!weeklyReminder && !dailyReminder) {
            return;
        }

        Date now = new Date();
        for (Event event : events) {
            if (!event.isNotify()) {
                continue;
            }

            long diff = event.getStart().getTime() - now.getTime();
            if (weeklyReminder &&
                    diff < getWeekInterval() &&
                    diff > getWeekInterval() - RemindersReceiver.getRepeatInterval()) {

                showReminderNotification(event, ReminderType.WEEKLY);
            } else if (dailyReminder &&
                    diff < AlarmManager.INTERVAL_DAY &&
                    diff > AlarmManager.INTERVAL_DAY - RemindersReceiver.getRepeatInterval()) {

                if (DateUtils.getDateMonthDay(event.getStart()) != DateUtils.getDateMonthDay(now)) {
                    showReminderNotification(event, ReminderType.DAILY);
                } else {
                    showReminderNotification(event, ReminderType.TODAY);
                }
            }
        }
    }

    protected void showReminderNotification(Event event, ReminderType type) {

        Map<ReminderType, Integer> contentMap = new HashMap<>();
        contentMap.put(ReminderType.DAILY, R.string.notification_dayly);
        contentMap.put(ReminderType.WEEKLY, R.string.notification_weekly);
        contentMap.put(ReminderType.TODAY, R.string.notification_today);

        String place = "";
        int contentResId = contentMap.get(type);

        if (event.getPlace() != null) {
            place = event.getPlace().getName() + " ";
        }

        place += (new SimpleDateFormat("HH:mm", Locale.getDefault())).format(event.getStart());

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getContext().getString(contentResId, event.getTitle()))
                        .setContentText(place);

        Intent resultIntent = EventDetailActivity_.intent(getContext()).eventId(event.getId()).get();

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getContext(),
                        event.getId(),
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(event.getId(), mBuilder.build());
    }

}
