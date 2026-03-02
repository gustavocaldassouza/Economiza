package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;

public class AddTransactionUseCase {
    private final TransactionRepository repository;

    public AddTransactionUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public void execute(Transaction transaction) {
        // Here we could add validation logic
        if (transaction.amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        repository.insert(transaction);
    }
}
