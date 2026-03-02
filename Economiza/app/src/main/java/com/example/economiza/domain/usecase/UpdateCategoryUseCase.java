package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.repository.CategoryRepository;

public class UpdateCategoryUseCase {
    private final CategoryRepository repository;

    public UpdateCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public void execute(Category category) {
        repository.update(category);
    }
}
