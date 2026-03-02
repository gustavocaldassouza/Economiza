package com.example.economiza.ui.fragments;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.model.Frequency;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.ui.adapter.RecurringAdapter;
import com.example.economiza.ui.viewmodel.RecurringPaymentViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RecurringFragment extends Fragment {

    private RecurringPaymentViewModel vm;
    private RecurringAdapter adapter;
    /**
     * Cached category list for pickers — loaded once from the category repository.
     */
    private List<Category> categoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recurring, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_recurring);
        MaterialButton btnAdd = view.findViewById(R.id.btn_add_recurring);
        android.widget.LinearLayout emptyState = view.findViewById(R.id.layout_recurring_empty);

        // Load categories on a background thread so the picker is populated
        EconomizaApp app = (EconomizaApp) requireActivity().getApplication();
        new Thread(() -> {
            categoryList = app.getCategoryRepository().getAllCategoriesSync();
        }).start();

        adapter = new RecurringAdapter();
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this, app.getViewModelFactory())
                .get(RecurringPaymentViewModel.class);

        vm.payments.observe(getViewLifecycleOwner(), payments -> {
            if (payments != null) {
                adapter.setPayments(payments);
                boolean empty = payments.isEmpty();
                rv.setVisibility(empty ? View.GONE : View.VISIBLE);
                emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });

        // Tap a card → open edit dialog
        adapter.setOnItemClickListener(this::showEditDialog);

        // Toggle active switch callback
        adapter.setOnToggleListener(payment -> vm.toggleActive(payment));

        // Swipe left to delete
        new ItemTouchHelper(new SwipeToDeleteCallback()).attachToRecyclerView(rv);

        btnAdd.setOnClickListener(v -> showAddDialog());
    }

    // ── Add Dialog ───────────────────────────────────────────────────────────

    private void showAddDialog() {
        View dialogView = buildPaymentFormView(null);
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Recurring Payment")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    RecurringPayment payment = readFormFields(dialogView, null);
                    if (payment == null)
                        return;

                    long now = System.currentTimeMillis();
                    if (payment.nextDueDate <= now) {
                        // Billing day already passed this month → ask the user
                        showPostNowConfirmationDialog(payment);
                    } else {
                        // Billing day is in the future — just save, processor will handle it
                        vm.addPaymentOnly(payment);
                        Toast.makeText(requireContext(), "Recurring payment added!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Shown when the chosen billing day has already passed this month.
     * Lets the user decide whether to record the current-month transaction
     * immediately.
     */
    private void showPostNowConfirmationDialog(RecurringPayment payment) {
        java.text.SimpleDateFormat monthFmt = new java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault());
        String monthName = monthFmt.format(new java.util.Date(payment.nextDueDate));
        String amountStr = String.format(java.util.Locale.getDefault(),
                "$ %.2f", payment.amount / 100.0);

        new AlertDialog.Builder(requireContext())
                .setTitle("Record this month?")
                .setMessage("The billing day for " + payment.description
                        + " (" + amountStr + ") has already passed in " + monthName + ".\n\n"
                        + "Would you like to record " + monthName + "'s transaction right now?")
                .setPositiveButton("Yes, record it", (d, w) -> {
                    // Insert + run processor → posts the current-month transaction
                    vm.addPayment(payment);
                    Toast.makeText(requireContext(),
                            payment.description + " added and " + monthName
                                    + "'s transaction recorded!",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Skip this month", (d, w) -> {
                    // Advance nextDueDate by one period so the processor won't auto-post it
                    payment.nextDueDate = advanceOneFrequency(payment.nextDueDate, payment.frequency);
                    vm.addPaymentOnly(payment);
                    Toast.makeText(requireContext(),
                            payment.description + " added. First payment due next cycle.",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * Advances a timestamp by one frequency period (mirrors
     * ProcessRecurringPaymentsUseCase logic).
     */
    private long advanceOneFrequency(long fromMs, Frequency frequency) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fromMs);
        switch (frequency) {
            case DAILY:
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case WEEKLY:
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case BIWEEKLY:
                cal.add(Calendar.WEEK_OF_YEAR, 2);
                break;
            case YEARLY:
                cal.add(Calendar.YEAR, 1);
                break;
            case MONTHLY:
            default:
                cal.add(Calendar.MONTH, 1);
                break;
        }
        return cal.getTimeInMillis();
    }

    // ── Edit Dialog ──────────────────────────────────────────────────────────

    private void showEditDialog(RecurringPayment existing) {
        View dialogView = buildPaymentFormView(existing);
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Recurring Payment")
                .setView(dialogView)
                .setPositiveButton("Update", (d, w) -> {
                    RecurringPayment updated = readFormFields(dialogView, existing);
                    if (updated == null)
                        return;
                    vm.updatePayment(updated);
                    Toast.makeText(requireContext(), "Payment updated!", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Delete", (d, w) -> {
                    vm.deletePayment(existing);
                    Toast.makeText(requireContext(), "Deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Form builder ─────────────────────────────────────────────────────────

    private View buildPaymentFormView(@Nullable RecurringPayment existing) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        // Name
        android.widget.EditText etName = new android.widget.EditText(requireContext());
        etName.setHint("Name (e.g. Netflix)");
        etName.setId(R.id.et_recurring_name);
        if (existing != null)
            etName.setText(existing.description);
        layout.addView(etName);

        // Amount
        android.widget.EditText etAmount = new android.widget.EditText(requireContext());
        etAmount.setHint("Amount (e.g. 45.90)");
        etAmount.setId(R.id.et_recurring_amount);
        etAmount.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (existing != null)
            etAmount.setText(String.format(java.util.Locale.getDefault(), "%.2f", existing.amount / 100.0));
        layout.addView(etAmount);

        // Day-of-month label + spinner
        android.widget.TextView tvDay = new android.widget.TextView(requireContext());
        tvDay.setText("Billing day (1–28)");
        tvDay.setPadding(0, 24, 0, 4);
        layout.addView(tvDay);

        Spinner spDay = new Spinner(requireContext());
        spDay.setId(R.id.sp_recurring_day);
        String[] days = new String[28];
        for (int i = 0; i < 28; i++)
            days[i] = String.valueOf(i + 1);
        spDay.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, days));
        int preselectedDay = 0;
        if (existing != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(existing.nextDueDate);
            preselectedDay = c.get(Calendar.DAY_OF_MONTH) - 1;
        }
        spDay.setSelection(Math.max(0, Math.min(preselectedDay, 27)));
        layout.addView(spDay);

        // Frequency label + spinner
        android.widget.TextView tvFreq = new android.widget.TextView(requireContext());
        tvFreq.setText("Frequency");
        tvFreq.setPadding(0, 24, 0, 4);
        layout.addView(tvFreq);

        Spinner spFreq = new Spinner(requireContext());
        spFreq.setId(R.id.sp_recurring_frequency);
        Frequency[] freqs = Frequency.values();
        String[] freqLabels = new String[freqs.length];
        for (int i = 0; i < freqs.length; i++)
            freqLabels[i] = formatFrequency(freqs[i]);
        spFreq.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, freqLabels));
        if (existing != null && existing.frequency != null) {
            for (int i = 0; i < freqs.length; i++) {
                if (freqs[i] == existing.frequency) {
                    spFreq.setSelection(i);
                    break;
                }
            }
        } else {
            // Default: MONTHLY (index 3)
            spFreq.setSelection(3);
        }
        layout.addView(spFreq);

        // Category label + spinner
        android.widget.TextView tvCat = new android.widget.TextView(requireContext());
        tvCat.setText("Category");
        tvCat.setPadding(0, 24, 0, 4);
        layout.addView(tvCat);

        Spinner spCat = new Spinner(requireContext());
        spCat.setId(R.id.sp_recurring_category);
        if (!categoryList.isEmpty()) {
            String[] catNames = categoryList.stream().map(c -> c.name).toArray(String[]::new);
            spCat.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, catNames));
            if (existing != null) {
                for (int i = 0; i < categoryList.size(); i++) {
                    if (categoryList.get(i).id == existing.categoryId) {
                        spCat.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            spCat.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, new String[] { "(No categories)" }));
        }
        layout.addView(spCat);

        // Store spinners as tags for readFormFields
        layout.setTag(new Object[] { spDay, spFreq, spCat });
        return layout;
    }

    /**
     * Reads and validates the form, returning a populated RecurringPayment or null
     * on error.
     */
    @Nullable
    private RecurringPayment readFormFields(View dialogView, @Nullable RecurringPayment existing) {
        android.widget.LinearLayout layout = (android.widget.LinearLayout) dialogView;
        android.widget.EditText etName = layout.findViewById(R.id.et_recurring_name);
        android.widget.EditText etAmount = layout.findViewById(R.id.et_recurring_amount);
        Object[] spinners = (Object[]) layout.getTag();
        Spinner spDay = (Spinner) spinners[0];
        Spinner spFreq = (Spinner) spinners[1];
        Spinner spCat = (Spinner) spinners[2];

        String name = etName.getText().toString().trim();
        String amtStr = etAmount.getText().toString().trim();
        if (name.isEmpty() || amtStr.isEmpty()) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return null;
        }

        RecurringPayment p = (existing != null) ? existing : new RecurringPayment();
        p.description = name;
        p.amount = Math.round(Double.parseDouble(amtStr) * 100);
        p.frequency = Frequency.values()[spFreq.getSelectedItemPosition()];
        p.isActive = (existing == null) || existing.isActive;

        int dayOfMonth = spDay.getSelectedItemPosition() + 1; // 1..28
        if (existing == null) {
            // Anchor to THIS month's billing day so the auto-processor can immediately
            // detect and post it if the day has already passed (e.g. chose day 1 on March 2
            // → processor posts March 1 and advances to April 1).
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            p.nextDueDate = cal.getTimeInMillis();
        } else {
            // Edit: keep the existing due-month but update the day-of-month
            Calendar existing_cal = Calendar.getInstance();
            existing_cal.setTimeInMillis(existing.nextDueDate);
            existing_cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            p.nextDueDate = existing_cal.getTimeInMillis();
        }

        if (!categoryList.isEmpty()) {
            p.categoryId = categoryList.get(spCat.getSelectedItemPosition()).id;
        }
        return p;
    }

    private String formatFrequency(Frequency f) {
        switch (f) {
            case DAILY:
                return "Daily";
            case WEEKLY:
                return "Weekly";
            case BIWEEKLY:
                return "Bi-weekly";
            case MONTHLY:
                return "Monthly";
            case YEARLY:
                return "Yearly";
            default:
                return f.name();
        }
    }

    // ── Swipe-to-delete ──────────────────────────────────────────────────────

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final ColorDrawable redBackground = new ColorDrawable(Color.parseColor("#D32F2F"));
        private final Paint textPaint;

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT);
            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(42f);
            textPaint.setTextAlign(Paint.Align.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
            RecurringPayment p = adapter.getPaymentAt(vh.getAdapterPosition());
            vm.deletePayment(p);
            Toast.makeText(requireContext(), "\"" + p.description + "\" removed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                @NonNull RecyclerView.ViewHolder vh, float dX, float dY,
                int actionState, boolean isCurrentlyActive) {
            View itemView = vh.itemView;
            if (dX < 0) {
                redBackground.setBounds(
                        (int) (itemView.getRight() + dX), itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());
                redBackground.draw(c);
                float textX = itemView.getRight() - 40f;
                float textY = itemView.getTop() + (itemView.getHeight() / 2f) + 15f;
                c.drawText("Delete", textX, textY, textPaint);
            }
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
