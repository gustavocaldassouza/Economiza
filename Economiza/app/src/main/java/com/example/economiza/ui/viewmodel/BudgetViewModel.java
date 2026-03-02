package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.usecase.AddBudgetUseCase;
import com.example.economiza.domain.usecase.GetBudgetsUseCase;

import java.util.List;

public class BudgetViewModel extends ViewModel {
    private final AddBudgetUseCase addBudget;
    public final LiveData<List<Budget>> budgets;

    public BudgetViewModel(GetBudgetsUseCase getBudgets, AddBudgetUseCase addBudget) {
        this.budgets = getBudgets.execute();
        this.addBudget = addBudget;
    }

    public void addBudget(Budget budget) {
        new Thread(() -> addBudget.execute(budget)).start();
    }
}
