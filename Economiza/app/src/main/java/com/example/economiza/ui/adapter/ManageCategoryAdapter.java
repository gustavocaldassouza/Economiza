package com.example.economiza.ui.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.R;
import com.example.economiza.domain.model.Category;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoryAdapter extends RecyclerView.Adapter<ManageCategoryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(Category category);

        void onDelete(Category category);
    }

    private List<Category> categories = new ArrayList<>();
    private final Listener listener;

    public ManageCategoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_manage, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Category cat = categories.get(position);
        h.name.setText(cat.name);
        h.iconLabel.setText(cat.iconName != null ? cat.iconName : "");

        // Tint the circle swatch with the category color
        GradientDrawable drawable = (GradientDrawable) h.colorSwatch.getBackground().mutate();
        try {
            drawable.setColor(Color.parseColor(cat.colorHex));
        } catch (Exception e) {
            drawable.setColor(Color.GRAY);
        }

        h.editBtn.setOnClickListener(v -> listener.onEdit(cat));
        h.deleteBtn.setOnClickListener(v -> listener.onDelete(cat));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View colorSwatch;
        TextView name, iconLabel;
        ImageView editBtn, deleteBtn;

        ViewHolder(View v) {
            super(v);
            colorSwatch = v.findViewById(R.id.view_cat_color);
            name = v.findViewById(R.id.txt_cat_name);
            iconLabel = v.findViewById(R.id.txt_cat_icon);
            editBtn = v.findViewById(R.id.btn_cat_edit);
            deleteBtn = v.findViewById(R.id.btn_cat_delete);
        }
    }
}
