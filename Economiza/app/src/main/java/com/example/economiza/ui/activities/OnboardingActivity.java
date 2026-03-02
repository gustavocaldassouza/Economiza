package com.example.economiza.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;

/**
 * Entry point shown on first install.
 *
 * • "Create New Vault" → {@link CreateVaultActivity} (sets up password)
 * • "Open Existing Vault" → {@link UnlockVaultActivity} (enter password)
 *
 * If the user returns here after a vault already exists (e.g. they backed out
 * of
 * unlock), only the "Open" path is shown as active — "Create" would be
 * destructive
 * and is blocked by VaultManager.
 */
public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        boolean vaultExists = ((EconomizaApp) getApplication())
                .getVaultManager().vaultExists();

        // If a vault already exists, skip this screen and go straight to unlock
        if (vaultExists) {
            startActivity(new Intent(this, UnlockVaultActivity.class));
            finish();
            return;
        }

        // No vault yet — show both options
        findViewById(R.id.card_create_vault)
                .setOnClickListener(v -> startActivity(new Intent(this, CreateVaultActivity.class)));

        // "Open Existing" makes sense if the user previously exported a vault file;
        // for now it routes to unlock (same password entry flow).
        findViewById(R.id.card_open_vault)
                .setOnClickListener(v -> startActivity(new Intent(this, UnlockVaultActivity.class)));
    }
}
