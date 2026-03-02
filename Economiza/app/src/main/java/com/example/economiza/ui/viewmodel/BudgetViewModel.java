package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.usecase.AddBudgetUseCase;
import com.example.economiza.domain.usecase.GetBudgetsUseCase;
import com.example.economiza.domain.usecase.GetCategoriesUseCase;

import java.util.List;

public class BudgetViewModel extends ViewModel {
    private final AddBudgetUseCase addBudget;
    public final LiveData<List<Budget>> budgets;
    public final LiveData<List<Category>> categories;

    public BudgetViewModel(GetBudgetsUseCase getBudgets, AddBudgetUseCase addBudget,
            GetCategoriesUseCase getCategories) {
        this.budgets = getBudgets.execute();
        this.addBudget = addBudget;
        this.categories = getCategories.execute();
    }

    public void addBudget(Budget budget) {
        new Thread(() -> addBudget.execute(budget)).start();
    }
}
