package com.example.economiza.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.R;
import com.example.economiza.domain.model.RecurringPayment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.ViewHolder> {

    private List<RecurringPayment> payments = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public void setPayments(List<RecurringPayment> payments) {
        this.payments = payments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurring, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        RecurringPayment p = payments.get(position);
        h.name.setText(p.description != null ? p.description : "Payment");
        h.frequency.setText(p.frequency != null ? p.frequency.toString() : "Monthly");
        h.due.setText("Due: " + sdf.format(new Date(p.nextDueDate)));
        h.amount.setText(String.format(Locale.getDefault(), "-R$ %.2f", p.amount / 100.0));
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, frequency, due, amount;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.txt_recurring_name);
            frequency = v.findViewById(R.id.txt_recurring_frequency);
            due = v.findViewById(R.id.txt_recurring_due);
            amount = v.findViewById(R.id.txt_recurring_amount);
        }
    }
}
