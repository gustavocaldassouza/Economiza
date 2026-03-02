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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Shown after a new vault is successfully created.
 * User can optionally set a 4–8 digit PIN that will be required alongside
 * the vault password on every future unlock.
 * Skipping leaves hasPinSet() == false, so the PIN field is hidden on unlock.
 */
public class SetPinActivity extends AppCompatActivity {

    private TextInputEditText etPin, etConfirm;
    private TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);

        etPin = findViewById(R.id.et_pin);
        etConfirm = findViewById(R.id.et_confirm_pin);
        txtError = findViewById(R.id.txt_error);
        MaterialButton btnSet = findViewById(R.id.btn_set_pin);
        TextView btnSkip = findViewById(R.id.btn_skip_pin);

        btnSet.setOnClickListener(v -> attemptSetPin());

        etConfirm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSetPin();
                return true;
            }
            return false;
        });

        // Skip → go straight to MainActivity (no PIN stored)
        btnSkip.setOnClickListener(v -> openMain());
    }

    private void attemptSetPin() {
        String pin = getText(etPin);
        String confirm = getText(etConfirm);

        txtError.setVisibility(View.GONE);

        if (pin.length() < 4) {
            showError("PIN must be at least 4 digits");
            return;
        }
        if (!pin.equals(confirm)) {
            showError("PINs do not match");
            etConfirm.setText("");
            etConfirm.requestFocus();
            return;
        }

        // Store PIN hash in VaultManager (off main thread for consistency,
        // though PBKDF2 10k iterations is fast enough here)
        new Thread(() -> {
            ((EconomizaApp) getApplication()).getVaultManager().createPin(pin);
            runOnUiThread(this::openMain);
        }).start();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showError(String msg) {
        txtError.setText(msg);
        txtError.setVisibility(View.VISIBLE);
    }

    private String getText(TextInputEditText v) {
        return v.getText() != null ? v.getText().toString().trim() : "";
    }
}
