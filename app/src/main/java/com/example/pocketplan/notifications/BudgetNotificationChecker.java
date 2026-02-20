package com.example.pocketplan.notifications;


import android.content.Context;
import android.content.SharedPreferences;

import com.example.pocketplan.DatabaseHelper;

/**
 * Call checkAllCategories() right after any transaction is saved.
 * It compares each category's spending against its saved budget and
 * fires a notification the FIRST time the limit is crossed each session.
 */
public class BudgetNotificationChecker {

    private static final String PREFS_BUDGET  = "BudgetPrefs";
    private static final String PREFS_ALERTED = "BudgetAlerted"; // tracks already-notified

    // Must match category names used in AddTransactionActivity
    private static final String[][] CATEGORIES = {
            {"Food & Dining",    "budget_food"},
            {"Transportation",   "budget_transport"},
            {"Shopping",         "budget_shopping"},
            {"Bills & Utilities","budget_bills"},
            {"Entertainment",    "budget_entertainment"},
            {"Other",            "budget_other"},
    };

    public static void checkAllCategories(Context context) {
        DatabaseHelper db     = new DatabaseHelper(context);
        SharedPreferences budgetPrefs  = context.getSharedPreferences(PREFS_BUDGET,  Context.MODE_PRIVATE);
        SharedPreferences alertedPrefs = context.getSharedPreferences(PREFS_ALERTED, Context.MODE_PRIVATE);

        for (String[] cat : CATEGORIES) {
            String name      = cat[0];
            String prefKey   = cat[1];

            float  budget = budgetPrefs.getFloat(prefKey, 0f);
            if (budget <= 0) continue; // no budget set for this category

            double spent  = db.getExpenseByCategory(name);

            if (spent > budget) {
                // Only notify once per threshold crossing (reset when budget is saved again)
                String alertKey = "alerted_" + prefKey;
                boolean alreadyAlerted = alertedPrefs.getBoolean(alertKey, false);

                if (!alreadyAlerted) {
                    NotificationHelper.showBudgetExceeded(context, name, spent, budget);
                    alertedPrefs.edit().putBoolean(alertKey, true).apply();
                }
            } else {
                // Spending is back under budget → reset so we can alert again if it crosses
                alertedPrefs.edit().putBoolean("alerted_" + prefKey, false).apply();
            }
        }

        db.close();
    }

    /** Call this when user saves new budgets so alerts can re-trigger if needed. */
    public static void resetAlerts(Context context) {
        context.getSharedPreferences(PREFS_ALERTED, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
}
