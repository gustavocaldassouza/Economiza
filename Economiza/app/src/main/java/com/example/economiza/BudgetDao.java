package com.example.economiza;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets")
    Flow<List<Budget>> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE category_id = :catId")
    Flow<Budget> getBudgetForCategory(int catId);
}