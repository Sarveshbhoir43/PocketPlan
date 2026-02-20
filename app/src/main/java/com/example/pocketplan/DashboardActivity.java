package com.example.pocketplan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import com.example.pocketplan.notifications.BudgetNotificationChecker;
import com.example.pocketplan.notifications.LowBalanceChecker;
import com.example.pocketplan.notifications.NotificationHelper;
import com.example.pocketplan.notifications.WeeklyScheduler;

import java.util.Calendar;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private TextView tvGreeting, tvUserName, tvTotalBalance, tvIncome, tvExpense;
    private ImageView imgProfile;
    private MaterialCardView cardAddExpense, cardAddIncome, cardBudget, cardReports;
    private MaterialButton btnClearAll;
    private BottomNavigationView bottomNavigation;
    private DatabaseHelper databaseHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!prefs.contains("name")) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);

        // Initialize notification channels & schedule weekly summary
        NotificationHelper.createChannels(this);
        WeeklyScheduler.schedule(this);

        initializeViews();
        setupGreeting();
        tvUserName.setText(prefs.getString("name", "User"));
        loadProfileImage();
        setupClickListeners();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBalance();
        loadProfileImage();
    }

    private void initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        imgProfile = findViewById(R.id.imgProfile);

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
        if (hour < 12)       greeting = "Good Morning,";
        else if (hour < 17)  greeting = "Good Afternoon,";
        else                 greeting = "Good Evening,";
        tvGreeting.setText(greeting);
    }

    private void loadProfileImage() {
        try {
            String imageBase64 = prefs.getString("profile_image", null);
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgProfile.setImageBitmap(bitmap);
            } else {
                imgProfile.setImageResource(R.drawable.ic_profile);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image: " + e.getMessage(), e);
            imgProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    private void setupClickListeners() {
        imgProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Add Expense → AddTransactionActivity (expense only)
        cardAddExpense.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class)));

        // Add Income → Beautiful Bottom Sheet
        cardAddIncome.setOnClickListener(v -> showAddIncomeBottomSheet());

        // Budget → BudgetActivity
        cardBudget.setOnClickListener(v ->
                startActivity(new Intent(this, BudgetActivity.class)));

        // Reports → ReportsActivity
        cardReports.setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));

        btnClearAll.setOnClickListener(v -> showClearAllDialog());
    }

    private void showAddIncomeBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_add_income);

        // Get references
        EditText etAmount = bottomSheetDialog.findViewById(R.id.etIncomeAmount);
        TextInputEditText etSource = bottomSheetDialog.findViewById(R.id.etIncomeSource);
        AutoCompleteTextView actvCategory = bottomSheetDialog.findViewById(R.id.actvIncomeCategory);
        MaterialButton btnAdd = bottomSheetDialog.findViewById(R.id.btnAddIncome);
        MaterialButton btnCancel = bottomSheetDialog.findViewById(R.id.btnCancelIncome);

        // Setup income category dropdown
        String[] incomeCategories = {
                "Salary", "Freelance", "Business", "Investment",
                "Gift", "Rental", "Bonus", "Other"
        };
        if (actvCategory != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, incomeCategories);
            actvCategory.setAdapter(adapter);
        }

        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                String amountStr = etAmount != null ? etAmount.getText().toString().trim() : "";
                String source = etSource != null ? etSource.getText().toString().trim() : "";
                String category = actvCategory != null ? actvCategory.getText().toString().trim() : "Income";

                if (amountStr.isEmpty()) {
                    if (etAmount != null) etAmount.setError("Enter an amount");
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        if (etAmount != null) etAmount.setError("Amount must be greater than 0");
                        return;
                    }

                    if (source.isEmpty()) source = "Income";
                    if (category.isEmpty()) category = "Income";

                    boolean success = saveIncomeTransaction(amount, source, category);

                    if (success) {
                        Toast.makeText(this, "✓ Income added successfully!", Toast.LENGTH_SHORT).show();
                        loadBalance();
                        LowBalanceChecker.check(this);
                        bottomSheetDialog.dismiss();
                    } else {
                        Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT).show();
                    }

                } catch (NumberFormatException e) {
                    if (etAmount != null) etAmount.setError("Invalid amount");
                }
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private boolean saveIncomeTransaction(double amount, String description, String category) {
        try {
            long result = databaseHelper.addTransaction(
                    description,
                    category,
                    amount,
                    "",
                    "INCOME",
                    System.currentTimeMillis()
            );
            Log.d(TAG, "Income transaction saved with ID: " + result);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error saving income: " + e.getMessage(), e);
            return false;
        }
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

            tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", totalBalance));
            tvIncome.setText(String.format(Locale.getDefault(), "₹%.2f", income));
            tvExpense.setText(String.format(Locale.getDefault(), "₹%.2f", expense));

        } catch (Exception e) {
            Log.e(TAG, "Error loading balance: " + e.getMessage(), e);
            tvTotalBalance.setText("₹0.00");
            tvIncome.setText("₹0.00");
            tvExpense.setText("₹0.00");
        }
    }

    private void showClearAllDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("This will delete:\n\n• All transactions (Income & Expenses)\n• Monthly salary\n• All financial records\n\nThis action cannot be undone!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Clear All", (dialog, which) ->
                        new AlertDialog.Builder(this)
                                .setTitle("Are you absolutely sure?")
                                .setMessage("This will permanently delete all your financial data!")
                                .setPositiveButton("Yes, Delete Everything", (d, w) -> clearAllData())
                                .setNegativeButton("Cancel", null)
                                .show())
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
            Toast.makeText(this, "Error clearing data", Toast.LENGTH_SHORT).show();
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