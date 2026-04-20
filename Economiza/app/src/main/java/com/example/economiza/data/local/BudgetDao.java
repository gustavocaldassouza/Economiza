package com.example.economiza.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.economiza.domain.model.Budget;

import androidx.lifecycle.LiveData;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT b.*, c.name as categoryName, (" +
            "SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transactions t " +
            "WHERE t.category_id = b.category_id " +
            "AND t.is_income = 0 " +
            "AND t.date BETWEEN :start AND :end" +
            ") as currentSpending " +
            "FROM budgets b " +
            "JOIN Categories c ON b.category_id = c.id")
    LiveData<List<com.example.economiza.domain.model.BudgetListItem>> getAllBudgetsWithSpending(long start, long end);

    @Query("SELECT b.*, c.name as categoryName, (" +
            "SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transactions t " +
            "WHERE t.category_id = b.category_id " +
            "AND t.is_income = 0 " +
            "AND t.date BETWEEN :start AND :end" +
            ") as currentSpending " +
            "FROM budgets b " +
            "JOIN Categories c ON b.category_id = c.id")
    List<com.example.economiza.domain.model.BudgetListItem> getBudgetsWithSpendingSync(long start, long end);

    @Query("SELECT * FROM budgets WHERE category_id = :catId")
    LiveData<Budget> getBudgetForCategory(int catId);
}
