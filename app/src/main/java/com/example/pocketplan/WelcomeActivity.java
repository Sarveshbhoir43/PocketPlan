package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import utils.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

    Button btnSignIn, btnLogin;
    LinearLayout card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If user is already logged in, skip welcome screen
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        card      = findViewById(R.id.welcomeCard);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnLogin  = findViewById(R.id.btnLogin);

        // Card animation
        card.setScaleX(0.95f);
        card.setScaleY(0.95f);
        card.setAlpha(0f);
        card.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(600).setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();

        btnSignIn.setTranslationY(100f);
        btnSignIn.setAlpha(0f);
        btnSignIn.animate().translationY(0f).alpha(1f)
                .setDuration(500).setStartDelay(400)
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();

        // Register (new user)
        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // Login (returning user)
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }
    }
}