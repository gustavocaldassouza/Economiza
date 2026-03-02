package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.usecase.AddCategoryUseCase;
import com.example.economiza.domain.usecase.DeleteCategoryUseCase;
import com.example.economiza.domain.usecase.GetCategoriesUseCase;

import java.util.List;

public class CategoryViewModel extends ViewModel {
    private final GetCategoriesUseCase getCategories;
    private final AddCategoryUseCase addCategory;
    private final DeleteCategoryUseCase deleteCategory;
    public final LiveData<List<Category>> categories;

    public CategoryViewModel(GetCategoriesUseCase getCategories,
            AddCategoryUseCase addCategory,
            DeleteCategoryUseCase deleteCategory) {
        this.getCategories = getCategories;
        this.addCategory = addCategory;
        this.deleteCategory = deleteCategory;
        this.categories = getCategories.execute();
    }

    public void addCategory(Category category) {
        new Thread(() -> addCategory.execute(category)).start();
    }

    public void deleteCategory(Category category) {
        new Thread(() -> deleteCategory.execute(category)).start();
    }
}
