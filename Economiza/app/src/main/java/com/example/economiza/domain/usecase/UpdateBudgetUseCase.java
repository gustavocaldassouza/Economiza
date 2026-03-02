package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.repository.BudgetRepository;

public class UpdateBudgetUseCase {
    private final BudgetRepository repository;

    public UpdateBudgetUseCase(BudgetRepository repository) {
        this.repository = repository;
    }

    public void execute(Budget budget) {
        repository.update(budget);
    }
}
