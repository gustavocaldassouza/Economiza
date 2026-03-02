package com.example.economiza.domain.usecase;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.repository.TransactionRepository;

public class GetTotalExpensesUseCase {
    private final TransactionRepository repository;

    public GetTotalExpensesUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public LiveData<Long> execute() {
        return repository.getTotalExpenses();
    }
}
