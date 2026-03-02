package com.example.economiza.ui.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.ui.viewmodel.TransactionViewModel;
import com.example.economiza.ui.viewmodel.ViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Screen for logging a new income or expense transaction.
 *
 * OCR / receipt scanning is a placeholder — tapping "Scan Receipt" shows a
 * "Coming Soon" message. The full ML Kit implementation will be added in a
 * later sprint once the core data layer is stable.
 */
public class AddTransactionActivity extends AppCompatActivity {

    private TransactionViewModel viewModel;
    private TextInputEditText etAmount, etDescription;
    private TextView txtDate;
    private MaterialButton btnExpense, btnIncome;
    private boolean isExpense = true;
    private long selectedDateMillis = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Bind views
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        txtDate = findViewById(R.id.txt_date);
        btnExpense = findViewById(R.id.btn_expense);
        btnIncome = findViewById(R.id.btn_income);

        // ViewModel
        ViewModelFactory factory = ((EconomizaApp) getApplication()).getViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);

        // Default date = today
        updateDateLabel();

        // Navigation
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Expense / Income toggle
        btnExpense.setOnClickListener(v -> setTransactionType(true));
        btnIncome.setOnClickListener(v -> setTransactionType(false));

        // Date picker
        findViewById(R.id.card_date).setOnClickListener(v -> showDatePicker());

        // OCR placeholder
        findViewById(R.id.btn_scan_receipt).setOnClickListener(v -> Toast.makeText(this,
                "📷 Receipt scanning coming soon! Enter details manually for now.",
                Toast.LENGTH_LONG).show());

        // Save
        findViewById(R.id.btn_save).setOnClickListener(v -> saveTransaction());
    }

    private void setTransactionType(boolean expense) {
        isExpense = expense;
        int blue = getColor(R.color.primary_blue);
        int transparent = android.graphics.Color.TRANSPARENT;
        int white = getColor(R.color.white);
        int grey = getColor(R.color.text_secondary);

        btnExpense.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                expense ? blue : transparent));
        btnExpense.setTextColor(expense ? white : grey);

        btnIncome.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                expense ? transparent : blue));
        btnIncome.setTextColor(expense ? grey : white);
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        txtDate.setText(sdf.format(new Date(selectedDateMillis)));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);
        new DatePickerDialog(this, (dp, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, 0, 0, 0);
            selectedDateMillis = selected.getTimeInMillis();
            updateDateLabel();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaction() {
        String amountStr = getText(etAmount);
        String description = getText(etDescription);

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        double amountDouble;
        try {
            amountDouble = Double.parseDouble(amountStr.replace(",", "."));
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid number");
            return;
        }

        if (amountDouble <= 0) {
            etAmount.setError("Amount must be greater than zero");
            return;
        }

        Transaction transaction = new Transaction();
        transaction.amount = Math.round(amountDouble * 100); // store as cents
        transaction.description = description;
        transaction.timestamp = selectedDateMillis;
        transaction.isIncome = !isExpense;
        transaction.categoryId = 0; // TODO: category picker

        // addTransaction already runs on a background thread inside the ViewModel
        viewModel.addTransaction(transaction);

        Toast.makeText(this, "Transaction saved ✓", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getText(TextInputEditText view) {
        return view.getText() != null ? view.getText().toString().trim() : "";
    }
}
