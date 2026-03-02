package com.example.economiza.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
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

/**
 * Adapter for the recurring-payments list.
 *
 * Supports:
 * <ul>
 * <li>Tap → {@link OnItemClickListener} (edit dialog)</li>
 * <li>Active switch → {@link OnToggleListener} (pause/resume)</li>
 * <li>{@link #getPaymentAt(int)} for swipe-to-delete</li>
 * </ul>
 */
public class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RecurringPayment payment);
    }

    public interface OnToggleListener {
        void onToggle(RecurringPayment payment);
    }

    private List<RecurringPayment> payments = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private OnItemClickListener clickListener;
    private OnToggleListener toggleListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }

    public void setOnToggleListener(OnToggleListener l) {
        this.toggleListener = l;
    }

    public void setPayments(List<RecurringPayment> payments) {
        this.payments = payments;
        notifyDataSetChanged();
    }

    /**
     * Returns the item at the given (adapter) position — used by swipe-to-delete.
     */
    public RecurringPayment getPaymentAt(int position) {
        return payments.get(position);
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
        h.frequency.setText(p.frequency != null ? formatFrequency(p.frequency) : "Monthly");
        h.due.setText("Next: " + sdf.format(new Date(p.nextDueDate)));
        h.amount.setText(String.format(Locale.getDefault(), "-$ %.2f", p.amount / 100.0));

        // Active switch: suppress listener while programmatically setting state
        h.switchActive.setOnCheckedChangeListener(null);
        h.switchActive.setChecked(p.isActive);
        h.switchActive.setOnCheckedChangeListener((btn, checked) -> {
            p.isActive = checked;
            if (toggleListener != null)
                toggleListener.onToggle(p);
        });

        // Card tap → edit
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null)
                clickListener.onItemClick(p);
        });

        // Dim inactive items visually
        h.itemView.setAlpha(p.isActive ? 1f : 0.5f);
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    private String formatFrequency(com.example.economiza.domain.model.Frequency f) {
        switch (f) {
            case DAILY:
                return "Daily";
            case WEEKLY:
                return "Weekly";
            case BIWEEKLY:
                return "Bi-weekly";
            case MONTHLY:
                return "Monthly";
            case YEARLY:
                return "Yearly";
            default:
                return f.name();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, frequency, due, amount;
        Switch switchActive;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.txt_recurring_name);
            frequency = v.findViewById(R.id.txt_recurring_frequency);
            due = v.findViewById(R.id.txt_recurring_due);
            amount = v.findViewById(R.id.txt_recurring_amount);
            switchActive = v.findViewById(R.id.switch_recurring_active);
        }
    }
}
