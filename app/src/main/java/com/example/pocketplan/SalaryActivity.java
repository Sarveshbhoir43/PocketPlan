package com.example.pocketplan;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SalaryActivity extends AppCompatActivity {

    private EditText etSalary;
    private MaterialButton btnSave;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);

        etSalary = findViewById(R.id.etSalary);
        btnSave = findViewById(R.id.btnSaveSalary);

        databaseHelper = new DatabaseHelper(this);

        // ✅ Show existing salary (only if > 0)
        double existingSalary = databaseHelper.getSalary();
        if (existingSalary > 0) {
            etSalary.setText(String.valueOf(existingSalary));
        } else {
            etSalary.setText("");
        }

        btnSave.setOnClickListener(v -> {

            String salaryStr = etSalary.getText().toString().trim();

            // ✅ Empty check
            if (salaryStr.isEmpty()) {
                Toast.makeText(this, "Please enter your salary", Toast.LENGTH_SHORT).show();
                return;
            }

            double salary;

            try {
                salary = Double.parseDouble(salaryStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Prevent zero / negative salary
            if (salary <= 0) {
                Toast.makeText(this, "Salary must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseHelper.saveSalary(salary);

            Toast.makeText(this, "Salary saved successfully", Toast.LENGTH_SHORT).show();
            finish(); // return to Dashboard
        });
    }

    // 🔒 Prevent exiting without salary
    @Override
    public void onBackPressed() {
        if (databaseHelper.getSalary() <= 0) {
            Toast.makeText(this, "Please save salary to continue", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}
