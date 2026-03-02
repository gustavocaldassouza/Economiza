package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.usecase.GetTotalExpensesUseCase;
import com.example.economiza.domain.usecase.GetTotalIncomeUseCase;

public class DashboardViewModel extends ViewModel {
    public final LiveData<Long> totalExpenses;
    public final LiveData<Long> totalIncome;
    /** Net balance = totalIncome - totalExpenses, in cents. */
    public final MediatorLiveData<Long> netBalance = new MediatorLiveData<>();

    public DashboardViewModel(GetTotalExpensesUseCase getTotalExpensesUseCase,
            GetTotalIncomeUseCase getTotalIncomeUseCase) {
        this.totalExpenses = getTotalExpensesUseCase.execute();
        this.totalIncome = getTotalIncomeUseCase.execute();

        netBalance.addSource(totalIncome, income -> recalcBalance());
        netBalance.addSource(totalExpenses, expenses -> recalcBalance());
    }

    private void recalcBalance() {
        Long income = totalIncome.getValue();
        Long expenses = totalExpenses.getValue();
        long inc = income != null ? income : 0L;
        long exp = expenses != null ? expenses : 0L;
        netBalance.setValue(inc - exp);
    }
}
