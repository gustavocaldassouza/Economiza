package com.example.economiza.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.economiza.domain.usecase.AddBudgetUseCase;
import com.example.economiza.domain.usecase.DeleteRecurringPaymentUseCase;
import com.example.economiza.domain.usecase.ProcessRecurringPaymentsUseCase;
import com.example.economiza.domain.usecase.UpdateRecurringPaymentUseCase;
import com.example.economiza.domain.usecase.AddCategoryUseCase;
import com.example.economiza.domain.usecase.AddRecurringPaymentUseCase;
import com.example.economiza.domain.usecase.AddTransactionUseCase;
import com.example.economiza.domain.usecase.DeleteCategoryUseCase;
import com.example.economiza.domain.usecase.UpdateCategoryUseCase;
import com.example.economiza.domain.usecase.DeleteTransactionUseCase;
import com.example.economiza.domain.usecase.UpdateTransactionUseCase;
import com.example.economiza.domain.usecase.UpdateBudgetUseCase;
import com.example.economiza.domain.usecase.DeleteBudgetUseCase;
import com.example.economiza.domain.usecase.GetTransactionByIdUseCase;
import com.example.economiza.domain.usecase.ExportDataUseCase;
import com.example.economiza.domain.usecase.GetBudgetsUseCase;
import com.example.economiza.domain.usecase.GetCategoriesUseCase;
import com.example.economiza.domain.usecase.GetRecurringPaymentsUseCase;
import com.example.economiza.domain.usecase.GetTransactionsUseCase;
import com.example.economiza.domain.repository.TransactionRepository;
import com.example.economiza.domain.usecase.GetTotalExpensesUseCase;
import com.example.economiza.domain.usecase.GetTotalIncomeUseCase;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final GetTransactionsUseCase getTransactions;
    private final AddTransactionUseCase addTransaction;
    private final UpdateTransactionUseCase updateTransaction;
    private final DeleteTransactionUseCase deleteTransaction;
    private final GetTransactionByIdUseCase getTransactionById;
    private final GetTotalExpensesUseCase getTotalExpenses;
    private final GetTotalIncomeUseCase getTotalIncome;
    private final TransactionRepository txRepo;
    private final GetCategoriesUseCase getCategories;
    private final AddCategoryUseCase addCategory;
    private final UpdateCategoryUseCase updateCategory;
    private final DeleteCategoryUseCase deleteCategory;
    private final GetBudgetsUseCase getBudgets;
    private final AddBudgetUseCase addBudget;
    private final UpdateBudgetUseCase updateBudget;
    private final DeleteBudgetUseCase deleteBudget;
    private final GetRecurringPaymentsUseCase getRecurringPayments;
    private final AddRecurringPaymentUseCase addRecurringPayment;
    private final UpdateRecurringPaymentUseCase updateRecurringPayment;
    private final DeleteRecurringPaymentUseCase deleteRecurringPayment;
    private final ProcessRecurringPaymentsUseCase processRecurring;
    private final ExportDataUseCase exportData;

    public ViewModelFactory(
            GetTransactionsUseCase getTransactions,
            AddTransactionUseCase addTransaction,
            UpdateTransactionUseCase updateTransaction,
            DeleteTransactionUseCase deleteTransaction,
            GetTransactionByIdUseCase getTransactionById,
            GetTotalExpensesUseCase getTotalExpenses,
            GetTotalIncomeUseCase getTotalIncome,
            TransactionRepository txRepo,
            GetCategoriesUseCase getCategories,
            AddCategoryUseCase addCategory,
            UpdateCategoryUseCase updateCategory,
            DeleteCategoryUseCase deleteCategory,
            GetBudgetsUseCase getBudgets,
            AddBudgetUseCase addBudget,
            UpdateBudgetUseCase updateBudget,
            DeleteBudgetUseCase deleteBudget,
            GetRecurringPaymentsUseCase getRecurringPayments,
            AddRecurringPaymentUseCase addRecurringPayment,
            UpdateRecurringPaymentUseCase updateRecurringPayment,
            DeleteRecurringPaymentUseCase deleteRecurringPayment,
            ProcessRecurringPaymentsUseCase processRecurring,
            ExportDataUseCase exportData) {
        this.getTransactions = getTransactions;
        this.addTransaction = addTransaction;
        this.updateTransaction = updateTransaction;
        this.deleteTransaction = deleteTransaction;
        this.getTransactionById = getTransactionById;
        this.getTotalExpenses = getTotalExpenses;
        this.getTotalIncome = getTotalIncome;
        this.txRepo = txRepo;
        this.getCategories = getCategories;
        this.addCategory = addCategory;
        this.updateCategory = updateCategory;
        this.deleteCategory = deleteCategory;
        this.getBudgets = getBudgets;
        this.addBudget = addBudget;
        this.updateBudget = updateBudget;
        this.deleteBudget = deleteBudget;
        this.getRecurringPayments = getRecurringPayments;
        this.addRecurringPayment = addRecurringPayment;
        this.updateRecurringPayment = updateRecurringPayment;
        this.deleteRecurringPayment = deleteRecurringPayment;
        this.processRecurring = processRecurring;
        this.exportData = exportData;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class))
            return (T) new DashboardViewModel(getTotalExpenses, getTotalIncome, txRepo, getCategories);

        if (modelClass.isAssignableFrom(TransactionViewModel.class))
            return (T) new TransactionViewModel(getTransactions, addTransaction, updateTransaction, deleteTransaction,
                    getTransactionById);
        if (modelClass.isAssignableFrom(CategoryViewModel.class))
            return (T) new CategoryViewModel(getCategories, addCategory, updateCategory, deleteCategory);

        if (modelClass.isAssignableFrom(BudgetViewModel.class))
            return (T) new BudgetViewModel(getBudgets, addBudget, updateBudget, deleteBudget, getCategories);
        if (modelClass.isAssignableFrom(RecurringPaymentViewModel.class))
            return (T) new RecurringPaymentViewModel(getRecurringPayments, addRecurringPayment, updateRecurringPayment,
                    deleteRecurringPayment, processRecurring);
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
