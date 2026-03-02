package com.example.economiza.data.repository;

import androidx.lifecycle.LiveData;
import com.example.economiza.data.local.CategoryDao;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.repository.CategoryRepository;

import java.util.List;

public class CategoryRepositoryImpl implements CategoryRepository {
    private final CategoryDao categoryDao;

    public CategoryRepositoryImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public void insert(Category category) {
        categoryDao.insert(category);
    }

    @Override
    public void update(Category category) {
        categoryDao.update(category);
    }

    @Override
    public void delete(Category category) {
        categoryDao.delete(category);
    }

    @Override
    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    @Override
    public LiveData<String> getCategoryNameById(int id) {
        return categoryDao.getCategoryNameById(id);
    }
}
