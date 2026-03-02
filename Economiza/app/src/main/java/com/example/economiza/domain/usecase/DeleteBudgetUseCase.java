package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.repository.BudgetRepository;

public class DeleteBudgetUseCase {
    private final BudgetRepository repository;

    public DeleteBudgetUseCase(BudgetRepository repository) {
        this.repository = repository;
    }

    public void execute(Budget budget) {
        repository.delete(budget);
    }
}
