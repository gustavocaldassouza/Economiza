package com.example.economiza.domain.usecase;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;
import java.util.List;

public class GetTransactionsUseCase {
    private final TransactionRepository repository;

    public GetTransactionsUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Transaction>> execute() {
        return repository.getAllTransactions();
    }
}
