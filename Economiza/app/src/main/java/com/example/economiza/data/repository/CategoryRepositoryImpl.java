package com.example.economiza.data.repository;

import androidx.lifecycle.LiveData;

import com.example.economiza.data.local.CategoryDao;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.repository.CategoryRepository;

import java.util.List;

public class CategoryRepositoryImpl implements CategoryRepository {
    private final CategoryDao dao;

    public CategoryRepositoryImpl(CategoryDao dao) {
        this.dao = dao;
    }

    @Override
    public void insert(Category category) {
        dao.insert(category);
    }

    @Override
    public void update(Category category) {
        dao.update(category);
    }

    @Override
    public void delete(Category category) {
        dao.delete(category);
    }

    @Override
    public LiveData<List<Category>> getAllCategories() {
        return dao.getAllCategories();
    }

    @Override
    public List<Category> getAllCategoriesSync() {
        return dao.getAllCategoriesSync();
    }

    @Override
    public int getCategoryCount() {
        return dao.getCategoryCount();
    }
}
