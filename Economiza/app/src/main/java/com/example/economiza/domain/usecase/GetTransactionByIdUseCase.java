package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;

public class GetTransactionByIdUseCase {
    private final TransactionRepository repository;

    public GetTransactionByIdUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction execute(int id) {
        return repository.findById(id);
    }
}
