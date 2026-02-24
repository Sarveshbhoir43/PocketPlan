package com.example.pocketplan.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.pocketplan.DatabaseHelper;
import utils.SessionManager;
import com.example.pocketplan.models.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fires every Sunday at the scheduled time.
 * Calculates the past 7 days of spending/income and shows a summary notification.
 */
public class WeeklySummaryReceiver extends BroadcastReceiver {

    private static final String TAG = "WeeklySummaryReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Weekly summary alarm triggered");

        DatabaseHelper db = new DatabaseHelper(context);
        int _uid = new SessionManager(context).getUserId();
        if (_uid == -1) return; // No user logged in

        // Time window: last 7 days
        long now       = System.currentTimeMillis();
        long weekStart = now - (7L * 24 * 60 * 60 * 1000);

        double weeklyExpense = 0;
        double weeklyIncome  = 0;
        Map<String, Double> categoryTotals = new HashMap<>();

        List<Transaction> transactions = db.getAllTransactions(_uid);
        for (Transaction t : transactions) {
            if (t.getTimestamp() < weekStart) continue;
            if (t.isIncome()) {
                weeklyIncome += t.getAmount();
            } else {
                weeklyExpense += t.getAmount();
                categoryTotals.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }

        // Find top spending category
        String topCategory = "";
        double topAmount   = 0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > topAmount) {
                topAmount   = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        db.close();

        NotificationHelper.showWeeklySummary(context, weeklyExpense, weeklyIncome, topCategory);
    }
}