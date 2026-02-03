package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    Button btnSignIn;
    LinearLayout card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // NOW views exist
        card = findViewById(R.id.welcomeCard);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Card animation - subtle scale and fade in
        card.setScaleX(0.95f);
        card.setScaleY(0.95f);
        card.setAlpha(0f);

        card.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Button animation - slide up and fade in
        btnSignIn.setTranslationY(100f);
        btnSignIn.setAlpha(0f);

        btnSignIn.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Button click
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
