package com.example.economiza.data.repository;

import androidx.lifecycle.LiveData;
import com.example.economiza.data.local.BudgetDao;
import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.repository.BudgetRepository;

import java.util.List;

public class BudgetRepositoryImpl implements BudgetRepository {
    private final BudgetDao budgetDao;

    public BudgetRepositoryImpl(BudgetDao budgetDao) {
        this.budgetDao = budgetDao;
    }

    @Override
    public void insert(Budget budget) {
        budgetDao.insert(budget);
    }

    @Override
    public void update(Budget budget) {
        budgetDao.update(budget);
    }

    @Override
    public void delete(Budget budget) {
        budgetDao.delete(budget);
    }

    @Override
    public LiveData<List<Budget>> getAllBudgets() {
        return budgetDao.getAllBudgets();
    }

    @Override
    public LiveData<Budget> getBudgetForCategory(int catId) {
        return budgetDao.getBudgetForCategory(catId);
    }
}
