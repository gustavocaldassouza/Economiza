package com.example.economiza.ui.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.R;
import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.model.Category;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private List<Budget> budgets = new ArrayList<>();
    private Map<Integer, Category> categoryMap = new HashMap<>();

    public void setBudgets(List<Budget> budgets, List<Category> categories) {
        this.budgets = budgets;
        // Rebuild map keyed by category id for O(1) lookup
        categoryMap = new HashMap<>();
        if (categories != null) {
            for (Category c : categories) {
                categoryMap.put(c.id, c);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Budget b = budgets.get(position);

        // --- Category name and color ---
        Category cat = categoryMap.get(b.categoryId);
        if (cat != null) {
            h.category.setText(cat.name);
            try {
                GradientDrawable bg = (GradientDrawable) h.colorSwatch.getBackground().mutate();
                bg.setColor(Color.parseColor(cat.colorHex));
            } catch (Exception e) {
                // fallback gray if colorHex is invalid
            }
        } else {
            h.category.setText("Category #" + b.categoryId);
        }

        // --- Amounts ---
        double limit = b.monthlyLimit / 100.0;
        double spent = b.spentSoFar / 100.0;
        double remaining = limit - spent;

        h.spent.setText(String.format(Locale.getDefault(), "R$%.0f / R$%.0f", spent, limit));

        // --- Progress bar ---
        int progress = limit > 0 ? (int) ((spent / limit) * 100) : 0;
        h.progress.setProgress(Math.min(progress, 100));

        // --- Warning / Over budget states ---
        if (progress >= 100) {
            // Over budget — red bar
            h.progress.setIndicatorColor(0xFFEF5350);
            h.remaining.setTextColor(0xFFEF5350);
            h.remaining.setText(String.format(Locale.getDefault(), "R$%.2f over budget!", -remaining));
            h.warning.setVisibility(View.VISIBLE);
            h.warning.setText("⚠ Over budget!");
            h.warning.setTextColor(0xFFEF5350);
        } else if (progress >= 80) {
            // Approaching limit — orange warning
            h.progress.setIndicatorColor(0xFFFFA726);
            h.remaining.setTextColor(0xFFFFA726);
            h.remaining.setText(String.format(Locale.getDefault(), "R$%.2f remaining", remaining));
            h.warning.setVisibility(View.VISIBLE);
            h.warning.setText("⚠ Approaching limit!");
            h.warning.setTextColor(0xFFFFA726);
        } else {
            // Healthy state
            h.warning.setVisibility(View.GONE);
            h.remaining.setTextColor(0xFF00D084);
            h.remaining.setText(String.format(Locale.getDefault(), "R$%.2f remaining", remaining));
        }
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View colorSwatch;
        TextView category, spent, remaining, warning;
        LinearProgressIndicator progress;

        ViewHolder(View v) {
            super(v);
            colorSwatch = v.findViewById(R.id.view_budget_color);
            category = v.findViewById(R.id.txt_budget_category);
            spent = v.findViewById(R.id.txt_budget_spent);
            remaining = v.findViewById(R.id.txt_budget_remaining);
            warning = v.findViewById(R.id.txt_budget_warning);
            progress = v.findViewById(R.id.progress_budget);
        }
    }
}
