package com.example.pocketplan.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

/**
 * Schedules a repeating alarm that fires every Sunday at 8:00 PM.
 * Call schedule() once from your Application or DashboardActivity.onCreate().
 */
public class WeeklyScheduler {

    private static final String TAG = "WeeklyScheduler";

    public static void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeeklySummaryReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Next Sunday at 8:00 PM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If it's already past this Sunday's 8 PM, schedule for next Sunday
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        long triggerAt = calendar.getTimeInMillis();
        long intervalWeekly = AlarmManager.INTERVAL_DAY * 7;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP, triggerAt, intervalWeekly, pendingIntent);
            }
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, triggerAt, intervalWeekly, pendingIntent);
        }

        Log.d(TAG, "Weekly summary scheduled for: " + calendar.getTime());
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeeklySummaryReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Weekly summary alarm cancelled.");
    }
}