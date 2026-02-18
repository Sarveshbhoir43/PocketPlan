package com.example.pocketplan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
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
    private ImageView imgProfile;  // ADDED

    // Feature Cards
    private MaterialCardView cardAddExpense;
    private MaterialCardView cardAddIncome;
    private MaterialCardView cardBudget;
    private MaterialCardView cardReports;

    // Clear All Button
    private MaterialButton btnClearAll;

    // Bottom Navigation
    private BottomNavigationView bottomNavigation;

    // Database
    private DatabaseHelper databaseHelper;

    // SharedPreferences
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is registered
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
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

        // Load profile image
        loadProfileImage();

        setupClickListeners();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - loading balance");
        loadBalance();
        loadProfileImage(); // Reload profile image when returning to this activity
    }

    private void initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        imgProfile = findViewById(R.id.imgProfile);  // ADDED

        cardAddExpense = findViewById(R.id.cardAddExpense);
        cardAddIncome = findViewById(R.id.cardAddIncome);
        cardBudget = findViewById(R.id.cardBudget);
        cardReports = findViewById(R.id.cardReports);

        btnClearAll = findViewById(R.id.btnClearAll);

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

    // ADDED: Load profile image from SharedPreferences
    private void loadProfileImage() {
        try {
            String imageBase64 = prefs.getString("profile_image", null);
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgProfile.setImageBitmap(bitmap);
                Log.d(TAG, "Profile image loaded successfully");
            } else {
                // Set default placeholder
                imgProfile.setImageResource(R.drawable.ic_profile);
                Log.d(TAG, "No profile image found, using placeholder");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image: " + e.getMessage(), e);
            imgProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    private void setupClickListeners() {

        // Profile image click - navigate to Profile
        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        // Add Expense Card - Navigate to AddTransactionActivity
        cardAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            startActivity(intent);
        });

        // Add Income Card - Show quick dialog and save directly
        cardAddIncome.setOnClickListener(v -> showAddIncomeDialog());

        // Budget/Salary card
        cardBudget.setOnClickListener(v -> showSalaryDialog());

        // Reports card - Navigate to Transactions
        cardReports.setOnClickListener(v ->
                startActivity(new Intent(this, TransactionsActivity.class)));

        // Clear All Button
        btnClearAll.setOnClickListener(v -> showClearAllDialog());
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

    private void showAddIncomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Income");

        // Create input layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // Amount input
        final TextInputEditText inputAmount = new TextInputEditText(this);
        inputAmount.setHint("Enter amount");
        inputAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputAmount);

        // Description input
        final TextInputEditText inputDescription = new TextInputEditText(this);
        inputDescription.setHint("Description (optional)");
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 20;
        inputDescription.setLayoutParams(params);
        layout.addView(inputDescription);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String amountStr = inputAmount.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);

                if (amount <= 0) {
                    Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.isEmpty()) {
                    description = "Income";
                }

                // Save income transaction directly to database
                boolean success = saveIncomeTransaction(amount, description);

                if (success) {
                    Toast.makeText(this, "Income added successfully!", Toast.LENGTH_SHORT).show();
                    loadBalance();
                } else {
                    Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private boolean saveIncomeTransaction(double amount, String description) {
        try {
            long timestamp = System.currentTimeMillis();

            long result = databaseHelper.addTransaction(
                    description,
                    "Income",
                    amount,
                    "",
                    "INCOME",
                    timestamp
            );

            Log.d(TAG, "Income transaction saved with ID: " + result);
            return result != -1;

        } catch (Exception e) {
            Log.e(TAG, "Error saving income: " + e.getMessage(), e);
            return false;
        }
    }

    private void showSalaryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Monthly Salary");

        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Enter your monthly salary");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

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
                    loadBalance();
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

    private void showClearAllDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("This will delete:\n\n• All transactions (Income & Expenses)\n• Monthly salary\n• All financial records\n\nThis action cannot be undone!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Clear All", (dialog, which) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Are you absolutely sure?")
                            .setMessage("This will permanently delete all your financial data!")
                            .setPositiveButton("Yes, Delete Everything", (d, w) -> clearAllData())
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllData() {
        try {
            boolean transactionsCleared = databaseHelper.clearAllTransactions();
            boolean salaryCleared = databaseHelper.setSalary(0);

            if (transactionsCleared && salaryCleared) {
                Toast.makeText(this, "✓ All data cleared successfully", Toast.LENGTH_LONG).show();
                loadBalance();
            } else {
                Toast.makeText(this, "Failed to clear some data", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error clearing data: " + e.getMessage(), e);
            Toast.makeText(this, "Error clearing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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