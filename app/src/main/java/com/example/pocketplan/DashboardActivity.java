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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // ------------------------
        // Check if user is registered
        // ------------------------
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!prefs.contains("name")) {
            // User not registered → go to registration
            Intent intent = new Intent(DashboardActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        // Initialize views
        initializeViews();

        // Set up greeting based on time
        setupGreeting();

        // Load user name from SharedPreferences
        tvUserName.setText(prefs.getString("name", "User"));

        // Set up click listeners
        setupClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void initializeViews() {
        // Header views
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);

        // Balance card views
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);

        // Feature cards
        cardAddExpense = findViewById(R.id.cardAddExpense);
        cardTransactions = findViewById(R.id.cardTransactions);
        cardBudget = findViewById(R.id.cardBudget);
        cardReports = findViewById(R.id.cardReports);

        // Bottom navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hourOfDay >= 0 && hourOfDay < 12) {
            greeting = "Good Morning,";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            greeting = "Good Afternoon,";
        } else {
            greeting = "Good Evening,";
        }

        tvGreeting.setText(greeting);
    }

    private void setupClickListeners() {
        // Add Expense Card
        cardAddExpense.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Add Expense...", Toast.LENGTH_SHORT).show();
        });

        // Transactions Card
        cardTransactions.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Transactions...", Toast.LENGTH_SHORT).show();
        });

        // Budget Planner Card
        cardBudget.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Budget Planner...", Toast.LENGTH_SHORT).show();
        });

        // Reports Card
        cardReports.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Reports...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        // Set Dashboard as selected
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    // Already on dashboard
                    return true;
                } else if (itemId == R.id.nav_transactions) {
                    Intent intent = new Intent(DashboardActivity.this, TransactionsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });
    }

    // Method to update balance dynamically (can be called from other parts of the app)
    public void updateBalance(double totalBalance, double income, double expense) {
        tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", totalBalance));
        tvIncome.setText(String.format(Locale.getDefault(), "₹%.2f", income));
        tvExpense.setText(String.format(Locale.getDefault(), "₹%.2f", expense));
    }

    @Override
    public void onBackPressed() {
        // Exit app from dashboard
        finishAffinity();
    }
}
