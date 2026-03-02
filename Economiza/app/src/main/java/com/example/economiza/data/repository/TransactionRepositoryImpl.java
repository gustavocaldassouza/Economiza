package com.example.economiza.data.repository;

import androidx.lifecycle.LiveData;
import com.example.economiza.data.local.TransactionDao;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;

import java.util.List;

public class TransactionRepositoryImpl implements TransactionRepository {
    private final TransactionDao transactionDao;

    public TransactionRepositoryImpl(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    @Override
    public void insert(Transaction transaction) {
        transactionDao.insert(transaction);
    }

    @Override
    public void update(Transaction transaction) {
        transactionDao.update(transaction);
    }

    @Override
    public void delete(Transaction transaction) {
        transactionDao.delete(transaction);
    }

    @Override
    public LiveData<List<Transaction>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    @Override
    public List<Transaction> getAllTransactionsSync() {
        return transactionDao.getAllTransactionsSync();
    }

    @Override
    public LiveData<Long> getTotalExpenses() {
        return transactionDao.getTotalExpenses();
    }

    @Override
    public LiveData<Long> getTotalIncome() {
        return transactionDao.getTotalIncome();
    }

    @Override
    public LiveData<List<Transaction>> getTransactionsByCategory(int catId) {
        return transactionDao.getTransactionsByCategory(catId);
    }

    @Override
    public LiveData<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate) {
        return transactionDao.getTransactionsByDateRange(startDate, endDate);
    }

    @Override
    public LiveData<List<Transaction>> getTransactionsByCategoryInDateRange(int catId, long start, long end) {
        return transactionDao.getTransactionsByCategoryInDateRange(catId, start, end);
    }
}
