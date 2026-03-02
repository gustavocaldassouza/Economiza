package com.example.economiza.domain.usecase;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.repository.BudgetRepository;
import java.util.List;

public class GetBudgetsUseCase {
    private final BudgetRepository repository;

    public GetBudgetsUseCase(BudgetRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Budget>> execute() {
        return repository.getAllBudgets();
    }
}
