package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.model.CategoryTotal;
import com.example.economiza.domain.model.DayTotal;
import com.example.economiza.domain.usecase.GetCategoriesUseCase;
import com.example.economiza.domain.usecase.GetTotalExpensesUseCase;
import com.example.economiza.domain.usecase.GetTotalIncomeUseCase;
import com.example.economiza.domain.repository.TransactionRepository;

import java.util.Calendar;
import java.util.List;

public class DashboardViewModel extends ViewModel {

    public final LiveData<Long> totalExpenses;
    public final LiveData<Long> totalIncome;
    public final MediatorLiveData<Long> netBalance = new MediatorLiveData<>();

    /** Expenses grouped by category — drives the Pie Chart. */
    public final LiveData<List<CategoryTotal>> expensesByCategory;

    /** Day-by-day spending for the last 7 days — drives the Bar Chart. */
    public final LiveData<List<DayTotal>> weeklyExpenses;

    /** All categories, needed to resolve categoryId → name/color for pie slices. */
    public final LiveData<List<Category>> categories;

    public DashboardViewModel(GetTotalExpensesUseCase getTotalExpenses,
            GetTotalIncomeUseCase getTotalIncome,
            TransactionRepository txRepo,
            GetCategoriesUseCase getCategories) {
        this.totalExpenses = getTotalExpenses.execute();
        this.totalIncome = getTotalIncome.execute();
        this.categories = getCategories.execute();

        // Pie chart
        this.expensesByCategory = txRepo.getExpensesByCategory();

        // Bar chart — last 7 days
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long endOfToday = cal.getTimeInMillis() + 86_400_000L - 1;
        cal.add(Calendar.DAY_OF_YEAR, -6);
        long startOf7DaysAgo = cal.getTimeInMillis();
        this.weeklyExpenses = txRepo.getDailyExpenses(startOf7DaysAgo, endOfToday);

        // Net balance
        netBalance.addSource(totalIncome, v -> recalcBalance());
        netBalance.addSource(totalExpenses, v -> recalcBalance());
    }

    private void recalcBalance() {
        long inc = totalIncome.getValue() != null ? totalIncome.getValue() : 0L;
        long exp = totalExpenses.getValue() != null ? totalExpenses.getValue() : 0L;
        netBalance.setValue(inc - exp);
    }
}
