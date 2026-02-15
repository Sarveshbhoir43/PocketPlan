package com.example.pocketplan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

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
        Log.d(TAG, "onResume called - loading balance");
        loadBalance();
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

        // Budget card now opens salary dialog
        cardBudget.setOnClickListener(v -> showSalaryDialog());

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

    private void loadBalance() {
        try {
            double salary = databaseHelper.getSalary();
            double income = databaseHelper.getTotalIncome();
            double expense = databaseHelper.getTotalExpense();

            double totalBalance = salary + income - expense;

            Log.d(TAG, "Balance - Salary: " + salary + ", Income: " + income +
                    ", Expense: " + expense + ", Total: " + totalBalance);

            updateBalance(totalBalance, income, expense);

        } catch (Exception e) {
            Log.e(TAG, "Error loading balance: " + e.getMessage(), e);
            updateBalance(0, 0, 0);
        }
    }

    private void updateBalance(double totalBalance, double income, double expense) {
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", totalBalance));
        tvIncome.setText(String.format(Locale.getDefault(), "₹%.2f", income));
        tvExpense.setText(String.format(Locale.getDefault(), "₹%.2f", expense));
    }

    private void showSalaryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Monthly Salary");

        // Inflate custom layout
        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Enter your monthly salary");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Set current salary
        double currentSalary = databaseHelper.getSalary();
        if (currentSalary > 0) {
            input.setText(String.valueOf(currentSalary));
        }

        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 50;
        params.rightMargin = 50;
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String salaryStr = input.getText().toString().trim();

            if (salaryStr.isEmpty()) {
                Toast.makeText(this, "Please enter salary", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double salary = Double.parseDouble(salaryStr);

                if (salary < 0) {
                    Toast.makeText(this, "Salary cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = databaseHelper.setSalary(salary);

                if (success) {
                    Toast.makeText(this, "Salary updated successfully", Toast.LENGTH_SHORT).show();
                    loadBalance(); // Refresh dashboard
                } else {
                    Toast.makeText(this, "Failed to update salary", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid salary amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigation.getSelectedItemId() != R.id.nav_dashboard) {
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        } else {
            super.onBackPressed();
        }
    }
}