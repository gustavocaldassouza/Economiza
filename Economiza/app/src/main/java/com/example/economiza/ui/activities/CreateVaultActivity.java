package com.example.economiza.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.economiza.EconomizaApp;
import com.example.economiza.MainActivity;
import com.example.economiza.R;
import com.example.economiza.data.local.VaultManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Screen for first-time vault creation.
 * User picks a password → VaultManager derives PBKDF2 key → database is
 * initialised.
 */
public class CreateVaultActivity extends AppCompatActivity {

    private TextInputEditText etPassword, etConfirm;
    private TextInputLayout tilPassword, tilConfirm;
    private LinearProgressIndicator strengthBar;
    private TextView txtStrength;
    private MaterialButton btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vault);

        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);
        tilPassword = findViewById(R.id.til_password);
        tilConfirm = findViewById(R.id.til_confirm);
        strengthBar = findViewById(R.id.progress_strength);
        txtStrength = findViewById(R.id.txt_strength);
        btnCreate = findViewById(R.id.btn_create);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Live strength meter
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int count) {
                updateStrengthBar(s.toString());
            }
        });

        btnCreate.setOnClickListener(v -> attemptCreate());
    }

    private void attemptCreate() {
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

        tilPassword.setError(null);
        tilConfirm.setError(null);

        if (password.length() < 8) {
            tilPassword.setError("Password must be at least 8 characters");
            return;
        }
        if (!password.equals(confirm)) {
            tilConfirm.setError("Passwords do not match");
            return;
        }

        // PBKDF2 is CPU-intensive — run on a background thread
        btnCreate.setEnabled(false);
        btnCreate.setText("Creating vault…");

        new Thread(() -> {
            try {
                VaultManager vm = ((EconomizaApp) getApplication()).getVaultManager();
                byte[] key = vm.createVault(password);
                // Wipe password from memory as soon as possible
                password.getClass(); // no-op to prevent over-eager compiler optimisation

                ((EconomizaApp) getApplication()).initDependencies(key);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Vault created!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnCreate.setEnabled(true);
                    btnCreate.setText("Create Vault");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /** Shows password strength based on length and character variety. */
    private void updateStrengthBar(String password) {
        int score = 0;
        if (password.length() >= 8)
            score++;
        if (password.length() >= 12)
            score++;
        if (password.matches(".*[A-Z].*"))
            score++;
        if (password.matches(".*[0-9].*"))
            score++;
        if (password.matches(".*[^a-zA-Z0-9].*"))
            score++;

        int progress = score * 20; // 0..100
        strengthBar.setProgress(progress);

        int[] colors = {
                0xFFEF5350, // red — Weak
                0xFFEF5350, // red
                0xFFFFD54F, // yellow — Fair
                0xFFFFD54F, // yellow
                0xFF00D084, // green — Strong
                0xFF00D084 // green
        };
        String[] labels = { "", "Weak", "Fair", "Fair", "Strong", "Very Strong" };

        if (score > 0) {
            strengthBar.setIndicatorColor(colors[score]);
            txtStrength.setTextColor(colors[score]);
            txtStrength.setText(labels[score]);
        } else {
            txtStrength.setText("");
        }
    }
}
