package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.repository.RecurringPaymentRepository;

public class UpdateRecurringPaymentUseCase {
    private final RecurringPaymentRepository repository;

    public UpdateRecurringPaymentUseCase(RecurringPaymentRepository repository) {
        this.repository = repository;
    }

    public void execute(RecurringPayment payment) {
        repository.update(payment);
    }
}
