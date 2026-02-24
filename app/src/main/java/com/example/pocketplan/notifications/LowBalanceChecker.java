package com.example.pocketplan.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.pocketplan.DatabaseHelper;

import utils.SessionManager;

public class LowBalanceChecker {

    private static final String TAG            = "LowBalanceChecker";
    private static final String PREFS_SETTINGS = "NotificationSettings";
    private static final String KEY_THRESHOLD  = "low_balance_threshold";
    private static final String KEY_ALERTED    = "low_balance_alerted_";  // per-user suffix

    public static final double DEFAULT_THRESHOLD = 1000.0;

    public static void check(Context context) {

        // Respect user's notification toggle
        if (!NotificationHelper.areNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications off — skipping low balance check");
            return;
        }

        int userId = new SessionManager(context).getUserId();
        if (userId == -1) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
        double threshold = Double.longBitsToDouble(
                prefs.getLong(KEY_THRESHOLD, Double.doubleToLongBits(DEFAULT_THRESHOLD)));

        DatabaseHelper db = new DatabaseHelper(context);
        double salary  = db.getSalary(userId);
        double income  = db.getTotalIncome(userId);
        double expense = db.getTotalExpense(userId);
        double balance = salary + income - expense;
        db.close();

        Log.d(TAG, "Balance: " + balance + " | Threshold: " + threshold);

        String alertKey = KEY_ALERTED + userId;

        if (balance < threshold) {
            boolean alreadyAlerted = prefs.getBoolean(alertKey, false);
            if (!alreadyAlerted) {
                Log.d(TAG, "Low balance — firing notification");
                NotificationHelper.showLowBalance(context, balance, threshold);
                prefs.edit().putBoolean(alertKey, true).apply();
            }
        } else {
            prefs.edit().putBoolean(alertKey, false).apply();
        }
    }

    public static void setThreshold(Context context, double threshold) {
        int userId = new SessionManager(context).getUserId();
        context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_THRESHOLD, Double.doubleToLongBits(threshold))
                .putBoolean(KEY_ALERTED + userId, false)
                .apply();
    }

    public static double getThreshold(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(
                prefs.getLong(KEY_THRESHOLD, Double.doubleToLongBits(DEFAULT_THRESHOLD)));
    }
}