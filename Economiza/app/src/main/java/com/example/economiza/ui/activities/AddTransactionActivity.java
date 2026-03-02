package com.example.economiza.ui.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.ui.viewmodel.CategoryViewModel;
import com.example.economiza.ui.viewmodel.TransactionViewModel;
import com.example.economiza.ui.viewmodel.ViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Screen for logging a new income or expense transaction.
 * Includes a live category picker loaded from the encrypted database.
 */
public class AddTransactionActivity extends BaseActivity {

    private TransactionViewModel txViewModel;
    private TextInputEditText etAmount, etDescription;
    private TextView txtDate;
    private MaterialButton btnExpense, btnIncome, btnDelete;
    private Spinner spinnerCategory;
    private View viewCategoryColor;

    private boolean isExpense = true;
    private long selectedDateMillis = System.currentTimeMillis();
    private List<Category> categoryList = new ArrayList<>();
    private int selectedCategoryId = 0;
    private int editingTransactionId = -1;

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
        spinnerCategory = findViewById(R.id.spinner_category);
        viewCategoryColor = findViewById(R.id.view_category_color);
        btnDelete = findViewById(R.id.btn_delete);

        // ViewModels
        ViewModelFactory factory = ((EconomizaApp) getApplication()).getViewModelFactory();
        txViewModel = new ViewModelProvider(this, factory).get(TransactionViewModel.class);
        CategoryViewModel catViewModel = new ViewModelProvider(this, factory).get(CategoryViewModel.class);

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

        // ── Category spinner ────────────────────────────────────────────────
        catViewModel.categories.observe(this, categories -> {
            if (categories == null || categories.isEmpty())
                return;
            categoryList = categories;

            ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                    this, android.R.layout.simple_spinner_item, categoryList) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView tv = (TextView) super.getView(position, convertView, parent);
                    tv.setText(categoryList.get(position).name);
                    tv.setTextColor(Color.WHITE);
                    return tv;
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                    tv.setText(categoryList.get(position).name);
                    tv.setTextColor(Color.WHITE);
                    tv.setPadding(32, 32, 32, 32);
                    return tv;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);

            // Pre-select if we already have the ID (from editing)
            if (selectedCategoryId != 0) {
                for (int i = 0; i < categoryList.size(); i++) {
                    if (categoryList.get(i).id == selectedCategoryId) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            } else if (!categoryList.isEmpty()) {
                selectedCategoryId = categoryList.get(0).id;
            }

            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Category selected = categoryList.get(pos);
                    selectedCategoryId = selected.id;
                    // Update the color swatch
                    updateCategoryColor(selected.colorHex);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        });

        // Check if editing
        editingTransactionId = getIntent().getIntExtra("EXTRA_TRANSACTION_ID", -1);
        if (editingTransactionId != -1) {
            ((TextView) findViewById(R.id.txt_title)).setText("Edit Transaction");
            // Show and wire the delete button only in edit mode
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> confirmDelete());
            loadTransactionForEditing();
        }
    }

    private void loadTransactionForEditing() {
        new Thread(() -> {
            Transaction t = txViewModel.getTransactionById(editingTransactionId);
            if (t != null) {
                runOnUiThread(() -> {
                    etAmount.setText(String.valueOf(t.amount / 100.0));
                    etDescription.setText(t.description);
                    selectedDateMillis = t.timestamp;
                    updateDateLabel();
                    setTransactionType(!t.isIncome);
                    selectedCategoryId = t.categoryId;

                    // Set spinner selection if categories are already loaded
                    if (!categoryList.isEmpty()) {
                        for (int i = 0; i < categoryList.size(); i++) {
                            if (categoryList.get(i).id == selectedCategoryId) {
                                spinnerCategory.setSelection(i);
                                updateCategoryColor(categoryList.get(i).colorHex);
                                break;
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void updateCategoryColor(String colorHex) {
        if (viewCategoryColor != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            try {
                bg.setColor(Color.parseColor(colorHex));
            } catch (Exception e) {
                bg.setColor(Color.GRAY);
            }
            viewCategoryColor.setBackground(bg);
        }
    }

    private void setTransactionType(boolean expense) {
        isExpense = expense;
        int blue = getColor(R.color.primary_blue);
        int inactiveColor = getColor(R.color.card_background);
        int white = getColor(R.color.white);
        int grey = getColor(R.color.text_secondary);

        btnExpense.setBackgroundTintList(android.content.res.ColorStateList.valueOf(expense ? blue : inactiveColor));
        btnExpense.setTextColor(expense ? white : grey);

        btnIncome.setBackgroundTintList(android.content.res.ColorStateList.valueOf(expense ? inactiveColor : blue));
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
        if (editingTransactionId != -1)
            transaction.id = editingTransactionId;

        transaction.amount = Math.round(amountDouble * 100);
        transaction.description = description;
        transaction.timestamp = selectedDateMillis;
        transaction.isIncome = !isExpense;
        transaction.categoryId = selectedCategoryId;

        if (editingTransactionId != -1) {
            txViewModel.updateTransaction(transaction);
        } else {
            txViewModel.addTransaction(transaction);
        }

        Toast.makeText(this, "Transaction saved ✓", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getText(TextInputEditText view) {
        return view.getText() != null ? view.getText().toString().trim() : "";
    }

    private void confirmDelete() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to permanently delete this transaction?")
                .setPositiveButton("Delete", (d, w) -> {
                    // Build a minimal Transaction with just the ID so Room can find and delete it
                    Transaction t = new Transaction();
                    t.id = editingTransactionId;
                    txViewModel.deleteTransaction(t);
                    Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
