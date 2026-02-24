package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.pocketplan.notifications.BudgetNotificationChecker;
import com.example.pocketplan.notifications.LowBalanceChecker;

import java.util.HashMap;
import java.util.Map;

import utils.SessionManager;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String TAG = "AddTransaction";

    private TextInputEditText etTitle, etAmount, etNote;
    private AutoCompleteTextView actvCategory;
    private TextInputLayout tilCategory;
    private MaterialButton btnSave;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private int currentUserId;

    private Map<String, Integer> categoryIcons;
    private String[] categories = {
            "Food & Dining","Transportation","Shopping","Entertainment",
            "Bills & Utilities","Healthcare","Education","Travel","Groceries","Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = sessionManager.getUserId();

        setContentView(R.layout.activity_add_transaction);
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupCategoryIcons();
        setupCategoryDropdown();
        setupListeners();
    }

    private void initializeViews() {
        etTitle      = findViewById(R.id.etTitle);
        etAmount     = findViewById(R.id.etAmount);
        etNote       = findViewById(R.id.etNote);
        actvCategory = findViewById(R.id.actvCategory);
        tilCategory  = findViewById(R.id.tilCategory);
        btnSave      = findViewById(R.id.btnSave);
    }

    private void setupCategoryIcons() {
        categoryIcons = new HashMap<>();
        categoryIcons.put("Food & Dining",     android.R.drawable.ic_menu_recent_history);
        categoryIcons.put("Transportation",    android.R.drawable.ic_menu_directions);
        categoryIcons.put("Shopping",          android.R.drawable.ic_menu_gallery);
        categoryIcons.put("Entertainment",     android.R.drawable.ic_media_play);
        categoryIcons.put("Bills & Utilities", android.R.drawable.ic_menu_agenda);
        categoryIcons.put("Healthcare",        android.R.drawable.ic_menu_add);
        categoryIcons.put("Education",         android.R.drawable.ic_menu_info_details);
        categoryIcons.put("Travel",            android.R.drawable.ic_menu_compass);
        categoryIcons.put("Groceries",         android.R.drawable.ic_menu_today);
        categoryIcons.put("Other",             android.R.drawable.ic_dialog_info);
    }

    private void setupCategoryDropdown() {
        actvCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories));
        actvCategory.setOnItemClickListener((parent, view, position, id) ->
                updateCategoryIcon(categories[position]));
    }

    private void updateCategoryIcon(String category) {
        Integer iconRes = categoryIcons.get(category);
        if (iconRes != null) {
            tilCategory.setStartIconDrawable(iconRes);
            int color;
            switch (category) {
                case "Food & Dining":     color = 0xFFFF9800; break;
                case "Transportation":   color = 0xFF2196F3; break;
                case "Shopping":         color = 0xFFE91E63; break;
                case "Entertainment":    color = 0xFF9C27B0; break;
                case "Bills & Utilities":color = 0xFFF44336; break;
                case "Healthcare":       color = 0xFF4CAF50; break;
                case "Education":        color = 0xFF3F51B5; break;
                case "Travel":           color = 0xFF00BCD4; break;
                case "Groceries":        color = 0xFF8BC34A; break;
                default:                 color = 0xFF607D8B; break;
            }
            tilCategory.setStartIconTintList(android.content.res.ColorStateList.valueOf(color));
        }
    }

    private void setupListeners() {
        findViewById(R.id.btnClose).setOnClickListener(v -> { setResult(RESULT_CANCELED); finish(); });
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String title    = etTitle.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String note     = etNote.getText().toString().trim();
        String amtStr   = etAmount.getText().toString().trim();

        if (title.isEmpty())    { etTitle.setError("Title is required");    etTitle.requestFocus();    return; }
        if (category.isEmpty()) { tilCategory.setError("Category required"); actvCategory.requestFocus(); return; }
        if (amtStr.isEmpty())   { etAmount.setError("Amount is required");  etAmount.requestFocus();   return; }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) { etAmount.setError("Amount must be > 0"); etAmount.requestFocus(); return; }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount"); etAmount.requestFocus(); return;
        }

        long result = databaseHelper.addTransaction(
                title, category, amount, note, "EXPENSE",
                System.currentTimeMillis(), currentUserId);

        if (result != -1) {
            Log.d(TAG, "Expense saved with ID: " + result + " for user " + currentUserId);
            Toast.makeText(this, "✓ Expense Saved", Toast.LENGTH_SHORT).show();
            BudgetNotificationChecker.checkAllCategories(this);
            LowBalanceChecker.check(this);
            Intent intent = new Intent();
            intent.putExtra("success", true);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, "❌ Failed to save expense", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) databaseHelper.close();
    }
}