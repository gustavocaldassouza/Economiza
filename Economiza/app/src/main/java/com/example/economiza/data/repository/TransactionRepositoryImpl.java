package com.example.economiza.data.repository;

import androidx.lifecycle.LiveData;

import com.example.economiza.data.local.TransactionDao;
import com.example.economiza.domain.model.CategoryTotal;
import com.example.economiza.domain.model.DayTotal;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;

import java.util.List;

public class TransactionRepositoryImpl implements TransactionRepository {
    private final TransactionDao dao;

    public TransactionRepositoryImpl(TransactionDao dao) {
        this.dao = dao;
    }

    @Override
    public void insert(Transaction t) {
        dao.insert(t);
    }

    @Override
    public void update(Transaction t) {
        dao.update(t);
    }

    @Override
    public void delete(Transaction t) {
        dao.delete(t);
    }

    @Override
    public LiveData<List<Transaction>> getAllTransactions() {
        return dao.getAllTransactions();
    }

    @Override
    public List<Transaction> getAllTransactionsSync() {
        return dao.getAllTransactionsSync();
    }

    @Override
    public LiveData<Long> getTotalExpenses() {
        return dao.getTotalExpenses();
    }

    @Override
    public LiveData<Long> getTotalIncome() {
        return dao.getTotalIncome();
    }

    @Override
    public LiveData<List<Transaction>> getTransactionsByCategory(int c) {
        return dao.getTransactionsByCategory(c);
    }

    @Override
    public LiveData<List<Transaction>> getTransactionsByDateRange(long s, long e) {
        return dao.getTransactionsByDateRange(s, e);
    }

    @Override
    public LiveData<List<Transaction>> getTransactionsByCategoryInDateRange(int c, long s, long e) {
        return dao.getTransactionsByCategoryInDateRange(c, s, e);
    }

    @Override
    public LiveData<List<CategoryTotal>> getExpensesByCategory() {
        return dao.getExpensesByCategory();
    }

    @Override
    public LiveData<List<DayTotal>> getDailyExpenses(long s, long e) {
        return dao.getDailyExpenses(s, e);
    }
}
