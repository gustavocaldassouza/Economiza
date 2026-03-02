package com.example.economiza.ui.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.domain.model.Category;
import com.example.economiza.ui.adapter.ManageCategoryAdapter;
import com.example.economiza.ui.viewmodel.CategoryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ManageCategoriesActivity extends BaseActivity
        implements ManageCategoryAdapter.Listener {

    private CategoryViewModel vm;
    private ManageCategoryAdapter adapter;

    // Preset colors for quick pick
    private static final String[] COLORS = {
            "#FF8A65", "#3D8BFF", "#FFD54F", "#81C784",
            "#CE93D8", "#F48FB1", "#00D084", "#8E97A8",
            "#EF5350", "#42A5F5", "#FFA726", "#26C6DA"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_categories_manage);
        FloatingActionButton fab = findViewById(R.id.fab_add_category);

        adapter = new ManageCategoryAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this,
                ((EconomizaApp) getApplication()).getViewModelFactory())
                .get(CategoryViewModel.class);

        vm.categories.observe(this, cats -> {
            if (cats != null)
                adapter.setCategories(cats);
        });

        fab.setOnClickListener(v -> showCategoryDialog(null));
    }

    @Override
    public void onEdit(Category category) {
        showCategoryDialog(category);
    }

    @Override
    public void onDelete(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Delete \"" + category.name + "\"? This may affect existing transactions.")
                .setPositiveButton("Delete", (d, w) -> vm.deleteCategory(category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCategoryDialog(Category existing) {
        boolean isEdit = existing != null;

        // Build dialog layout inline
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(56, 32, 56, 16);

        // Name field
        android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("Name (e.g. Groceries)");
        if (isEdit)
            etName.setText(existing.name);
        layout.addView(etName);

        // Color label
        android.widget.TextView colorLabel = new android.widget.TextView(this);
        colorLabel.setText("Pick a color:");
        colorLabel.setTextColor(Color.parseColor("#8E97A8"));
        colorLabel.setPadding(0, 24, 0, 8);
        layout.addView(colorLabel);

        // Color grid
        final String[] selectedColor = { isEdit && existing.colorHex != null ? existing.colorHex : "#3D8BFF" };
        android.widget.GridLayout colorGrid = new android.widget.GridLayout(this);
        colorGrid.setColumnCount(6);
        int swatchSize = (int) (40 * getResources().getDisplayMetrics().density);
        int swatchMargin = (int) (4 * getResources().getDisplayMetrics().density);

        android.view.View[] swatches = new android.view.View[COLORS.length];
        for (int i = 0; i < COLORS.length; i++) {
            final String hex = COLORS[i];
            android.view.View swatch = new android.view.View(this);

            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            bg.setColor(Color.parseColor(hex));
            swatch.setBackground(bg);

            android.widget.GridLayout.LayoutParams lp = new android.widget.GridLayout.LayoutParams();
            lp.width = swatchSize;
            lp.height = swatchSize;
            lp.setMargins(swatchMargin, swatchMargin, swatchMargin, swatchMargin);
            swatch.setLayoutParams(lp);

            swatches[i] = swatch;
            final int idx = i;
            swatch.setOnClickListener(v -> {
                selectedColor[0] = hex;
                for (android.view.View s : swatches)
                    s.setAlpha(0.45f);
                swatches[idx].setAlpha(1.0f);
                swatches[idx].setScaleX(1.2f);
                swatches[idx].setScaleY(1.2f);
            });
            colorGrid.addView(swatch);
        }
        layout.addView(colorGrid);

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Category" : "New Category")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Category cat = isEdit ? existing : new Category();
                    cat.name = name;
                    cat.colorHex = selectedColor[0];
                    cat.iconName = name.toLowerCase().replaceAll("\\s+", "_");
                    if (isEdit) {
                        vm.updateCategory(cat);
                    } else {
                        vm.addCategory(cat);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
