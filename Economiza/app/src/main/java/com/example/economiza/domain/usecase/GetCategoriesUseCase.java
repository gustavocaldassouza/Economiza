package com.example.economiza.domain.usecase;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.repository.CategoryRepository;
import java.util.List;

public class GetCategoriesUseCase {
    private final CategoryRepository repository;

    public GetCategoriesUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Category>> execute() {
        return repository.getAllCategories();
    }

    public List<Category> executeSync() {
        return repository.getAllCategoriesSync();
    }
}
