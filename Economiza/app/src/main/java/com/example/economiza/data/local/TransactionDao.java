package com.example.economiza.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.economiza.domain.model.CategoryTotal;
import com.example.economiza.domain.model.DayTotal;
import com.example.economiza.domain.model.Transaction;

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

    // ── Analytics queries for charts ──────────────────────────────────────────

    /** Sum of expenses grouped by category — feeds the Pie Chart. */
    @Query("SELECT category_id, SUM(amount) AS total " +
            "FROM Transactions WHERE is_income = 0 " +
            "GROUP BY category_id ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getExpensesByCategory();

    /**
     * Sum of expenses per day bucket in a date range — feeds the Bar Chart.
     * day_bucket = date_millis / 86_400_000 (integer division gives day index).
     */
    @Query("SELECT (date / 86400000) AS day_bucket, SUM(amount) AS total " +
            "FROM Transactions WHERE is_income = 0 AND date BETWEEN :start AND :end " +
            "GROUP BY day_bucket ORDER BY day_bucket ASC")
    LiveData<List<DayTotal>> getDailyExpenses(long start, long end);
}
