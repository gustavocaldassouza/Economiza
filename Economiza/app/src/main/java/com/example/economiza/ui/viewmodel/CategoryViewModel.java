package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.usecase.AddCategoryUseCase;
import com.example.economiza.domain.usecase.DeleteCategoryUseCase;
import com.example.economiza.domain.usecase.GetCategoriesUseCase;
import com.example.economiza.domain.usecase.UpdateCategoryUseCase;

import java.util.List;

public class CategoryViewModel extends ViewModel {
    private final AddCategoryUseCase addCategory;
    private final UpdateCategoryUseCase updateCategory;
    private final DeleteCategoryUseCase deleteCategory;
    public final LiveData<List<Category>> categories;

    public CategoryViewModel(GetCategoriesUseCase getCategories,
            AddCategoryUseCase addCategory,
            UpdateCategoryUseCase updateCategory,
            DeleteCategoryUseCase deleteCategory) {
        this.addCategory = addCategory;
        this.updateCategory = updateCategory;
        this.deleteCategory = deleteCategory;
        this.categories = getCategories.execute();
    }

    public void addCategory(Category category) {
        new Thread(() -> addCategory.execute(category)).start();
    }

    public void updateCategory(Category category) {
        new Thread(() -> updateCategory.execute(category)).start();
    }

    public void deleteCategory(Category category) {
        new Thread(() -> deleteCategory.execute(category)).start();
    }
}
