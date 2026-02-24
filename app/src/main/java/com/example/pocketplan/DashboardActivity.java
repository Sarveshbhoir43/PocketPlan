package com.example.pocketplan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private TextView tvGreeting, tvUserName, tvTotalBalance, tvIncome, tvExpense;
    private ImageView imgProfile;
    private MaterialCardView cardAddExpense, cardAddIncome, cardBudget, cardReports;
    private MaterialButton btnClearAll;
    private BottomNavigationView bottomNavigation;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Guard: must be logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = sessionManager.getUserId();
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);

        NotificationHelper.createChannels(this);
        WeeklyScheduler.schedule(this);
        requestNotificationPermissionIfNeeded();

        initializeViews();
        setupGreeting();
        tvUserName.setText(sessionManager.getUserName());
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
        tvGreeting      = findViewById(R.id.tvGreeting);
        tvUserName      = findViewById(R.id.tvUserName);
        tvTotalBalance  = findViewById(R.id.tvTotalBalance);
        tvIncome        = findViewById(R.id.tvIncome);
        tvExpense       = findViewById(R.id.tvExpense);
        imgProfile      = findViewById(R.id.imgProfile);
        cardAddExpense  = findViewById(R.id.cardAddExpense);
        cardAddIncome   = findViewById(R.id.cardAddIncome);
        cardBudget      = findViewById(R.id.cardBudget);
        cardReports     = findViewById(R.id.cardReports);
        btnClearAll     = findViewById(R.id.btnClearAll);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good Morning," : hour < 17 ? "Good Afternoon," : "Good Evening,";
        tvGreeting.setText(greeting);
    }

    private void loadProfileImage() {
        try {
            // Profile images are stored per-user using userId as the key
            String key = "profile_image_" + currentUserId;
            android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String imageBase64 = prefs.getString(key, null);
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                byte[] bytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imgProfile.setImageBitmap(bitmap);
            } else {
                imgProfile.setImageResource(R.drawable.ic_profile);
            }
        } catch (Exception e) {
            imgProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    private void setupClickListeners() {
        imgProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        cardAddExpense.setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));
        cardAddIncome.setOnClickListener(v -> showAddIncomeBottomSheet());
        cardBudget.setOnClickListener(v -> startActivity(new Intent(this, BudgetActivity.class)));
        cardReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        btnClearAll.setOnClickListener(v -> showClearAllDialog());
    }

    private void showAddIncomeBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(R.layout.dialog_add_income);

        EditText etAmount = dialog.findViewById(R.id.etIncomeAmount);
        TextInputEditText etSource = dialog.findViewById(R.id.etIncomeSource);
        AutoCompleteTextView actvCategory = dialog.findViewById(R.id.actvIncomeCategory);
        MaterialButton btnAdd = dialog.findViewById(R.id.btnAddIncome);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelIncome);

        String[] incomeCategories = {"Salary","Freelance","Business","Investment","Gift","Rental","Bonus","Other"};
        if (actvCategory != null) {
            actvCategory.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, incomeCategories));
        }

        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                String amountStr = etAmount != null ? etAmount.getText().toString().trim() : "";
                String source    = etSource != null ? etSource.getText().toString().trim() : "";
                String category  = actvCategory != null ? actvCategory.getText().toString().trim() : "Income";

                if (amountStr.isEmpty()) { if (etAmount != null) etAmount.setError("Enter an amount"); return; }
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) { if (etAmount != null) etAmount.setError("Amount must be > 0"); return; }
                    if (source.isEmpty())   source   = "Income";
                    if (category.isEmpty()) category = "Income";

                    long result = databaseHelper.addTransaction(
                            source, category, amount, "", "INCOME",
                            System.currentTimeMillis(), currentUserId);

                    if (result != -1) {
                        Toast.makeText(this, "✓ Income added!", Toast.LENGTH_SHORT).show();
                        loadBalance();
                        LowBalanceChecker.check(this);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    if (etAmount != null) etAmount.setError("Invalid amount");
                }
            });
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) return true;
            if (item.getItemId() == R.id.nav_transactions) {
                startActivity(new Intent(this, TransactionsActivity.class)); return true;
            }
            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class)); return true;
            }
            return false;
        });
    }

    private void loadBalance() {
        try {
            double salary  = databaseHelper.getSalary(currentUserId);
            double income  = databaseHelper.getTotalIncome(currentUserId);
            double expense = databaseHelper.getTotalExpense(currentUserId);
            double balance = salary + income - expense;

            tvTotalBalance.setText(String.format(Locale.getDefault(), "₹%.2f", balance));
            tvIncome.setText(String.format(Locale.getDefault(), "₹%.2f", income));
            tvExpense.setText(String.format(Locale.getDefault(), "₹%.2f", expense));
        } catch (Exception e) {
            Log.e(TAG, "loadBalance: " + e.getMessage());
            tvTotalBalance.setText("₹0.00");
            tvIncome.setText("₹0.00");
            tvExpense.setText("₹0.00");
        }
    }

    private void showClearAllDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("This will delete all your transactions and salary. This cannot be undone!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Clear All", (d, w) ->
                        new AlertDialog.Builder(this)
                                .setTitle("Are you absolutely sure?")
                                .setMessage("This will permanently delete all your financial data!")
                                .setPositiveButton("Yes, Delete Everything", (d2, w2) -> clearAllData())
                                .setNegativeButton("Cancel", null).show())
                .setNegativeButton("Cancel", null).show();
    }

    private void clearAllData() {
        boolean t = databaseHelper.clearAllTransactions(currentUserId);
        boolean s = databaseHelper.setSalary(0, currentUserId);
        if (t && s) {
            Toast.makeText(this, "✓ All data cleared", Toast.LENGTH_LONG).show();
            loadBalance();
        } else {
            Toast.makeText(this, "Failed to clear some data", Toast.LENGTH_SHORT).show();
        }
    }

    /** Request POST_NOTIFICATIONS permission on Android 13+ (first launch only). */
    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigation.getSelectedItemId() != R.id.nav_dashboard)
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        else
            super.onBackPressed();
    }
}