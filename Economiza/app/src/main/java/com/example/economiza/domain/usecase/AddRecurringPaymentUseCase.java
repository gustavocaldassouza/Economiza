package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.repository.RecurringPaymentRepository;

public class AddRecurringPaymentUseCase {
    private final RecurringPaymentRepository repository;

    public AddRecurringPaymentUseCase(RecurringPaymentRepository repository) {
        this.repository = repository;
    }

    public void execute(RecurringPayment payment) {
        repository.insert(payment);
    }
}
