package com.example.economiza.domain.usecase;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.repository.TransactionRepository;

public class GetTotalIncomeUseCase {
    private final TransactionRepository repository;

    public GetTotalIncomeUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public LiveData<Long> execute() {
        return repository.getTotalIncome();
    }
}
