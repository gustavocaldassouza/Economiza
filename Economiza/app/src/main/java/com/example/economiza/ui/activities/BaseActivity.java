package com.example.economiza.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.economiza.EconomizaApp;

/**
 * Common base for all secure activities.
 * Automatically checks if the vault is locked on every entry (Start/Restart).
 * If locked (memory wiped due to app backgrounding), redirects to
 * Onboarding/Unlock.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyVaultState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        verifyVaultState();
    }

    private void verifyVaultState() {
        EconomizaApp app = (EconomizaApp) getApplication();
        // If a vault exists on device but it's not currently held in memory (locked)
        if (app.getDatabase() == null && app.getVaultManager().vaultExists()) {
            Intent intent = new Intent(this, OnboardingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
