package com.example.economiza.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.ui.activities.AddTransactionActivity;
import com.example.economiza.ui.adapter.CategoryAdapter;
import com.example.economiza.ui.viewmodel.DashboardViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TextView txtBalance, txtIncome, txtExpenses;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtBalance = view.findViewById(R.id.txt_balance_value);
        txtIncome = view.findViewById(R.id.txt_income_value);
        txtExpenses = view.findViewById(R.id.txt_expense_value);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_smart_entry);

        // Use factory — critical: fixes crash from non-default constructor
        ViewModelProvider.Factory factory = ((EconomizaApp) requireActivity().getApplication()).getViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        // RecyclerView for top categories
        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCategories.setAdapter(new CategoryAdapter());
        }

        // FAB → Add Transaction
        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new Intent(requireActivity(), AddTransactionActivity.class)));
        }

        // Observe real income/expense data
        viewModel.totalIncome.observe(getViewLifecycleOwner(), income -> {
            if (income != null && txtIncome != null) {
                txtIncome.setText(String.format(Locale.getDefault(), "R$ %.2f", income / 100.0));
            }
        });

        viewModel.totalExpenses.observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && txtExpenses != null) {
                txtExpenses.setText(String.format(Locale.getDefault(), "R$ %.2f", expenses / 100.0));
            }
        });

        // Real net balance
        viewModel.netBalance.observe(getViewLifecycleOwner(), balance -> {
            if (balance != null && txtBalance != null) {
                double balanceReal = balance / 100.0;
                txtBalance.setText(String.format(Locale.getDefault(), "R$ %.2f", Math.abs(balanceReal)));
                txtBalance.setTextColor(balanceReal >= 0
                        ? Color.parseColor("#00D084") // green for positive
                        : Color.parseColor("#EF5350")); // red for negative
            }
        });
    }
}
