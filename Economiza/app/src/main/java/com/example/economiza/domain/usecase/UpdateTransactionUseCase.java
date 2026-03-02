package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;

public class UpdateTransactionUseCase {
    private final TransactionRepository repository;

    public UpdateTransactionUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public void execute(Transaction transaction) {
        if (transaction.amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        repository.update(transaction);
    }
}
