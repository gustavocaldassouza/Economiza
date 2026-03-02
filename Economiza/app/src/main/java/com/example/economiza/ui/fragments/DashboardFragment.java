package com.example.economiza.ui.fragments;

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

import com.example.economiza.R;
import com.example.economiza.domain.model.Category;
import com.example.economiza.ui.adapter.CategoryAdapter;
import com.example.economiza.ui.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TextView txtBalance;
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        txtBalance = view.findViewById(R.id.txt_balance_value);
        rvCategories = view.findViewById(R.id.rv_categories);

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryAdapter();
        rvCategories.setAdapter(categoryAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Setup mock data to match photos
        List<Category> mockCategories = new ArrayList<>();
        Category c1 = new Category();
        c1.name = "Food & Dining";
        Category c2 = new Category();
        c2.name = "Transport";
        Category c3 = new Category();
        c3.name = "Utilities";
        mockCategories.add(c1);
        mockCategories.add(c2);
        mockCategories.add(c3);
        categoryAdapter.setCategories(mockCategories);

        viewModel.totalExpenses.observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null) {
                // Update with real data if available
            }
        });
    }
}
