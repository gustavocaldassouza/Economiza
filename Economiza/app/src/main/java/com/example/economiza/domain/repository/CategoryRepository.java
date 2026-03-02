package com.example.economiza.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.economiza.domain.model.Category;

import java.util.List;

public interface CategoryRepository {
    void insert(Category category);

    void update(Category category);

    void delete(Category category);

    LiveData<List<Category>> getAllCategories();

    List<Category> getAllCategoriesSync();

    int getCategoryCount();
}
