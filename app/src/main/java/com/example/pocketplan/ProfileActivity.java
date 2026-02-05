package com.example.pocketplan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    // UI Components - Header
    private ImageView ivProfileImage;
    private MaterialButton btnEditPhoto;

    // UI Components - User Info Display
    private TextView tvUserName;
    private TextView tvEmail;
    private TextView tvPhone;

    // UI Components - Editable Fields
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private MaterialButton btnSaveChanges;

    // UI Components - Security
    private View btnChangePassword;
    private View btnBiometric;
    private SwitchMaterial switchBiometric;

    // UI Components - Preferences
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchDarkMode;
    private View btnCurrency;
    private TextView tvSelectedCurrency;

    // UI Components - Logout
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setupClickListeners();
        loadUserData(); // load from SharedPreferences
    }

    private void initializeViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);

        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBiometric = findViewById(R.id.btnBiometric);
        switchBiometric = findViewById(R.id.switchBiometric);

        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnCurrency = findViewById(R.id.btnCurrency);
        tvSelectedCurrency = findViewById(R.id.tvSelectedCurrency);

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnEditPhoto.setOnClickListener(v -> Toast.makeText(this, "Opening photo picker...", Toast.LENGTH_SHORT).show());
        btnSaveChanges.setOnClickListener(v -> saveUserChanges());
        btnChangePassword.setOnClickListener(v -> Toast.makeText(this, "Opening Change Password...", Toast.LENGTH_SHORT).show());
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "Biometric login enabled" : "Biometric login disabled", Toast.LENGTH_SHORT).show();
        });
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled", Toast.LENGTH_SHORT).show();
        });
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "Dark mode enabled" : "Dark mode disabled", Toast.LENGTH_SHORT).show();
        });
        btnCurrency.setOnClickListener(v -> showCurrencyDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = prefs.getString("name", "John Doe");
        String email = prefs.getString("email", "john.doe@example.com");
        String phone = prefs.getString("phone", "+91 98765 43210");

        tvUserName.setText(name);
        tvEmail.setText(email);
        tvPhone.setText(phone);

        etFullName.setText(name);
        etEmail.setText(email);
        etPhone.setText(phone);
    }

    private void saveUserChanges() {
        String newName = etFullName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newName.isEmpty()) { etFullName.setError("Name cannot be empty"); etFullName.requestFocus(); return; }
        if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) { etEmail.setError("Enter valid email"); etEmail.requestFocus(); return; }
        if (newPhone.isEmpty()) { etPhone.setError("Phone cannot be empty"); etPhone.requestFocus(); return; }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", newName);
        editor.putString("email", newEmail);
        editor.putString("phone", newPhone);
        editor.apply();

        tvUserName.setText(newName);
        tvEmail.setText(newEmail);
        tvPhone.setText(newPhone);

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void showCurrencyDialog() {
        final String[] currencies = {"INR (₹)", "USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Currency");
        builder.setItems(currencies, (dialog, which) -> {
            tvSelectedCurrency.setText(currencies[which]);
            Toast.makeText(this, "Currency changed to " + currencies[which], Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
