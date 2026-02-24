package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirmPassword, etPhone, etOtp;
    Button btnGenerateOtp, btnSubmit;
    TextView tvResendOtp;

    private String generatedOtp = "";
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager   = new SessionManager(this);
        databaseHelper   = new DatabaseHelper(this);

        // Already logged in → skip straight to Dashboard
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_register);

        etName            = findViewById(R.id.etName);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone           = findViewById(R.id.etPhone);
        etOtp             = findViewById(R.id.etOtp);
        btnGenerateOtp    = findViewById(R.id.btnGenerateOtp);
        btnSubmit         = findViewById(R.id.btnSubmit);
        tvResendOtp       = findViewById(R.id.tvResendOtp);

        btnGenerateOtp.setOnClickListener(v -> generateOtp());
        tvResendOtp.setOnClickListener(v -> generateOtp());
        btnSubmit.setOnClickListener(v -> validateAndRegister());

        // Link to LoginActivity
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    private void generateOtp() {
        String phone = etPhone.getText().toString().trim();
        if (phone.length() != 10) {
            etPhone.setError("Enter 10 digit phone number");
            return;
        }
        generatedOtp = String.valueOf(100000 + new Random().nextInt(900000));
        Toast.makeText(this, "OTP: " + generatedOtp, Toast.LENGTH_SHORT).show();
        etOtp.setEnabled(true);
        tvResendOtp.setEnabled(true);
    }

    private void validateAndRegister() {
        String name  = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();
        String cpass = etConfirmPassword.getText().toString();
        String phone = etPhone.getText().toString().trim();
        String otp   = etOtp.getText().toString().trim();

        if (name.isEmpty())  { etName.setError("Name is required");  etName.requestFocus();  return; }
        if (email.isEmpty()) { etEmail.setError("Email is required"); etEmail.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return;
        }
        if (pass.isEmpty())         { etPassword.setError("Password is required"); etPassword.requestFocus(); return; }
        if (pass.length() < 6)      { etPassword.setError("Minimum 6 characters"); etPassword.requestFocus(); return; }
        if (!pass.equals(cpass))    { etConfirmPassword.setError("Passwords do not match"); etConfirmPassword.requestFocus(); return; }
        if (phone.length() != 10)   { etPhone.setError("Phone must be 10 digits"); etPhone.requestFocus(); return; }
        if (otp.isEmpty())          { etOtp.setError("OTP is required"); etOtp.requestFocus(); return; }
        if (!otp.equals(generatedOtp)) { etOtp.setError("Invalid OTP"); etOtp.requestFocus(); return; }

        // Check duplicate email
        if (databaseHelper.isEmailTaken(email)) {
            etEmail.setError("This email is already registered");
            etEmail.requestFocus();
            return;
        }

        // Insert user into DB
        long userId = databaseHelper.registerUser(name, email, pass);
        if (userId == -1) {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save session
        sessionManager.saveSession((int) userId, name, email);

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}