package com.example.economiza;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    Flow<List<Transaction>> getAllTransactions();

    @Query("SELECT SUM(amount) FROM transactions WHERE is_income = 0")
    Flow<Long> getTotalExpenses();

    @Query("SELECT SUM(amount) FROM transactions WHERE is_income = 1")
    Flow<Long> getTotalIncome();

    @Query("SELECT * FROM transactions WHERE category_id = :catId ORDER BY date DESC")
    Flow<List<Transaction>> getTransactionsByCategory(int catId);

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    Flow<List<Transaction>> getTransactionsByDateRange(long startDate, long endDate);

    @Query("SELECT * FROM transactions WHERE category_id = :catId AND date BETWEEN :start AND :end ORDER BY date DESC")
    Flow<List<Transaction>> getTransactionsByCategoryInDateRange(int catId, long start, long end);
}
