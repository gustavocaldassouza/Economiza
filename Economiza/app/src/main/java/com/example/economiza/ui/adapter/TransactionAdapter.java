package com.example.economiza.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.R;
import com.example.economiza.domain.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

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
        h.description.setText(t.description != null && !t.description.isEmpty()
                ? t.description
                : "Transaction");
        h.date.setText(sdf.format(new Date(t.timestamp)));

        double amount = t.amount / 100.0;
        if (t.isIncome) {
            h.amount.setText(String.format(Locale.getDefault(), "+R$ %.2f", amount));
            h.amount.setTextColor(0xFF00D084); // green
        } else {
            h.amount.setText(String.format(Locale.getDefault(), "-R$ %.2f", amount));
            h.amount.setTextColor(0xFFEF5350); // red
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView description, date, amount;

        ViewHolder(View v) {
            super(v);
            description = v.findViewById(R.id.txt_tx_description);
            date = v.findViewById(R.id.txt_tx_date);
            amount = v.findViewById(R.id.txt_tx_amount);
        }
    }
}
