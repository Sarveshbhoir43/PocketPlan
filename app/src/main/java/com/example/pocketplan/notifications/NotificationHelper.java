package com.example.pocketplan.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.pocketplan.DashboardActivity;
import com.example.pocketplan.R;

public class NotificationHelper {

    // Channel IDs
    public static final String CHANNEL_BUDGET   = "channel_budget_alert";
    public static final String CHANNEL_WEEKLY   = "channel_weekly_summary";
    public static final String CHANNEL_BALANCE  = "channel_low_balance";

    // Notification IDs
    public static final int NOTIF_BUDGET_BASE  = 1000; // +category offset
    public static final int NOTIF_WEEKLY       = 2000;
    public static final int NOTIF_LOW_BALANCE  = 3000;

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = context.getSystemService(NotificationManager.class);

        NotificationChannel budgetChannel = new NotificationChannel(
                CHANNEL_BUDGET, "Budget Alerts", NotificationManager.IMPORTANCE_HIGH);
        budgetChannel.setDescription("Notifies when a category budget is exceeded.");
        nm.createNotificationChannel(budgetChannel);

        NotificationChannel weeklyChannel = new NotificationChannel(
                CHANNEL_WEEKLY, "Weekly Summary", NotificationManager.IMPORTANCE_DEFAULT);
        weeklyChannel.setDescription("Weekly spending summary every Sunday.");
        nm.createNotificationChannel(weeklyChannel);

        NotificationChannel balanceChannel = new NotificationChannel(
                CHANNEL_BALANCE, "Low Balance Warning", NotificationManager.IMPORTANCE_HIGH);
        balanceChannel.setDescription("Alerts when your total balance is low.");
        nm.createNotificationChannel(balanceChannel);
    }

    // ─── Budget Exceeded ─────────────────────────────────────────────────────────
    public static void showBudgetExceeded(Context context, String category,
                                          double spent, double budget) {
        String title = "⚠️ Budget Exceeded: " + category;
        String body  = String.format("You've spent ₹%.0f of your ₹%.0f budget.", spent, budget);
        int notifId  = NOTIF_BUDGET_BASE + Math.abs(category.hashCode() % 500);

        show(context, CHANNEL_BUDGET, notifId, title, body);
    }

    // ─── Weekly Summary ──────────────────────────────────────────────────────────
    public static void showWeeklySummary(Context context,
                                         double weeklyExpense,
                                         double weeklyIncome,
                                         String topCategory) {
        String title = "📊 Your Weekly Summary";
        String body  = String.format(
                "Spent: ₹%.0f  |  Earned: ₹%.0f\nTop category: %s",
                weeklyExpense, weeklyIncome, topCategory.isEmpty() ? "N/A" : topCategory);

        show(context, CHANNEL_WEEKLY, NOTIF_WEEKLY, title, body);
    }

    // ─── Low Balance ─────────────────────────────────────────────────────────────
    public static void showLowBalance(Context context, double balance, double threshold) {
        String title = "💸 Low Balance Warning";
        String body  = String.format(
                "Your balance is ₹%.0f, which is below your ₹%.0f threshold.",
                balance, threshold);

        show(context, CHANNEL_BALANCE, NOTIF_LOW_BALANCE, title, body);
    }

    // ─── Internal helper ─────────────────────────────────────────────────────────
    private static void show(Context context, String channel, int notifId,
                             String title, String body) {
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
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS permission not granted (Android 13+)
            android.util.Log.w("NotificationHelper", "Notification permission not granted.");
        }
    }
}