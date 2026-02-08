package com.example.pocketplan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    // UI Components
    private TextView tvGreeting;
    private TextView tvUserName;
    private TextView tvTotalBalance;
    private TextView tvIncome;
    private TextView tvExpense;

    // Feature Cards
    private MaterialCardView cardAddExpense;
    private MaterialCardView cardTransactions;
    private MaterialCardView cardBudget;
    private MaterialCardView cardReports;

    // Bottom Navigation
    private BottomNavigationView bottomNavigation;

    // Database
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Check if user is registered
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!prefs.contains("name")) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupGreeting();

        tvUserName.setText(prefs.getString("name", "User"));

        setupClickListeners();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        double salary = databaseHelper.getSalary();

        if (salary <= 0) {
            startActivity(new Intent(this, SalaryActivity.class));
        } else {
            loadBalance();
        }
    }

    private void initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);

        cardAddExpense = findViewById(R.id.cardAddExpense);
        cardTransactions = findViewById(R.id.cardTransactions);
        cardBudget = findViewById(R.id.cardBudget);
        cardReports = findViewById(R.id.cardReports);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) greeting = "Good Morning,";
        else if (hour < 17) greeting = "Good Afternoon,";
        else greeting = "Good Evening,";

        tvGreeting.setText(greeting);
    }

    private void setupClickListeners() {

        cardAddExpense.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class)));

        cardTransactions.setOnClickListener(v ->
                startActivity(new Intent(this, TransactionsActivity.class)));

        cardBudget.setOnClickListener(v ->
                startActivity(new Intent(this, SalaryActivity.class)));

        cardReports.setOnClickListener(v ->
                Toast.makeText(this, "Reports coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {

        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_dashboard) return true;

            if (item.getItemId() == R.id.nav_transactions) {
                startActivity(new Intent(this, TransactionsActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    // =============================
    // SAFE BALANCE LOADING (NO CRASH)
    // =============================
    private void loadBalance() {

        double salary = databaseHelper.getSalary();

        double income = 0;
        double expense = 0;

        try {
            income = databaseHelper.getTotalIncome();
            expense = databaseHelper.getTotalExpense();
        } catch (Exception e) {
            // Prevent crash if methods not yet implemented
        }

        double totalBalance = salary + income - expense;
        updateBalance(totalBalance, income, expense);
    }

    private void updateBalance(double totalBalance, double income, double expense) {
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", totalBalance));
        tvIncome.setText(String.format(Locale.getDefault(), "₹%.2f", income));
        tvExpense.setText(String.format(Locale.getDefault(), "₹%.2f", expense));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
