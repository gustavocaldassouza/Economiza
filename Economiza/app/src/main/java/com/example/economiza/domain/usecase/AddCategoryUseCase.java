package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.repository.CategoryRepository;

public class AddCategoryUseCase {
    private final CategoryRepository repository;

    public AddCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public void execute(Category category) {
        repository.insert(category);
    }
}
