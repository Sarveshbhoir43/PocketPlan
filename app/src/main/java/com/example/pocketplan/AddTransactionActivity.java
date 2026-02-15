package com.example.pocketplan;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String TAG = "AddTransaction";

    private TextInputEditText etTitle, etAmount, etNote;
    private AutoCompleteTextView actvCategory;
    private TextInputLayout tilCategory;
    private MaterialButtonToggleGroup toggleType;
    private MaterialButton btnSave, btnExpense, btnIncome;

    DatabaseHelper databaseHelper;

    // Category icons mapping
    private Map<String, Integer> categoryIcons;
    private String[] categories = {
            "Food & Dining",
            "Transportation",
            "Shopping",
            "Entertainment",
            "Bills & Utilities",
            "Healthcare",
            "Education",
            "Travel",
            "Groceries",
            "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Initialize views
        initializeViews();

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Setup category icons
        setupCategoryIcons();

        // Setup category dropdown
        setupCategoryDropdown();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        actvCategory = findViewById(R.id.actvCategory);
        tilCategory = findViewById(R.id.tilCategory);
        toggleType = findViewById(R.id.toggleType);
        btnSave = findViewById(R.id.btnSave);
        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);
    }

    private void setupCategoryIcons() {
        categoryIcons = new HashMap<>();
        categoryIcons.put("Food & Dining", android.R.drawable.ic_menu_recent_history);
        categoryIcons.put("Transportation", android.R.drawable.ic_menu_directions);
        categoryIcons.put("Shopping", android.R.drawable.ic_menu_gallery);
        categoryIcons.put("Entertainment", android.R.drawable.ic_media_play);
        categoryIcons.put("Bills & Utilities", android.R.drawable.ic_menu_agenda);
        categoryIcons.put("Healthcare", android.R.drawable.ic_menu_add);
        categoryIcons.put("Education", android.R.drawable.ic_menu_info_details);
        categoryIcons.put("Travel", android.R.drawable.ic_menu_compass);
        categoryIcons.put("Groceries", android.R.drawable.ic_menu_today);
        categoryIcons.put("Other", android.R.drawable.ic_dialog_info);
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        actvCategory.setAdapter(adapter);

        // Update icon when category is selected
        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = categories[position];
            updateCategoryIcon(selectedCategory);
        });
    }

    private void updateCategoryIcon(String category) {
        Integer iconRes = categoryIcons.get(category);
        if (iconRes != null) {
            tilCategory.setStartIconDrawable(iconRes);

            // Change icon color based on category
            int iconColor;
            switch (category) {
                case "Food & Dining":
                    iconColor = 0xFFFF9800; // Orange
                    break;
                case "Transportation":
                    iconColor = 0xFF2196F3; // Blue
                    break;
                case "Shopping":
                    iconColor = 0xFFE91E63; // Pink
                    break;
                case "Entertainment":
                    iconColor = 0xFF9C27B0; // Purple
                    break;
                case "Bills & Utilities":
                    iconColor = 0xFFF44336; // Red
                    break;
                case "Healthcare":
                    iconColor = 0xFF4CAF50; // Green
                    break;
                case "Education":
                    iconColor = 0xFF3F51B5; // Indigo
                    break;
                case "Travel":
                    iconColor = 0xFF00BCD4; // Cyan
                    break;
                case "Groceries":
                    iconColor = 0xFF8BC34A; // Light Green
                    break;
                default:
                    iconColor = 0xFF607D8B; // Blue Grey
                    break;
            }
            tilCategory.setStartIconTintList(android.content.res.ColorStateList.valueOf(iconColor));
        }
    }

    private void setupListeners() {
        // Close button
        findViewById(R.id.btnClose).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Save button
        btnSave.setOnClickListener(v -> saveTransaction());

        // Type toggle listener (optional - for visual feedback)
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnExpense) {
                    btnSave.setBackgroundColor(0xFFF44336); // Red for expense
                } else {
                    btnSave.setBackgroundColor(0xFF4CAF50); // Green for income
                }
            }
        });
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        // Determine type from toggle group
        String type = (toggleType.getCheckedButtonId() == R.id.btnIncome)
                ? "INCOME"
                : "EXPENSE";

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (category.isEmpty()) {
            tilCategory.setError("Category is required");
            actvCategory.requestFocus();
            return;
        }

        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
            return;
        }

        // Insert into SQLite database
        SQLiteDatabase db = null;
        long result = -1;

        try {
            db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_TITLE, title);
            values.put(DatabaseHelper.COL_CATEGORY, category);
            values.put(DatabaseHelper.COL_AMOUNT, amount);
            values.put(DatabaseHelper.COL_NOTE, note);
            values.put(DatabaseHelper.COL_TYPE, type);
            values.put(DatabaseHelper.COL_TIMESTAMP, System.currentTimeMillis()); // ✅ CRITICAL FIX

            result = db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);

            if (result != -1) {
                Log.d(TAG, "Transaction saved successfully with ID: " + result);
                Toast.makeText(this, "✓ Transaction Saved Successfully", Toast.LENGTH_SHORT).show();

                // Send result back
                Intent intent = new Intent();
                intent.putExtra("success", true);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Log.e(TAG, "Failed to insert transaction");
                Toast.makeText(this, "❌ Failed to save transaction", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving transaction: " + e.getMessage(), e);
            Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}