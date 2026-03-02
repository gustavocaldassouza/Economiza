package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;

public class DeleteTransactionUseCase {
    private final TransactionRepository repository;

    public DeleteTransactionUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public void execute(Transaction transaction) {
        repository.delete(transaction);
    }
}
