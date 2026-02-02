package com.example.pocketplan;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
import db.DBHelper;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirmPassword, etPhone, etOtp;
    Button btnGenerateOtp, btnSubmit;
    TextView tvResendOtp;

    String generatedOtp = "";
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DBHelper(this); // initialize database

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);

        btnGenerateOtp = findViewById(R.id.btnGenerateOtp);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        // Generate OTP
        btnGenerateOtp.setOnClickListener(v -> generateOtp());
        tvResendOtp.setOnClickListener(v -> generateOtp());

        // Submit registration
        btnSubmit.setOnClickListener(v -> validateForm());
    }

    private void generateOtp() {
        String phone = etPhone.getText().toString().trim();

        if (phone.length() != 10) {
            etPhone.setError("Enter 10 digit phone number");
            etPhone.requestFocus();
            return;
        }

        generatedOtp = String.valueOf(100000 + new Random().nextInt(900000));
        Toast.makeText(this, "OTP: " + generatedOtp, Toast.LENGTH_SHORT).show();

        etOtp.setEnabled(true);
        tvResendOtp.setEnabled(true);
    }

    private void validateForm() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String cpass = etConfirmPassword.getText().toString();
        String phone = etPhone.getText().toString();
        String otp = etOtp.getText().toString();

        // ===== Validation =====
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (!pass.equals(cpass)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (phone.length() != 10) {
            etPhone.setError("Phone number must be 10 digits");
            etPhone.requestFocus();
            return;
        }

        if (otp.isEmpty()) {
            etOtp.setError("OTP is required");
            etOtp.requestFocus();
            return;
        }

        if (otp.length() != 6) {
            etOtp.setError("OTP must be 6 digits");
            etOtp.requestFocus();
            return;
        }

        if (!otp.equals(generatedOtp)) {
            etOtp.setError("Invalid OTP");
            etOtp.requestFocus();
            return;
        }

        // ===== Save user to DB =====
        boolean registered = dbHelper.registerUser(name, email, pass, phone);
        if (!registered) {
            Toast.makeText(this, "User already exists with this email or phone", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show();

        // ===== Clear form for new registration =====
        clearForm();
    }

    private void clearForm() {
        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        etPhone.setText("");
        etOtp.setText("");
        generatedOtp = "";
    }
}
