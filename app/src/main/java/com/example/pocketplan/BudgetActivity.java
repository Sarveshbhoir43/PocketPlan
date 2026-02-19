package com.example.pocketplan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class BudgetActivity extends AppCompatActivity {

    private static final String TAG = "BudgetActivity";
    private static final String PREFS_BUDGET = "BudgetPrefs";

    private DatabaseHelper databaseHelper;
    private SharedPreferences budgetPrefs;

    // Summary TextViews
    private TextView tvTotalBudget, tvTotalSpent, tvTotalRemaining;

    // Budget input fields
    private TextInputEditText etBudgetFood, etBudgetTransport, etBudgetShopping,
            etBudgetBills, etBudgetEntertainment, etBudgetOther;

    // Spent labels
    private TextView tvSpentFood, tvSpentTransport, tvSpentShopping,
            tvSpentBills, tvSpentEntertainment, tvSpentOther;

    // Progress bars
    private ProgressBar progressFood, progressTransport, progressShopping,
            progressBills, progressEntertainment, progressOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        databaseHelper = new DatabaseHelper(this);
        budgetPrefs = getSharedPreferences(PREFS_BUDGET, MODE_PRIVATE);

        setupToolbar();
        initializeViews();
        loadSavedBudgets();
        loadSpentAmounts();
        updateSummary();

        findViewById(R.id.btnSaveBudgets).setOnClickListener(v -> saveBudgets());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        tvTotalBudget    = findViewById(R.id.tvTotalBudget);
        tvTotalSpent     = findViewById(R.id.tvTotalSpent);
        tvTotalRemaining = findViewById(R.id.tvTotalRemaining);

        etBudgetFood          = findViewById(R.id.etBudgetFood);
        etBudgetTransport     = findViewById(R.id.etBudgetTransport);
        etBudgetShopping      = findViewById(R.id.etBudgetShopping);
        etBudgetBills         = findViewById(R.id.etBudgetBills);
        etBudgetEntertainment = findViewById(R.id.etBudgetEntertainment);
        etBudgetOther         = findViewById(R.id.etBudgetOther);

        tvSpentFood          = findViewById(R.id.tvSpentFood);
        tvSpentTransport     = findViewById(R.id.tvSpentTransport);
        tvSpentShopping      = findViewById(R.id.tvSpentShopping);
        tvSpentBills         = findViewById(R.id.tvSpentBills);
        tvSpentEntertainment = findViewById(R.id.tvSpentEntertainment);
        tvSpentOther         = findViewById(R.id.tvSpentOther);

        progressFood          = findViewById(R.id.progressFood);
        progressTransport     = findViewById(R.id.progressTransport);
        progressShopping      = findViewById(R.id.progressShopping);
        progressBills         = findViewById(R.id.progressBills);
        progressEntertainment = findViewById(R.id.progressEntertainment);
        progressOther         = findViewById(R.id.progressOther);
    }

    private void loadSavedBudgets() {
        setFieldText(etBudgetFood,          budgetPrefs.getFloat("budget_food", 0));
        setFieldText(etBudgetTransport,     budgetPrefs.getFloat("budget_transport", 0));
        setFieldText(etBudgetShopping,      budgetPrefs.getFloat("budget_shopping", 0));
        setFieldText(etBudgetBills,         budgetPrefs.getFloat("budget_bills", 0));
        setFieldText(etBudgetEntertainment, budgetPrefs.getFloat("budget_entertainment", 0));
        setFieldText(etBudgetOther,         budgetPrefs.getFloat("budget_other", 0));
    }

    private void setFieldText(TextInputEditText field, float value) {
        if (value > 0) {
            field.setText(String.format(Locale.getDefault(), "%.0f", value));
        }
    }

    private void loadSpentAmounts() {
        try {
            double food          = databaseHelper.getExpenseByCategory("Food & Dining");
            double transport     = databaseHelper.getExpenseByCategory("Transportation");
            double shopping      = databaseHelper.getExpenseByCategory("Shopping");
            double bills         = databaseHelper.getExpenseByCategory("Bills & Utilities");
            double entertainment = databaseHelper.getExpenseByCategory("Entertainment");
            double other         = databaseHelper.getExpenseByCategory("Other");

            updateCategoryUI(tvSpentFood,          progressFood,          food,
                    budgetPrefs.getFloat("budget_food", 0));
            updateCategoryUI(tvSpentTransport,     progressTransport,     transport,
                    budgetPrefs.getFloat("budget_transport", 0));
            updateCategoryUI(tvSpentShopping,      progressShopping,      shopping,
                    budgetPrefs.getFloat("budget_shopping", 0));
            updateCategoryUI(tvSpentBills,         progressBills,         bills,
                    budgetPrefs.getFloat("budget_bills", 0));
            updateCategoryUI(tvSpentEntertainment, progressEntertainment, entertainment,
                    budgetPrefs.getFloat("budget_entertainment", 0));
            updateCategoryUI(tvSpentOther,         progressOther,         other,
                    budgetPrefs.getFloat("budget_other", 0));

        } catch (Exception e) {
            Log.e(TAG, "Error loading spent amounts: " + e.getMessage(), e);
        }
    }

    private void updateCategoryUI(TextView tvSpent, ProgressBar progress,
                                   double spent, float budget) {
        tvSpent.setText(String.format(Locale.getDefault(), "Spent: ₹%.0f", spent));

        if (budget > 0) {
            int percent = (int) Math.min((spent / budget) * 100, 100);
            progress.setProgress(percent);
        } else {
            progress.setProgress(0);
        }
    }

    private void updateSummary() {
        float totalBudget =
                budgetPrefs.getFloat("budget_food", 0) +
                budgetPrefs.getFloat("budget_transport", 0) +
                budgetPrefs.getFloat("budget_shopping", 0) +
                budgetPrefs.getFloat("budget_bills", 0) +
                budgetPrefs.getFloat("budget_entertainment", 0) +
                budgetPrefs.getFloat("budget_other", 0);

        double totalSpent = 0;
        try {
            totalSpent =
                    databaseHelper.getExpenseByCategory("Food & Dining") +
                    databaseHelper.getExpenseByCategory("Transportation") +
                    databaseHelper.getExpenseByCategory("Shopping") +
                    databaseHelper.getExpenseByCategory("Bills & Utilities") +
                    databaseHelper.getExpenseByCategory("Entertainment") +
                    databaseHelper.getExpenseByCategory("Other");
        } catch (Exception e) {
            Log.e(TAG, "Error computing total spent: " + e.getMessage(), e);
        }

        double remaining = totalBudget - totalSpent;

        tvTotalBudget.setText(String.format(Locale.getDefault(), "₹%.0f", (double) totalBudget));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "₹%.0f", totalSpent));
        tvTotalRemaining.setText(String.format(Locale.getDefault(), "₹%.0f", remaining));
    }

    private void saveBudgets() {
        SharedPreferences.Editor editor = budgetPrefs.edit();

        editor.putFloat("budget_food",          parseField(etBudgetFood));
        editor.putFloat("budget_transport",     parseField(etBudgetTransport));
        editor.putFloat("budget_shopping",      parseField(etBudgetShopping));
        editor.putFloat("budget_bills",         parseField(etBudgetBills));
        editor.putFloat("budget_entertainment", parseField(etBudgetEntertainment));
        editor.putFloat("budget_other",         parseField(etBudgetOther));

        editor.apply();

        Toast.makeText(this, "✓ Budgets saved successfully!", Toast.LENGTH_SHORT).show();

        // Refresh UI after save
        loadSpentAmounts();
        updateSummary();
    }

    private float parseField(TextInputEditText field) {
        try {
            String text = field.getText().toString().trim();
            return text.isEmpty() ? 0f : Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}
