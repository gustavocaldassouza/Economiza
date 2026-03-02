package com.example.economiza.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.economiza.EconomizaApp;
import com.example.economiza.MainActivity;
import com.example.economiza.R;
import com.example.economiza.data.local.AppDatabase;
import com.example.economiza.data.local.VaultManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Unlocking requires BOTH the vault password (PBKDF2 key derivation) AND the
 * device PIN (fast secondary check). This provides defence-in-depth: the vault
 * stays encrypted even if one credential is compromised.
 *
 * "Forgot your password?" → warns the user that all data will be erased, then
 * destroys the vault and redirects to CreateVaultActivity so they can start
 * fresh.
 */
public class UnlockVaultActivity extends AppCompatActivity {

    private TextInputEditText etPassword, etPin;
    private TextView txtError, txtPinError;
    private MaterialButton btnUnlock;
    private CircularProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_vault);

        etPassword = findViewById(R.id.et_password);
        etPin = findViewById(R.id.et_pin);
        txtError = findViewById(R.id.txt_error);
        txtPinError = findViewById(R.id.txt_pin_error);
        btnUnlock = findViewById(R.id.btn_unlock);
        progressBar = findViewById(R.id.progress_bar);

        // Check whether a PIN has been configured — hide the PIN field if not
        VaultManager vaultManager = ((EconomizaApp) getApplication()).getVaultManager();
        boolean pinRequired = vaultManager.hasPinSet();
        findViewById(R.id.lbl_pin).setVisibility(pinRequired ? View.VISIBLE : View.GONE);
        findViewById(R.id.til_pin).setVisibility(pinRequired ? View.VISIBLE : View.GONE);
        txtPinError.setVisibility(View.GONE);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Unlock on button click
        btnUnlock.setOnClickListener(v -> attemptUnlock());

        // Allow pressing "Done" on the PIN keyboard to trigger unlock
        etPin.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptUnlock();
                return true;
            }
            return false;
        });
        // Also support "Done" from the password field when PIN not required
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (!pinRequired && actionId == EditorInfo.IME_ACTION_DONE) {
                attemptUnlock();
                return true;
            }
            return false;
        });

        // Forgot password → nuke vault → go to Create New Vault
        TextView txtForgot = findViewById(R.id.txt_forgot);
        if (txtForgot != null) {
            txtForgot.setOnClickListener(v -> confirmForgotPassword());
        }
    }

    // ─── Unlock flow ─────────────────────────────────────────────────────────

    private void attemptUnlock() {
        String password = getText(etPassword);
        String pin = getText(etPin);

        // Clear previous errors
        clearErrors();

        if (password.isEmpty()) {
            showError("Please enter your vault password");
            return;
        }

        VaultManager vm = ((EconomizaApp) getApplication()).getVaultManager();
        boolean pinRequired = vm.hasPinSet();

        if (pinRequired && pin.isEmpty()) {
            showPinError("Please enter your device PIN");
            return;
        }
        if (pinRequired && (pin.length() < 4 || pin.length() > 8)) {
            showPinError("PIN must be 4–8 digits");
            return;
        }

        setLoading(true);

        // PBKDF2 is CPU-intensive — always off the main thread
        new Thread(() -> {
            // 1. Verify vault password and derive encryption key
            byte[] key = vm.unlockVault(password);

            // 2. Verify PIN (if set) — fast check, still off-thread for consistency
            boolean pinOk = !pinRequired || vm.verifyPin(pin);

            runOnUiThread(() -> {
                setLoading(false);
                if (key == null) {
                    showError("❌ Incorrect vault password. Please try again.");
                    etPassword.setText("");
                    etPassword.requestFocus();
                } else if (!pinOk) {
                    showPinError("❌ Incorrect device PIN. Please try again.");
                    etPin.setText("");
                    etPin.requestFocus();
                } else {
                    // ✅ Both credentials correct
                    try {
                        ((EconomizaApp) getApplication()).initDependencies(key);
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        showError("Failed to open vault: " + e.getMessage());
                    }
                }
            });
        }).start();
    }

    // ─── Forgot password flow ─────────────────────────────────────────────────

    private void confirmForgotPassword() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Forgot Password")
                .setMessage(
                        "If you cannot remember your vault password, you can create a new vault.\n\n" +
                                "⚠️ WARNING: This will permanently delete ALL your existing financial data " +
                                "and cannot be undone.\n\n" +
                                "Are you absolutely sure you want to erase and start fresh?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("🗑 Erase & Create New Vault", (d, w) -> destroyAndRecreate())
                .setNegativeButton("Cancel — I'll Try Again", null)
                .show();
    }

    private void destroyAndRecreate() {
        // Close and destroy the encrypted database instance
        AppDatabase.destroyInstance();

        // Try to also delete the physical DB file so the new vault starts clean
        try {
            java.io.File dbFile = getDatabasePath("economiza_vault.db");
            if (dbFile.exists())
                dbFile.delete();
            // Also delete WAL and SHM sidecar files
            new java.io.File(dbFile.getPath() + "-wal").delete();
            new java.io.File(dbFile.getPath() + "-shm").delete();
        } catch (Exception ignored) {
            /* best-effort */ }

        // Wipe all vault metadata (salt, hash, PIN) from SharedPreferences
        ((EconomizaApp) getApplication()).getVaultManager().destroyVault();

        // Navigate to vault creation, clearing the back stack
        Intent intent = new Intent(this, CreateVaultActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ─── UI helpers ───────────────────────────────────────────────────────────

    private void showError(String msg) {
        txtError.setText(msg);
        txtError.setVisibility(View.VISIBLE);
    }

    private void showPinError(String msg) {
        txtPinError.setText(msg);
        txtPinError.setVisibility(View.VISIBLE);
    }

    private void clearErrors() {
        txtError.setVisibility(View.GONE);
        txtPinError.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        btnUnlock.setEnabled(!loading);
        btnUnlock.setText(loading ? "Verifying…" : "Unlock Vault");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String getText(TextInputEditText v) {
        return v.getText() != null ? v.getText().toString().trim() : "";
    }
}
