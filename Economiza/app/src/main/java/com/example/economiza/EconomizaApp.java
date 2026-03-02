package com.example.economiza;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.economiza.data.local.AppDatabase;
import com.example.economiza.data.local.VaultManager;
import com.example.economiza.ui.activities.CreateVaultActivity;
import com.example.economiza.ui.activities.OnboardingActivity;
import com.example.economiza.ui.activities.SetPinActivity;
import com.example.economiza.ui.activities.UnlockVaultActivity;
import android.app.Application;

import com.example.economiza.data.local.AppDatabase;
import com.example.economiza.data.local.VaultManager;
import com.example.economiza.data.repository.BudgetRepositoryImpl;
import com.example.economiza.data.repository.CategoryRepositoryImpl;
import com.example.economiza.data.repository.RecurringPaymentRepositoryImpl;
import com.example.economiza.data.repository.TransactionRepositoryImpl;
import com.example.economiza.domain.repository.BudgetRepository;
import com.example.economiza.domain.repository.CategoryRepository;
import com.example.economiza.domain.repository.RecurringPaymentRepository;
import com.example.economiza.domain.repository.TransactionRepository;
import com.example.economiza.domain.usecase.AddBudgetUseCase;
import com.example.economiza.domain.usecase.AddCategoryUseCase;
import com.example.economiza.domain.usecase.AddRecurringPaymentUseCase;
import com.example.economiza.domain.usecase.AddTransactionUseCase;
import com.example.economiza.domain.usecase.DeleteCategoryUseCase;
import com.example.economiza.domain.usecase.UpdateCategoryUseCase;
import com.example.economiza.domain.usecase.DeleteTransactionUseCase;
import com.example.economiza.domain.usecase.ExportDataUseCase;
import com.example.economiza.domain.usecase.GetBudgetsUseCase;
import com.example.economiza.domain.usecase.GetCategoriesUseCase;
import com.example.economiza.domain.usecase.GetRecurringPaymentsUseCase;
import com.example.economiza.domain.usecase.GetTransactionsUseCase;
import com.example.economiza.domain.usecase.GetTotalExpensesUseCase;
import com.example.economiza.domain.usecase.GetTotalIncomeUseCase;
import com.example.economiza.ui.viewmodel.ViewModelFactory;

public class EconomizaApp extends Application {

    private VaultManager vaultManager;
    private AppDatabase database;
    private ViewModelFactory viewModelFactory;
    private ExportDataUseCase exportDataUseCase;
    private CategoryRepository categoryRepository;
    private TransactionRepository transactionRepository;

    private int startedActivityCount = 0;
    private boolean isConfigChange = false;

    @Override
    public void onCreate() {
        super.onCreate();
        vaultManager = new VaultManager(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity a, Bundle b) {
            }

            @Override
            public void onActivityStarted(Activity a) {
                startedActivityCount++;
            }

            @Override
            public void onActivityResumed(Activity a) {
            }

            @Override
            public void onActivityPaused(Activity a) {
            }

            @Override
            public void onActivityStopped(Activity a) {
                isConfigChange = a.isChangingConfigurations();
                startedActivityCount--;
                if (startedActivityCount == 0 && !isConfigChange) {
                    // Lock vault when app is backgrounded
                    lockVault();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity a, Bundle s) {
            }

            @Override
            public void onActivityDestroyed(Activity a) {
            }
        });
    }

    /**
     * Called after the user authenticates (Create or Unlock vault).
     * Builds the entire dependency graph.
     */
    public void initDependencies(byte[] keyBytes) {
        database = AppDatabase.getInstance(this, keyBytes);

        // Seed default categories on first run
        new Thread(this::seedDefaultCategories).start();

        // Repositories
        transactionRepository = new TransactionRepositoryImpl(database.transactionDao());
        categoryRepository = new CategoryRepositoryImpl(database.categoryDao());
        BudgetRepository budgetRepository = new BudgetRepositoryImpl(database.budgetDao());
        RecurringPaymentRepository recurringRepo = new RecurringPaymentRepositoryImpl(database.recurringPaymentDao());

        // Use Cases
        GetTransactionsUseCase getTransactions = new GetTransactionsUseCase(transactionRepository);
        AddTransactionUseCase addTransaction = new AddTransactionUseCase(transactionRepository);
        DeleteTransactionUseCase deleteTransaction = new DeleteTransactionUseCase(transactionRepository);
        GetTotalExpensesUseCase getTotalExpenses = new GetTotalExpensesUseCase(transactionRepository);
        GetTotalIncomeUseCase getTotalIncome = new GetTotalIncomeUseCase(transactionRepository);
        GetCategoriesUseCase getCategories = new GetCategoriesUseCase(categoryRepository);
        AddCategoryUseCase addCategory = new AddCategoryUseCase(categoryRepository);
        UpdateCategoryUseCase updateCategory = new UpdateCategoryUseCase(categoryRepository);
        DeleteCategoryUseCase deleteCategory = new DeleteCategoryUseCase(categoryRepository);
        GetBudgetsUseCase getBudgets = new GetBudgetsUseCase(budgetRepository);
        AddBudgetUseCase addBudget = new AddBudgetUseCase(budgetRepository);
        GetRecurringPaymentsUseCase getRecurringPayments = new GetRecurringPaymentsUseCase(recurringRepo);
        AddRecurringPaymentUseCase addRecurringPayment = new AddRecurringPaymentUseCase(recurringRepo);
        exportDataUseCase = new ExportDataUseCase(transactionRepository);

        viewModelFactory = new ViewModelFactory(
                getTransactions, addTransaction, deleteTransaction,
                getTotalExpenses, getTotalIncome,
                transactionRepository,
                getCategories, addCategory, updateCategory, deleteCategory,
                getBudgets, addBudget,
                getRecurringPayments, addRecurringPayment,
                exportDataUseCase);
    }

    /** Seeds 8 default categories if the categories table is empty. */
    private void seedDefaultCategories() {
        if (categoryRepository == null)
            return;
        if (categoryRepository.getCategoryCount() > 0)
            return;

        String[][] defaultCats = {
                { "Food & Dining", "#FF8A65", "ic_food" },
                { "Transport", "#3D8BFF", "ic_transport" },
                { "Utilities", "#FFD54F", "ic_utilities" },
                { "Healthcare", "#81C784", "ic_health" },
                { "Entertainment", "#CE93D8", "ic_entertainment" },
                { "Shopping", "#F48FB1", "ic_shopping" },
                { "Savings", "#00D084", "ic_savings" },
                { "Other", "#8E97A8", "ic_other" },
        };
        for (String[] c : defaultCats) {
            com.example.economiza.domain.model.Category cat = new com.example.economiza.domain.model.Category();
            cat.name = c[0];
            cat.colorHex = c[1];
            cat.iconName = c[2];
            categoryRepository.insert(cat);
        }
    }

    public void lockVault() {
        AppDatabase.destroyInstance();
        database = null;
        viewModelFactory = null;
        exportDataUseCase = null;
        transactionRepository = null;
        categoryRepository = null;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public ExportDataUseCase getExportDataUseCase() {
        return exportDataUseCase;
    }

    public ViewModelFactory getViewModelFactory() {
        if (viewModelFactory == null)
            throw new IllegalStateException("Vault not yet unlocked.");
        return viewModelFactory;
    }

    public AppDatabase getDatabase() {
        return database;
    }
}
