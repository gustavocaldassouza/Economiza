package com.example.economiza.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.economiza.MainActivity;
import com.example.economiza.R;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        findViewById(R.id.card_create_vault).setOnClickListener(v -> {
            // In a real app, this would initialize a new encrypted database
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
        });

        findViewById(R.id.card_open_vault).setOnClickListener(v -> {
            // In a real app, this would open a file picker
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
        });
    }
}
