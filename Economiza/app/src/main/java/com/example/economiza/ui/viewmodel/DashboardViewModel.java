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
import com.example.economiza.domain.model.SafeToSpendLimit;
import com.example.economiza.domain.model.BurnRateWarning;
import com.example.economiza.domain.model.ForecastedBalance;
import com.example.economiza.domain.model.ExpenseRatio;

import com.example.economiza.domain.usecase.CalculateSafeToSpendUseCase;
import com.example.economiza.domain.usecase.CalculateCategoryBurnRateUseCase;
import com.example.economiza.domain.usecase.ProjectEndOfMonthBalanceUseCase;
import com.example.economiza.domain.usecase.CalculateExpenseRatioUseCase;

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

    // --- Predictive Forecast Data Streams ---
    public final MediatorLiveData<SafeToSpendLimit> safeToSpendLimit = new MediatorLiveData<>();
    public final MediatorLiveData<List<BurnRateWarning>> burnRateWarnings = new MediatorLiveData<>();
    public final MediatorLiveData<ForecastedBalance> forecastedBalance = new MediatorLiveData<>();
    public final MediatorLiveData<ExpenseRatio> expenseRatio = new MediatorLiveData<>();

    private final CalculateSafeToSpendUseCase calculateSafeToSpend;
    private final CalculateCategoryBurnRateUseCase calculateCategoryBurnRate;
    private final ProjectEndOfMonthBalanceUseCase projectEndOfMonthBalance;
    private final CalculateExpenseRatioUseCase calculateExpenseRatio;

    public DashboardViewModel(GetTotalExpensesUseCase getTotalExpenses,
            GetTotalIncomeUseCase getTotalIncome,
            TransactionRepository txRepo,
            GetCategoriesUseCase getCategories,
            CalculateSafeToSpendUseCase calculateSafeToSpend,
            CalculateCategoryBurnRateUseCase calculateCategoryBurnRate,
            ProjectEndOfMonthBalanceUseCase projectEndOfMonthBalance,
            CalculateExpenseRatioUseCase calculateExpenseRatio) {
        this.calculateSafeToSpend = calculateSafeToSpend;
        this.calculateCategoryBurnRate = calculateCategoryBurnRate;
        this.projectEndOfMonthBalance = projectEndOfMonthBalance;
        this.calculateExpenseRatio = calculateExpenseRatio;
        this.totalExpenses = getTotalExpenses.execute();
        this.totalIncome = getTotalIncome.execute();
        this.categories = getCategories.execute();

        // Analytics Date Range: last 7 days (including today)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long endOfToday = cal.getTimeInMillis() + 86_400_000L - 1;
        cal.add(Calendar.DAY_OF_YEAR, -6);
        long startOf7DaysAgo = cal.getTimeInMillis();

        // Pie chart — matching the weekly context
        this.expensesByCategory = txRepo.getExpensesByCategory(startOf7DaysAgo, endOfToday);

        // Bar chart — last 7 days
        this.weeklyExpenses = txRepo.getDailyExpenses(startOf7DaysAgo, endOfToday);

        // Net balance
        netBalance.addSource(totalIncome, v -> recalcBalance());
        netBalance.addSource(totalExpenses, v -> recalcBalance());
        
        // Recalculate forecasts whenever net balance or categories (e.g., new transaction, budget edit) are updated
        safeToSpendLimit.addSource(netBalance, v -> recalculateForecasts());
        burnRateWarnings.addSource(netBalance, v -> recalculateForecasts());
        forecastedBalance.addSource(netBalance, v -> recalculateForecasts());
        expenseRatio.addSource(netBalance, v -> recalculateForecasts());
    }

    private void recalcBalance() {
        long inc = totalIncome.getValue() != null ? totalIncome.getValue() : 0L;
        long exp = totalExpenses.getValue() != null ? totalExpenses.getValue() : 0L;
        netBalance.setValue(inc - exp);
    }
    
    private void recalculateForecasts() {
        new Thread(() -> {
            SafeToSpendLimit limitObj = calculateSafeToSpend.execute();
            List<BurnRateWarning> warnings = calculateCategoryBurnRate.execute();
            ForecastedBalance balanceObj = projectEndOfMonthBalance.execute();
            ExpenseRatio ratioObj = calculateExpenseRatio.execute();
            
            safeToSpendLimit.postValue(limitObj);
            burnRateWarnings.postValue(warnings);
            forecastedBalance.postValue(balanceObj);
            expenseRatio.postValue(ratioObj);
        }).start();
    }
}
