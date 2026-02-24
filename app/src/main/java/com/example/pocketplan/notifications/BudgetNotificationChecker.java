package com.example.pocketplan.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.pocketplan.DatabaseHelper;

import utils.SessionManager;

public class BudgetNotificationChecker {

    private static final String TAG          = "BudgetChecker";
    private static final String PREFS_BUDGET  = "BudgetPrefs";
    private static final String PREFS_ALERTED = "BudgetAlerted";

    // Category name → SharedPreferences key (must match BudgetActivity and AddTransactionActivity)
    private static final String[][] CATEGORIES = {
            {"Food & Dining",     "budget_food"},
            {"Transportation",    "budget_transport"},
            {"Shopping",          "budget_shopping"},
            {"Bills & Utilities", "budget_bills"},
            {"Entertainment",     "budget_entertainment"},
            {"Other",             "budget_other"},
    };

    /**
     * Call this immediately after saving any expense transaction.
     * Compares each category's total spending against its saved budget limit
     * and fires a notification the first time spending exceeds the limit.
     */
    public static void checkAllCategories(Context context) {

        // Respect user's notification toggle
        if (!NotificationHelper.areNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications off — skipping budget check");
            return;
        }

        // Get current user
        int userId = new SessionManager(context).getUserId();
        if (userId == -1) {
            Log.d(TAG, "No user logged in — skipping budget check");
            return;
        }

        DatabaseHelper db = new DatabaseHelper(context);
        SharedPreferences budgetPrefs  = context.getSharedPreferences(PREFS_BUDGET,  Context.MODE_PRIVATE);
        SharedPreferences alertedPrefs = context.getSharedPreferences(PREFS_ALERTED, Context.MODE_PRIVATE);

        for (String[] cat : CATEGORIES) {
            String name    = cat[0];
            String prefKey = cat[1];

            float budget = budgetPrefs.getFloat(prefKey, 0f);
            if (budget <= 0) continue; // No budget set for this category → skip

            double spent = db.getExpenseByCategory(name, userId);

            Log.d(TAG, "Category: " + name + " | Spent: " + spent + " | Budget: " + budget);

            if (spent > budget) {
                String alertKey = "alerted_" + prefKey + "_" + userId;
                boolean alreadyAlerted = alertedPrefs.getBoolean(alertKey, false);

                if (!alreadyAlerted) {
                    Log.d(TAG, "Budget exceeded for " + name + " — firing notification");
                    NotificationHelper.showBudgetExceeded(context, name, spent, budget);
                    alertedPrefs.edit().putBoolean(alertKey, true).apply();
                } else {
                    Log.d(TAG, "Already alerted for " + name + " — skipping");
                }
            } else {
                // Spending back under budget → reset so it can alert again next time
                String alertKey = "alerted_" + prefKey + "_" + userId;
                alertedPrefs.edit().putBoolean(alertKey, false).apply();
            }
        }

        db.close();
    }

    /**
     * Call when user saves new budget values — resets all alert flags
     * so notifications can re-trigger if spending still exceeds the new limits.
     */
    public static void resetAlerts(Context context) {
        int userId = new SessionManager(context).getUserId();
        if (userId == -1) return;
        SharedPreferences alertedPrefs = context.getSharedPreferences(PREFS_ALERTED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = alertedPrefs.edit();
        for (String[] cat : CATEGORIES) {
            editor.remove("alerted_" + cat[1] + "_" + userId);
        }
        editor.apply();
        Log.d(TAG, "Budget alert flags reset for user " + userId);
    }
}