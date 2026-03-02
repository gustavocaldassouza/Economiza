package com.example.economiza.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.economiza.EconomizaApp;
import com.example.economiza.MainActivity;
import com.example.economiza.R;
import com.example.economiza.data.local.VaultManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Screen for unlocking an existing vault with the user's password.
 * PBKDF2 key derivation happens on a background thread to avoid ANR.
 * On success → MainActivity. On failure → shows error, user can retry.
 */
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

        // Allow pressing "Done" on keyboard to trigger unlock
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptUnlock();
                return true;
            }
            return false;
        });
    }

    private void attemptUnlock() {
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (password.isEmpty()) {
            showError("Please enter your password");
            return;
        }

        setLoading(true);

        // PBKDF2 is intentionally slow — do it off the main thread
        new Thread(() -> {
            VaultManager vm = ((EconomizaApp) getApplication()).getVaultManager();
            byte[] key = vm.unlockVault(password);

            runOnUiThread(() -> {
                setLoading(false);
                if (key != null) {
                    try {
                        ((EconomizaApp) getApplication()).initDependencies(key);
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        showError("Failed to open vault: " + e.getMessage());
                    }
                } else {
                    showError("Incorrect password. Please try again.");
                    etPassword.setText("");
                    etPassword.requestFocus();
                }
            });
        }).start();
    }

    private void showError(String message) {
        txtError.setText(message);
        txtError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        btnUnlock.setEnabled(!loading);
        btnUnlock.setText(loading ? "Unlocking…" : "Unlock");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        txtError.setVisibility(View.GONE);
    }
}
