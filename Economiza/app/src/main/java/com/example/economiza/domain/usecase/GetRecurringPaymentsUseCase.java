package com.example.economiza.domain.usecase;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.repository.RecurringPaymentRepository;
import java.util.List;

public class GetRecurringPaymentsUseCase {
    private final RecurringPaymentRepository repository;

    public GetRecurringPaymentsUseCase(RecurringPaymentRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<RecurringPayment>> execute() {
        return repository.getActiveRecurringPayments();
    }
}
