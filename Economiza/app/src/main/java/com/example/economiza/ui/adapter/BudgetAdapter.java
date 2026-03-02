package com.example.economiza.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.R;
import com.example.economiza.domain.model.BudgetListItem;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {
    public interface OnBudgetClickListener {
        void onBudgetClick(BudgetListItem budget);
    }

    private List<BudgetListItem> budgets = new ArrayList<>();
    private OnBudgetClickListener listener;

    public void setOnBudgetClickListener(OnBudgetClickListener listener) {
        this.listener = listener;
    }

    public void setBudgets(List<BudgetListItem> budgets) {
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
        BudgetListItem item = budgets.get(position);
        h.category.setText(item.categoryName != null ? item.categoryName : "Category " + item.budget.categoryId);

        double limit = item.budget.monthlyLimit / 100.0;
        double spent = item.currentSpending / 100.0;
        double remaining = limit - spent;

        h.spent.setText(String.format(Locale.getDefault(), "$%.0f / $%.0f", spent, limit));

        int progress = limit > 0 ? (int) ((spent / limit) * 100) : 0;
        h.progress.setProgress(Math.min(progress, 100));

        // Reset colors
        h.progress.setIndicatorColor(h.itemView.getContext().getColor(R.color.primary_blue));
        h.remaining.setTextColor(h.itemView.getContext().getColor(R.color.accent_green));

        if (progress >= 90) {
            h.progress.setIndicatorColor(0xFFEF5350); // Red
            h.remaining.setTextColor(0xFFEF5350);
        }

        if (remaining >= 0) {
            h.remaining.setText(String.format(Locale.getDefault(), "$%.2f remaining", remaining));
        } else {
            h.remaining.setText(String.format(Locale.getDefault(), "$%.2f over budget", -remaining));
            h.remaining.setTextColor(0xFFEF5350);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBudgetClick(item);
            }
        });
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
