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
import com.example.economiza.domain.model.Budget;
import com.example.economiza.ui.adapter.BudgetAdapter;
import com.example.economiza.ui.viewmodel.BudgetViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class BudgetFragment extends Fragment {

    private BudgetViewModel vm;
    private BudgetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_budgets);
        MaterialButton btnAdd = view.findViewById(R.id.btn_add_budget);

        adapter = new BudgetAdapter();
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this,
                ((EconomizaApp) requireActivity().getApplication()).getViewModelFactory())
                .get(BudgetViewModel.class);

        vm.budgets.observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null)
                adapter.setBudgets(budgets);
        });

        btnAdd.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void showAddBudgetDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(android.R.layout.simple_list_item_1, null);

        // Build inline dialog with two fields
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        android.widget.EditText etCategoryId = new android.widget.EditText(requireContext());
        etCategoryId.setHint("Category ID");
        etCategoryId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        android.widget.EditText etLimit = new android.widget.EditText(requireContext());
        etLimit.setHint("Monthly limit (e.g. 500.00)");
        etLimit.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        layout.addView(etCategoryId);
        layout.addView(etLimit);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set Budget")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String catStr = etCategoryId.getText().toString().trim();
                    String limitStr = etLimit.getText().toString().trim();
                    if (catStr.isEmpty() || limitStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Budget budget = new Budget();
                    budget.categoryId = Integer.parseInt(catStr);
                    budget.monthlyLimit = Math.round(Double.parseDouble(limitStr) * 100);
                    budget.spentSoFar = 0;
                    vm.addBudget(budget);
                    Toast.makeText(requireContext(), "Budget set!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
