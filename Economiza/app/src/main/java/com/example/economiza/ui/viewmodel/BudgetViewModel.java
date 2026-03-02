package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.model.BudgetListItem;
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.usecase.AddBudgetUseCase;
import com.example.economiza.domain.usecase.GetBudgetsUseCase;
import com.example.economiza.domain.usecase.GetCategoriesUseCase;

import java.util.Calendar;
import java.util.List;

public class BudgetViewModel extends ViewModel {
    private final AddBudgetUseCase addBudget;
    public final LiveData<List<BudgetListItem>> budgets;
    public final LiveData<List<Category>> categories;

    public BudgetViewModel(GetBudgetsUseCase getBudgets,
            AddBudgetUseCase addBudget,
            GetCategoriesUseCase getCategories) {
        this.addBudget = addBudget;
        this.categories = getCategories.execute();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        long end = cal.getTimeInMillis() - 1;

        this.budgets = getBudgets.execute(start, end);
    }

    public void addBudget(Budget budget) {
        new Thread(() -> addBudget.execute(budget)).start();
    }
}
