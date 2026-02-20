package com.example.pocketplan;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import com.example.pocketplan.notifications.LowBalanceChecker;
import com.example.pocketplan.notifications.WeeklyScheduler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // UI Components
    private ImageView ivProfileImage;
    private TextView tvUserName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private MaterialButton btnEditPhoto;
    private MaterialButton btnSaveChanges;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchBiometric;
    private MaterialButton btnLogout;
    private BottomNavigationView bottomNavigation;

    // SharedPreferences
    private SharedPreferences prefs;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme before setContentView
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        loadThemePreference();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setupActivityResultLaunchers();
        loadUserData();
        setupClickListeners();
        setupBottomNavigation();
        loadDarkModeSwitch();
    }

    private void initializeViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchBiometric = findViewById(R.id.switchBiometric);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupActivityResultLaunchers() {
        // Gallery Launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                handleImageSelection(imageUri);
                            } catch (IOException e) {
                                Log.e(TAG, "Error loading image: " + e.getMessage(), e);
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        // Camera Launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap photo = (Bitmap) extras.get("data");
                            if (photo != null) {
                                handleCameraImage(photo);
                            }
                        }
                    }
                }
        );

        // Permission Launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void loadUserData() {
        String name = prefs.getString("name", "User");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("phone", "");

        // Set display values
        tvUserName.setText(name);
        tvEmail.setText(email.isEmpty() ? "No email" : email);
        tvPhone.setText(phone.isEmpty() ? "No phone" : phone);

        // Set editable values
        etFullName.setText(name);
        etEmail.setText(email);
        etPhone.setText(phone);

        // Load profile image if exists
        loadProfileImage();

        // Load preferences
        switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", true));
        switchBiometric.setChecked(prefs.getBoolean("biometric_enabled", false));
    }

    private void loadProfileImage() {
        String imageBase64 = prefs.getString("profile_image", null);
        if (imageBase64 != null) {
            try {
                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivProfileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile image: " + e.getMessage(), e);
            }
        }
    }

    private void setupClickListeners() {
        // Edit Photo Button
        btnEditPhoto.setOnClickListener(v -> showPhotoOptions());

        // Save Changes Button
        btnSaveChanges.setOnClickListener(v -> saveUserData());

        // Dark Mode Switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Notifications Switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            if (isChecked) {
                WeeklyScheduler.schedule(this);
            } else {
                WeeklyScheduler.cancel(this);
            }
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Biometric Switch
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("biometric_enabled", isChecked).apply();
            Toast.makeText(this, isChecked ? "Biometric login enabled" : "Biometric login disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Logout Button
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showPhotoOptions() {
        String[] options = {"Choose from Gallery", "Take Photo", "Remove Photo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Photo");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Gallery
                    openGallery();
                    break;
                case 1: // Camera
                    checkCameraPermission();
                    break;
                case 2: // Remove
                    removeProfilePhoto();
                    break;
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageSelection(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        if (bitmap != null) {
            // Resize bitmap to save space
            Bitmap resizedBitmap = resizeBitmap(bitmap, 500, 500);

            // Display image
            ivProfileImage.setImageBitmap(resizedBitmap);

            // Save to SharedPreferences
            saveProfileImage(resizedBitmap);

            Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show();
        }

        if (inputStream != null) {
            inputStream.close();
        }
    }

    private void handleCameraImage(Bitmap photo) {
        // Resize bitmap
        Bitmap resizedBitmap = resizeBitmap(photo, 500, 500);

        // Display image
        ivProfileImage.setImageBitmap(resizedBitmap);

        // Save to SharedPreferences
        saveProfileImage(resizedBitmap);

        Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show();
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    private void saveProfileImage(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            prefs.edit().putString("profile_image", imageBase64).apply();
            Log.d(TAG, "Profile image saved successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error saving profile image: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to save profile image", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeProfilePhoto() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Photo")
                .setMessage("Are you sure you want to remove your profile photo?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    // Reset to default image
                    ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder);

                    // Remove from SharedPreferences
                    prefs.edit().remove("profile_image").apply();

                    Toast.makeText(this, "Profile photo removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveUserData() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.apply();

        // Update display values
        tvUserName.setText(name);
        tvEmail.setText(email.isEmpty() ? "No email" : email);
        tvPhone.setText(phone.isEmpty() ? "No phone" : phone);

        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadThemePreference() {
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void loadDarkModeSwitch() {
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear user session
                    prefs.edit()
                            .remove("name")
                            .remove("email")
                            .remove("phone")
                            .remove("profile_image")
                            .apply();

                    // Navigate to Welcome/Register Activity
                    Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_transactions) {
                    Intent intent = new Intent(ProfileActivity.this, TransactionsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}