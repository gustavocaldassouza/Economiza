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

public class UnlockVaultActivity extends AppCompatActivity {

    private TextInputEditText etPassword;
    private TextView txtError;
    private MaterialButton btnUnlock;
    private CircularProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_vault);

        etPassword = findViewById(R.id.et_password);
        txtError = findViewById(R.id.txt_error);
        btnUnlock = findViewById(R.id.btn_unlock);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnUnlock.setOnClickListener(v -> attemptUnlock());

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptUnlock();
                return true;
            }
            return false;
        });

        TextView txtForgot = findViewById(R.id.txt_forgot);
        if (txtForgot != null) {
            txtForgot.setOnClickListener(v -> confirmForgotPassword());
        }
    }

    private void attemptUnlock() {
        String password = getText(etPassword);

        txtError.setVisibility(View.GONE);

        if (password.isEmpty()) {
            showError("Please enter your vault password");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            VaultManager vm = ((EconomizaApp) getApplication()).getVaultManager();
            byte[] key = vm.unlockVault(password);

            runOnUiThread(() -> {
                setLoading(false);
                if (key == null) {
                    showError("Incorrect vault password. Please try again.");
                    etPassword.setText("");
                    etPassword.requestFocus();
                } else {
                    try {
                        ((EconomizaApp) getApplication()).initDependencies(key);
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        showError("Failed to open vault: " + e.getMessage());
                    }
                }
            });
        }).start();
    }

    private void confirmForgotPassword() {
        new AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setMessage(
                        "If you cannot remember your vault password, you can create a new vault.\n\n" +
                                "WARNING: This will permanently delete ALL your existing financial data " +
                                "and cannot be undone.\n\nAre you absolutely sure?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Erase & Create New Vault", (d, w) -> destroyAndRecreate())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void destroyAndRecreate() {
        AppDatabase.destroyInstance();
        try {
            java.io.File dbFile = getDatabasePath("economiza_vault.db");
            if (dbFile.exists()) dbFile.delete();
            new java.io.File(dbFile.getPath() + "-wal").delete();
            new java.io.File(dbFile.getPath() + "-shm").delete();
        } catch (Exception ignored) {}

        ((EconomizaApp) getApplication()).getVaultManager().destroyVault();

        Intent intent = new Intent(this, CreateVaultActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showError(String msg) {
        txtError.setText(msg);
        txtError.setVisibility(View.VISIBLE);
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
