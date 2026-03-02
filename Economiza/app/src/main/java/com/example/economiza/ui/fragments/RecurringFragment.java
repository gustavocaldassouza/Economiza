package com.example.economiza.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.domain.model.Frequency;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.ui.adapter.RecurringAdapter;
import com.example.economiza.ui.viewmodel.RecurringPaymentViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class RecurringFragment extends Fragment {

    private RecurringPaymentViewModel vm;
    private RecurringAdapter adapter;

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

        adapter = new RecurringAdapter();
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this,
                ((EconomizaApp) requireActivity().getApplication()).getViewModelFactory())
                .get(RecurringPaymentViewModel.class);

        vm.payments.observe(getViewLifecycleOwner(), payments -> {
            if (payments != null)
                adapter.setPayments(payments);
        });

        btnAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        android.widget.EditText etName = new android.widget.EditText(requireContext());
        etName.setHint("Name (e.g. Netflix)");

        android.widget.EditText etAmount = new android.widget.EditText(requireContext());
        etAmount.setHint("Amount (e.g. 45.90)");
        etAmount.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        layout.addView(etName);
        layout.addView(etAmount);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Recurring Payment")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim();
                    if (name.isEmpty() || amtStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RecurringPayment p = new RecurringPayment();
                    p.description = name;
                    p.amount = Math.round(Double.parseDouble(amtStr) * 100);
                    p.frequency = Frequency.MONTHLY;
                    p.isActive = true;
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, 1);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    p.nextDueDate = cal.getTimeInMillis();
                    vm.addPayment(p);
                    Toast.makeText(requireContext(), "Recurring payment added!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
