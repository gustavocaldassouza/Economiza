package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.usecase.GetTotalExpensesUseCase;
import com.example.economiza.domain.usecase.GetTotalIncomeUseCase;

public class DashboardViewModel extends ViewModel {
    public final LiveData<Long> totalExpenses;
    public final LiveData<Long> totalIncome;

    public DashboardViewModel(GetTotalExpensesUseCase getTotalExpensesUseCase,
            GetTotalIncomeUseCase getTotalIncomeUseCase) {
        this.totalExpenses = getTotalExpensesUseCase.execute();
        this.totalIncome = getTotalIncomeUseCase.execute();
    }
}
