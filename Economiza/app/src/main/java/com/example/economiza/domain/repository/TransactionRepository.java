package com.example.economiza.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.economiza.domain.model.CategoryTotal;
import com.example.economiza.domain.model.DayTotal;
import com.example.economiza.domain.model.Transaction;

import java.util.List;

public interface TransactionRepository {
    void insert(Transaction transaction);

    void update(Transaction transaction);

    void delete(Transaction transaction);

    LiveData<List<Transaction>> getAllTransactions();

    List<Transaction> getAllTransactionsSync();

    LiveData<Long> getTotalExpenses();

    LiveData<Long> getTotalIncome();

    LiveData<List<Transaction>> getTransactionsByCategory(int catId);

    LiveData<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate);

    LiveData<List<Transaction>> getTransactionsByCategoryInDateRange(int catId, long start, long end);

    // Analytics
    LiveData<List<CategoryTotal>> getExpensesByCategory();

    LiveData<List<DayTotal>> getDailyExpenses(long start, long end);
}
