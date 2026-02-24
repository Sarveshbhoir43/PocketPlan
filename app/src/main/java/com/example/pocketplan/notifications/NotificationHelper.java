package com.example.pocketplan.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.pocketplan.DashboardActivity;
import com.example.pocketplan.R;

import utils.SessionManager;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    // Channel IDs
    public static final String CHANNEL_BUDGET  = "channel_budget_alert";
    public static final String CHANNEL_WEEKLY  = "channel_weekly_summary";
    public static final String CHANNEL_BALANCE = "channel_low_balance";

    // Notification IDs
    public static final int NOTIF_BUDGET_BASE = 1000;
    public static final int NOTIF_WEEKLY      = 2000;
    public static final int NOTIF_LOW_BALANCE = 3000;

    // SharedPreferences key for the notification toggle
    private static final String PREFS_USER    = "UserPrefs";
    private static final String KEY_NOTIF_ON  = "notifications_enabled_";

    // ─── Create channels (call once at app start) ─────────────────────────────
    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = context.getSystemService(NotificationManager.class);

        NotificationChannel budgetCh = new NotificationChannel(
                CHANNEL_BUDGET, "Budget Alerts", NotificationManager.IMPORTANCE_HIGH);
        budgetCh.setDescription("Notifies when a category budget is exceeded.");
        nm.createNotificationChannel(budgetCh);

        NotificationChannel weeklyCh = new NotificationChannel(
                CHANNEL_WEEKLY, "Weekly Summary", NotificationManager.IMPORTANCE_DEFAULT);
        weeklyCh.setDescription("Weekly spending summary every Sunday.");
        nm.createNotificationChannel(weeklyCh);

        NotificationChannel balanceCh = new NotificationChannel(
                CHANNEL_BALANCE, "Low Balance Warning", NotificationManager.IMPORTANCE_HIGH);
        balanceCh.setDescription("Alerts when your total balance is low.");
        nm.createNotificationChannel(balanceCh);
    }

    /**
     * Returns true if the user has notifications enabled in their profile toggle.
     * Defaults to true (on) for new users.
     */
    public static boolean areNotificationsEnabled(Context context) {
        int userId = new SessionManager(context).getUserId();
        if (userId == -1) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_NOTIF_ON + userId, true);
    }

    // ─── Budget Exceeded ──────────────────────────────────────────────────────
    public static void showBudgetExceeded(Context context, String category,
                                          double spent, double budget) {
        String title = "⚠️ Budget Exceeded: " + category;
        String body  = String.format("You've spent ₹%.0f of your ₹%.0f budget.", spent, budget);
        int notifId  = NOTIF_BUDGET_BASE + Math.abs(category.hashCode() % 500);
        show(context, CHANNEL_BUDGET, notifId, title, body);
    }

    // ─── Weekly Summary ───────────────────────────────────────────────────────
    public static void showWeeklySummary(Context context, double weeklyExpense,
                                         double weeklyIncome, String topCategory) {
        String title = "📊 Your Weekly Summary";
        String body  = String.format(
                "Spent: ₹%.0f  |  Earned: ₹%.0f\nTop category: %s",
                weeklyExpense, weeklyIncome, topCategory.isEmpty() ? "N/A" : topCategory);
        show(context, CHANNEL_WEEKLY, NOTIF_WEEKLY, title, body);
    }

    // ─── Low Balance ──────────────────────────────────────────────────────────
    public static void showLowBalance(Context context, double balance, double threshold) {
        String title = "💸 Low Balance Warning";
        String body  = String.format(
                "Your balance is ₹%.0f, below your ₹%.0f threshold.", balance, threshold);
        show(context, CHANNEL_BALANCE, NOTIF_LOW_BALANCE, title, body);
    }

    // ─── Internal show helper ─────────────────────────────────────────────────
    private static void show(Context context, String channel, int notifId,
                             String title, String body) {

        // 1. Check user toggle
        if (!areNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications disabled by user — skipping: " + title);
            return;
        }

        // 2. Check Android 13+ POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted — skipping: " + title);
                return;
            }
        }

        // 3. Build and fire
        Intent intent = new Intent(context, DashboardActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pi = PendingIntent.getActivity(
                context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_wallet)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build());
            Log.d(TAG, "Notification sent: " + title);
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException sending notification: " + e.getMessage());
        }
    }
}