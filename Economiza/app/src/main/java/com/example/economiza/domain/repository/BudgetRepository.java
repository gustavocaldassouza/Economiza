package com.example.economiza.domain.repository;

import androidx.lifecycle.LiveData;
import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.model.BudgetListItem;
import java.util.List;

public interface BudgetRepository {
    void insert(Budget budget);

    void update(Budget budget);

    void delete(Budget budget);

    LiveData<List<BudgetListItem>> getAllBudgetsWithSpending(long start, long end);

    LiveData<Budget> getBudgetForCategory(int catId);

    List<BudgetListItem> getBudgetsWithSpendingSync(long start, long end);
}
