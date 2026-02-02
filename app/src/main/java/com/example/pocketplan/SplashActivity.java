package com.example.pocketplan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View rootView = findViewById(android.R.id.content);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Animate the splash screen sliding down
            rootView.animate()
                    .translationY(rootView.getHeight())
                    .setDuration(800)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                        startActivity(intent);
                        // Use no transition - we already animated
                        overridePendingTransition(0, 0);
                        finish();
                    })
                    .start();
        }, 2000);
    }
}
