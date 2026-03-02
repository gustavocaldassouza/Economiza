package com.example.economiza.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.R;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.CategoryRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction t);
    }

    private OnTransactionClickListener listener;
    private final CategoryRepository categoryRepository;
    private List<Transaction> transactions = new ArrayList<>();
    private final Map<Integer, String> categoryCache = new HashMap<>();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public TransactionAdapter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void setListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Transaction t = transactions.get(position);

        h.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onTransactionClick(t);
        });

        h.description.setText(t.description != null && !t.description.isEmpty()
                ? t.description
                : "Transaction");
        h.date.setText(sdf.format(new Date(t.timestamp)));

        double amount = t.amount / 100.0;
        if (t.isIncome) {
            h.amount.setText(String.format(Locale.getDefault(), "+$ %.2f", amount));
            h.amount.setTextColor(0xFF00D084); // green
        } else {
            h.amount.setText(String.format(Locale.getDefault(), "-$ %.2f", amount));
            h.amount.setTextColor(0xFFEF5350); // red
        }

        // Handle category chip
        if (categoryCache.containsKey(t.categoryId)) {
            h.category.setText(categoryCache.get(t.categoryId));
        } else {
            h.category.setText("...");
            executor.execute(() -> {
                String name = categoryRepository.getCategoryNameByIdSync(t.categoryId);
                if (name != null) {
                    categoryCache.put(t.categoryId, name);
                    h.itemView.post(() -> notifyItemChanged(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView description, date, amount, category;

        ViewHolder(View v) {
            super(v);
            description = v.findViewById(R.id.txt_tx_description);
            date = v.findViewById(R.id.txt_tx_date);
            amount = v.findViewById(R.id.txt_tx_amount);
            category = v.findViewById(R.id.txt_tx_category);
        }
    }
}
