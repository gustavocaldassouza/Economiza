package com.example.economiza.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.economiza.domain.usecase.AddBudgetUseCase;
import com.example.economiza.domain.usecase.AddCategoryUseCase;
import com.example.economiza.domain.usecase.AddRecurringPaymentUseCase;
import com.example.economiza.domain.usecase.AddTransactionUseCase;
import com.example.economiza.domain.usecase.DeleteCategoryUseCase;
import com.example.economiza.domain.usecase.DeleteTransactionUseCase;
import com.example.economiza.domain.usecase.ExportDataUseCase;
import com.example.economiza.domain.usecase.GetBudgetsUseCase;
import com.example.economiza.domain.usecase.GetCategoriesUseCase;
import com.example.economiza.domain.usecase.GetRecurringPaymentsUseCase;
import com.example.economiza.domain.usecase.GetTransactionsUseCase;
import com.example.economiza.domain.usecase.GetTotalExpensesUseCase;
import com.example.economiza.domain.usecase.GetTotalIncomeUseCase;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final GetTransactionsUseCase getTransactions;
    private final AddTransactionUseCase addTransaction;
    private final DeleteTransactionUseCase deleteTransaction;
    private final GetTotalExpensesUseCase getTotalExpenses;
    private final GetTotalIncomeUseCase getTotalIncome;
    private final GetCategoriesUseCase getCategories;
    private final AddCategoryUseCase addCategory;
    private final DeleteCategoryUseCase deleteCategory;
    private final GetBudgetsUseCase getBudgets;
    private final AddBudgetUseCase addBudget;
    private final GetRecurringPaymentsUseCase getRecurringPayments;
    private final AddRecurringPaymentUseCase addRecurringPayment;
    private final ExportDataUseCase exportData;

    public ViewModelFactory(
            GetTransactionsUseCase getTransactions,
            AddTransactionUseCase addTransaction,
            DeleteTransactionUseCase deleteTransaction,
            GetTotalExpensesUseCase getTotalExpenses,
            GetTotalIncomeUseCase getTotalIncome,
            GetCategoriesUseCase getCategories,
            AddCategoryUseCase addCategory,
            DeleteCategoryUseCase deleteCategory,
            GetBudgetsUseCase getBudgets,
            AddBudgetUseCase addBudget,
            GetRecurringPaymentsUseCase getRecurringPayments,
            AddRecurringPaymentUseCase addRecurringPayment,
            ExportDataUseCase exportData) {
        this.getTransactions = getTransactions;
        this.addTransaction = addTransaction;
        this.deleteTransaction = deleteTransaction;
        this.getTotalExpenses = getTotalExpenses;
        this.getTotalIncome = getTotalIncome;
        this.getCategories = getCategories;
        this.addCategory = addCategory;
        this.deleteCategory = deleteCategory;
        this.getBudgets = getBudgets;
        this.addBudget = addBudget;
        this.getRecurringPayments = getRecurringPayments;
        this.addRecurringPayment = addRecurringPayment;
        this.exportData = exportData;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class))
            return (T) new DashboardViewModel(getTotalExpenses, getTotalIncome);
        if (modelClass.isAssignableFrom(TransactionViewModel.class))
            return (T) new TransactionViewModel(getTransactions, addTransaction);
        if (modelClass.isAssignableFrom(CategoryViewModel.class))
            return (T) new CategoryViewModel(getCategories, addCategory, deleteCategory);
        if (modelClass.isAssignableFrom(BudgetViewModel.class))
            return (T) new BudgetViewModel(getBudgets, addBudget);
        if (modelClass.isAssignableFrom(RecurringPaymentViewModel.class))
            return (T) new RecurringPaymentViewModel(getRecurringPayments, addRecurringPayment);
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
