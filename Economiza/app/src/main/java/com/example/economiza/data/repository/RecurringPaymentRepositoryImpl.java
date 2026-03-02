package com.example.economiza.data.repository;

import androidx.lifecycle.LiveData;
import com.example.economiza.data.local.RecurringPaymentDao;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.repository.RecurringPaymentRepository;

import java.util.List;

public class RecurringPaymentRepositoryImpl implements RecurringPaymentRepository {
    private final RecurringPaymentDao recurringPaymentDao;

    public RecurringPaymentRepositoryImpl(RecurringPaymentDao recurringPaymentDao) {
        this.recurringPaymentDao = recurringPaymentDao;
    }

    @Override
    public void insert(RecurringPayment recurringPayment) {
        recurringPaymentDao.insert(recurringPayment);
    }

    @Override
    public void update(RecurringPayment recurringPayment) {
        recurringPaymentDao.update(recurringPayment);
    }

    @Override
    public void delete(RecurringPayment recurringPayment) {
        recurringPaymentDao.delete(recurringPayment);
    }

    @Override
    public LiveData<List<RecurringPayment>> getAllRecurringPayments() {
        return recurringPaymentDao.getAllRecurringPayments();
    }

    @Override
    public LiveData<List<RecurringPayment>> getActiveRecurringPayments() {
        return recurringPaymentDao.getActiveRecurringPayments();
    }
}
