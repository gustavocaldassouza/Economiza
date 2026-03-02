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
import com.example.economiza.domain.model.Category;
import com.example.economiza.ui.adapter.BudgetAdapter;
import com.example.economiza.ui.viewmodel.BudgetViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    private BudgetViewModel vm;
    private BudgetAdapter adapter;
    // Cache category list for the picker dialog
    private List<Category> cachedCategories = new ArrayList<>();

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

        // Observe budgets and pass the category map to the adapter
        vm.budgets.observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null)
                adapter.setBudgets(budgets, cachedCategories);
        });

        // Keep categories cached for the picker dialog
        vm.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                cachedCategories = categories;
                // Refresh adapter with updated names if budgets already loaded
                List<Budget> current = vm.budgets.getValue();
                if (current != null)
                    adapter.setBudgets(current, categories);
            }
        });

        btnAdd.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void showAddBudgetDialog() {
        if (cachedCategories.isEmpty()) {
            Toast.makeText(requireContext(), "No categories yet. Add a category first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build names array for the picker
        String[] names = new String[cachedCategories.size()];
        for (int i = 0; i < cachedCategories.size(); i++) {
            names[i] = cachedCategories.get(i).name;
        }

        final int[] selectedIndex = { 0 };

        // First dialog: pick a category
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Category")
                .setSingleChoiceItems(names, 0, (d, which) -> selectedIndex[0] = which)
                .setPositiveButton("Next", (d, w) -> showLimitDialog(cachedCategories.get(selectedIndex[0])))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLimitDialog(Category category) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 0);

        android.widget.EditText etLimit = new android.widget.EditText(requireContext());
        etLimit.setHint("Monthly limit (e.g. 500.00)");
        etLimit.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etLimit);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set limit for \"" + category.name + "\"")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String limitStr = etLimit.getText().toString().trim();
                    if (limitStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Enter a limit value", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Budget budget = new Budget();
                    budget.categoryId = category.id;
                    budget.monthlyLimit = Math.round(Double.parseDouble(limitStr) * 100);
                    budget.spentSoFar = 0;
                    vm.addBudget(budget);
                    Toast.makeText(requireContext(), "Budget set for " + category.name + "!", Toast.LENGTH_SHORT)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
