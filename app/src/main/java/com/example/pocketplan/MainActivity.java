package com.example.pocketplan;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import db.DBHelper;
import utils.NotificationUtil;

public class MainActivity extends AppCompatActivity {

    DBHelper db;
    int userId = 1; // temporary (later from SessionManager)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);

        // TEMP: test expense insert
        testAddExpense();
    }

    private void testAddExpense() {

        boolean inserted = db.addExpense(
                250.0,
                "Food",
                "2026-02-01",
                "Lunch",
                userId
        );

        if (inserted) {
            Log.d("DB_TEST", "Expense added successfully");

            if (db.isBudgetExceeded(userId)) {
                NotificationUtil.showAlert(
                        this,
                        "Budget limit exceeded!"
                );
            }
        }
    }
}
