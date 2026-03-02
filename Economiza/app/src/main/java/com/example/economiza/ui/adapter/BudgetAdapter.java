package com.example.economiza.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.R;
import com.example.economiza.domain.model.Budget;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private List<Budget> budgets = new ArrayList<>();

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
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
        h.category.setText("Category #" + b.categoryId);

        double limit = b.monthlyLimit / 100.0;
        double spent = b.spentSoFar / 100.0;
        double remaining = limit - spent;

        h.spent.setText(String.format(Locale.getDefault(), "R$%.0f / R$%.0f", spent, limit));

        int progress = limit > 0 ? (int) ((spent / limit) * 100) : 0;
        h.progress.setProgress(Math.min(progress, 100));
        if (progress >= 90) {
            h.progress.setIndicatorColor(0xFFEF5350);
            h.remaining.setTextColor(0xFFEF5350);
        }

        if (remaining >= 0) {
            h.remaining.setText(String.format(Locale.getDefault(), "R$%.2f remaining", remaining));
        } else {
            h.remaining.setText(String.format(Locale.getDefault(), "R$%.2f over budget", -remaining));
        }
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView category, spent, remaining;
        LinearProgressIndicator progress;

        ViewHolder(View v) {
            super(v);
            category = v.findViewById(R.id.txt_budget_category);
            spent = v.findViewById(R.id.txt_budget_spent);
            remaining = v.findViewById(R.id.txt_budget_remaining);
            progress = v.findViewById(R.id.progress_budget);
        }
    }
}
