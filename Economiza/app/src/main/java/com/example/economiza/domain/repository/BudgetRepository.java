package com.example.economiza.domain.repository;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.model.Budget;
import java.util.List;

public interface BudgetRepository {
    void insert(Budget budget);

    void update(Budget budget);

    void delete(Budget budget);

    LiveData<List<Budget>> getAllBudgets();

    LiveData<Budget> getBudgetForCategory(int catId);
}
