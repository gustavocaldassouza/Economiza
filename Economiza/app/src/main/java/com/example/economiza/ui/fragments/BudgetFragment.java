package com.example.economiza.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.example.economiza.domain.model.BudgetListItem;
import com.example.economiza.ui.adapter.BudgetAdapter;
import com.example.economiza.ui.viewmodel.BudgetViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.example.economiza.domain.model.Category;
import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    private BudgetViewModel vm;
    private BudgetAdapter adapter;
    private List<Category> allCategories = new ArrayList<>();

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
        TextView txtEmpty = view.findViewById(R.id.txt_empty_budgets);
        MaterialButton btnAdd = view.findViewById(R.id.btn_add_budget);

        adapter = new BudgetAdapter();
        adapter.setOnBudgetClickListener(item -> showBudgetOptionsDialog(item));
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this,
                ((EconomizaApp) requireActivity().getApplication()).getViewModelFactory())
                .get(BudgetViewModel.class);

        vm.budgets.observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null) {
                adapter.setBudgets(budgets);
                if (budgets.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                }
            }
        });

        vm.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories != null)
                allCategories = categories;
        });

        btnAdd.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void showAddBudgetDialog() {
        if (allCategories.isEmpty()) {
            Toast.makeText(requireContext(), "Create some categories first", Toast.LENGTH_SHORT).show();
            return;
        }

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 32, 64, 0);

        // Category Spinner
        android.widget.TextView txtLabelCat = new android.widget.TextView(requireContext());
        txtLabelCat.setText("Select Category");
        txtLabelCat.setPadding(0, 0, 0, 8);

        Spinner spinner = new Spinner(requireContext());
        List<String> names = new ArrayList<>();
        for (Category c : allCategories)
            names.add(c.name);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, names);
        spinner.setAdapter(spinAdapter);

        // Limit Field
        TextInputLayout tilLimit = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilLimit.setHint("Monthly Limit ($)");
        // tilLimit.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINED);
        tilLimit.setBoxCornerRadii(16f, 16f, 16f, 16f);
        TextInputEditText etLimit = new TextInputEditText(tilLimit.getContext());
        etLimit.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tilLimit.addView(etLimit);

        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 32, 0, 0);

        layout.addView(txtLabelCat);
        layout.addView(spinner);
        layout.addView(tilLimit, lp);

        new AlertDialog.Builder(requireContext())
                .setTitle("Set New Budget")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String limitStr = etLimit.getText().toString().trim();
                    if (limitStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Enter a limit", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int selectedPos = spinner.getSelectedItemPosition();
                    Category selectedCat = allCategories.get(selectedPos);

                    Budget budget = new Budget();
                    budget.categoryId = selectedCat.id;
                    budget.monthlyLimit = Math.round(Double.parseDouble(limitStr) * 100);
                    budget.spentSoFar = 0; // Legacy field, logic now uses dynamic calc

                    vm.addBudget(budget);
                    Toast.makeText(requireContext(), "Budget created for " + selectedCat.name, Toast.LENGTH_SHORT)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showBudgetOptionsDialog(BudgetListItem item) {
        String[] options = { "Edit", "Delete" };
        new AlertDialog.Builder(requireContext())
                .setTitle(item.categoryName != null ? item.categoryName : "Budget Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditBudgetDialog(item);
                    } else {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Delete Budget")
                                .setMessage("Are you sure you want to delete this budget?")
                                .setPositiveButton("Delete", (d, w) -> {
                                    vm.deleteBudget(item.budget);
                                    Toast.makeText(requireContext(), "Budget deleted", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                })
                .show();
    }

    private void showEditBudgetDialog(BudgetListItem item) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 32, 64, 0);

        TextInputLayout tilLimit = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        tilLimit.setHint("Monthly Limit ($)");
        tilLimit.setBoxCornerRadii(16f, 16f, 16f, 16f);
        TextInputEditText etLimit = new TextInputEditText(tilLimit.getContext());
        etLimit.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etLimit.setText(String.valueOf(item.budget.monthlyLimit / 100.0));
        tilLimit.addView(etLimit);

        layout.addView(tilLimit);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Budget: " + item.categoryName)
                .setView(layout)
                .setPositiveButton("Update", (d, w) -> {
                    String limitStr = etLimit.getText().toString().trim();
                    if (limitStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Enter a limit", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    item.budget.monthlyLimit = Math.round(Double.parseDouble(limitStr) * 100);
                    vm.updateBudget(item.budget);
                    Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
