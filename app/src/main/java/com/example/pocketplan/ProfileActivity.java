package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        // Initialize views
        initializeViews();
        
        // Set up click listeners
        setupClickListeners();
        
        // Load user data
        loadUserData();
    }

    private void initializeViews() {
        // Header views
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        
        // User info display
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        
        // Editable fields
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        
        // Security
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBiometric = findViewById(R.id.btnBiometric);
        switchBiometric = findViewById(R.id.switchBiometric);
        
        // Preferences
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnCurrency = findViewById(R.id.btnCurrency);
        tvSelectedCurrency = findViewById(R.id.tvSelectedCurrency);
        
        // Logout
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        // Edit Photo
        btnEditPhoto.setOnClickListener(v -> {
            Toast.makeText(this, "Opening photo picker...", Toast.LENGTH_SHORT).show();
            // Implement photo picker logic here
            // Intent intent = new Intent(Intent.ACTION_PICK);
            // intent.setType("image/*");
            // startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        // Save Changes
        btnSaveChanges.setOnClickListener(v -> {
            saveUserChanges();
        });

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Change Password...", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            // startActivity(intent);
        });

        // Biometric Switch
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Biometric login enabled", Toast.LENGTH_SHORT).show();
                // Enable biometric authentication
            } else {
                Toast.makeText(this, "Biometric login disabled", Toast.LENGTH_SHORT).show();
                // Disable biometric authentication
            }
        });

        // Notifications Switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
            // Save preference
        });

        // Dark Mode Switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Dark mode enabled", Toast.LENGTH_SHORT).show();
                // Apply dark theme
            } else {
                Toast.makeText(this, "Dark mode disabled", Toast.LENGTH_SHORT).show();
                // Apply light theme
            }
        });

        // Currency Selection
        btnCurrency.setOnClickListener(v -> {
            showCurrencyDialog();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void loadUserData() {
        // Load user data from database/shared preferences
        // This is sample data - replace with actual data loading
        String userName = "John Doe";
        String email = "john.doe@example.com";
        String phone = "+91 98765 43210";

        // Update display
        tvUserName.setText(userName);
        tvEmail.setText(email);
        tvPhone.setText(phone);

        // Update editable fields
        etFullName.setText(userName);
        etEmail.setText(email);
        etPhone.setText(phone);
    }

    private void saveUserChanges() {
        String newName = etFullName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        // Validation
        if (newName.isEmpty()) {
            etFullName.setError("Name cannot be empty");
            etFullName.requestFocus();
            return;
        }

        if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (newPhone.isEmpty()) {
            etPhone.setError("Phone cannot be empty");
            etPhone.requestFocus();
            return;
        }

        // Save to database/shared preferences
        // TODO: Implement actual save logic
        
        // Update display
        tvUserName.setText(newName);
        tvEmail.setText(newEmail);
        tvPhone.setText(newPhone);

        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void showCurrencyDialog() {
        final String[] currencies = {"INR (₹)", "USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Currency");
        builder.setItems(currencies, (dialog, which) -> {
            tvSelectedCurrency.setText(currencies[which]);
            Toast.makeText(this, "Currency changed to " + currencies[which], Toast.LENGTH_SHORT).show();
            // Save currency preference
        });
        builder.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> {
            // Clear user session
            // Navigate to login screen
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(intent);
            // finish();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Handle photo picker result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle image selection
        // if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
        //     Uri imageUri = data.getData();
        //     ivProfileImage.setImageURI(imageUri);
        //     // Upload image to server
        // }
    }
}
