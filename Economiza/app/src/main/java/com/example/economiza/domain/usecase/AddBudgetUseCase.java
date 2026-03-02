package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.repository.BudgetRepository;

public class AddBudgetUseCase {
    private final BudgetRepository repository;

    public AddBudgetUseCase(BudgetRepository repository) {
        this.repository = repository;
    }

    public void execute(Budget budget) {
        repository.insert(budget);
    }
}
