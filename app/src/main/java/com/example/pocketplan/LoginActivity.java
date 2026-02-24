package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        // Already logged in → go to Dashboard
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin   = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());

        // Navigate to Register
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        if (tvGoToRegister != null) {
            tvGoToRegister.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
            });
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();

        if (email.isEmpty()) { etEmail.setError("Email is required"); etEmail.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return;
        }
        if (pass.isEmpty()) { etPassword.setError("Password is required"); etPassword.requestFocus(); return; }

        // Validate against DB
        int userId = databaseHelper.loginUser(email, pass);
        if (userId == -1) {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve user name then save session
        String name = databaseHelper.getUserName(userId);
        sessionManager.saveSession(userId, name, email);

        Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}