package com.example.economiza.domain.repository;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.model.RecurringPayment;
import java.util.List;

public interface RecurringPaymentRepository {
    void insert(RecurringPayment recurringPayment);

    void update(RecurringPayment recurringPayment);

    void delete(RecurringPayment recurringPayment);

    LiveData<List<RecurringPayment>> getAllRecurringPayments();

    LiveData<List<RecurringPayment>> getActiveRecurringPayments();
}
