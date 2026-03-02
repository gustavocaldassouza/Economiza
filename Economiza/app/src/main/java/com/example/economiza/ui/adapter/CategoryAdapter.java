package com.example.economiza.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.R;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.model.Transaction;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories = new ArrayList<>();

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.txtName.setText(category.name);
        // Mocking some data for the visuals in the photos
        if (category.name.contains("Food")) {
            holder.txtAmount.setText("-$450.00");
            holder.txtCount.setText("12 Transactions");
            holder.progress.setProgress(70);
            holder.icon.setImageResource(R.drawable.ic_folder); // Use real category icons in production
        } else if (category.name.contains("Transport")) {
            holder.txtAmount.setText("-$120.50");
            holder.txtCount.setText("5 Transactions");
            holder.progress.setProgress(30);
            holder.icon.setImageResource(R.drawable.ic_folder);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView txtName, txtCount, txtAmount;
        LinearProgressIndicator progress;

        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.img_cat_icon);
            txtName = view.findViewById(R.id.txt_cat_name);
            txtCount = view.findViewById(R.id.txt_cat_count);
            txtAmount = view.findViewById(R.id.txt_cat_amount);
            progress = view.findViewById(R.id.progress_category);
        }
    }
}
