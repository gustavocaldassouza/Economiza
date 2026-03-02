package com.example.economiza.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.economiza.domain.model.Transaction;

import androidx.lifecycle.LiveData;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM Transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM Transactions ORDER BY date DESC")
    List<Transaction> getAllTransactionsSync();

    @Query("SELECT SUM(amount) FROM Transactions WHERE is_income = 0")
    LiveData<Long> getTotalExpenses();

    @Query("SELECT SUM(amount) FROM Transactions WHERE is_income = 1")
    LiveData<Long> getTotalIncome();

    @Query("SELECT * FROM Transactions WHERE category_id = :catId ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(int catId);

    @Query("SELECT * FROM Transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate);

    @Query("SELECT * FROM Transactions WHERE category_id = :catId AND date BETWEEN :start AND :end ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategoryInDateRange(int catId, long start, long end);
}
