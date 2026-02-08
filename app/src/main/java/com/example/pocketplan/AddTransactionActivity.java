package com.example.pocketplan;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText etTitle, etCategory, etAmount, etNote;
    private RadioButton rbIncome, rbExpense;
    private MaterialButton btnSave;

    DatabaseHelper databaseHelper;   // ✅ DATABASE HELPER

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // UI binding (UNCHANGED)
        etTitle = findViewById(R.id.etTitle);
        etCategory = findViewById(R.id.etCategory);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        rbIncome = findViewById(R.id.rbIncome);
        rbExpense = findViewById(R.id.rbExpense);
        btnSave = findViewById(R.id.btnSave);

        // Close popup
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Save button
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {

        String title = etTitle.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        String type = rbIncome.isChecked() ? "INCOME" : "EXPENSE";

        // Validation (UNCHANGED LOGIC)
        if (title.isEmpty() || category.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert amount
        double amount = Double.parseDouble(amountStr);

        // ✅ INSERT INTO SQLITE DATABASE
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("category", category);
        values.put("amount", amount);
        values.put("note", note);
        values.put("type", type);

        long result = db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);

        if (result != -1) {
            Toast.makeText(this, "Transaction Saved", Toast.LENGTH_SHORT).show();

            // ✅ SEND DATA BACK TO TransactionsActivity
            Intent intent = new Intent();
            intent.putExtra("id", result);  // ID of the inserted row
            intent.putExtra("title", title);
            intent.putExtra("category", category);
            intent.putExtra("amount", amount);
            intent.putExtra("note", note);
            intent.putExtra("type", type);

            setResult(RESULT_OK, intent);
            finish(); // close popup
        } else {
            Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
        }
    }
}
