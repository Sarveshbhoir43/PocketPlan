package com.example.pocketplan.notifications;


import android.content.Context;
import android.content.SharedPreferences;

import com.example.pocketplan.DatabaseHelper;

/**
 * Call check() after every transaction is saved.
 * Fires a Low Balance notification when balance drops below the user's threshold.
 */
public class LowBalanceChecker {

    private static final String PREFS_SETTINGS = "NotificationSettings";
    private static final String KEY_THRESHOLD  = "low_balance_threshold";
    private static final String KEY_ALERTED    = "low_balance_alerted";

    public static final double DEFAULT_THRESHOLD = 1000.0; // ₹1,000 default

    public static void check(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
        double threshold = Double.longBitsToDouble(
                prefs.getLong(KEY_THRESHOLD, Double.doubleToLongBits(DEFAULT_THRESHOLD)));

        DatabaseHelper db = new DatabaseHelper(context);
        double salary  = db.getSalary();
        double income  = db.getTotalIncome();
        double expense = db.getTotalExpense();
        double balance = salary + income - expense;
        db.close();

        if (balance < threshold) {
            boolean alreadyAlerted = prefs.getBoolean(KEY_ALERTED, false);
            if (!alreadyAlerted) {
                NotificationHelper.showLowBalance(context, balance, threshold);
                prefs.edit().putBoolean(KEY_ALERTED, true).apply();
            }
        } else {
            // Balance recovered → reset so it can alert again if balance drops
            prefs.edit().putBoolean(KEY_ALERTED, false).apply();
        }
    }

    /** Save user-defined threshold (called from ProfileActivity settings). */
    public static void setThreshold(Context context, double threshold) {
        context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_THRESHOLD, Double.doubleToLongBits(threshold))
                .putBoolean(KEY_ALERTED, false) // reset so it can re-trigger
                .apply();
    }

    public static double getThreshold(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(
                prefs.getLong(KEY_THRESHOLD, Double.doubleToLongBits(DEFAULT_THRESHOLD)));
    }
}