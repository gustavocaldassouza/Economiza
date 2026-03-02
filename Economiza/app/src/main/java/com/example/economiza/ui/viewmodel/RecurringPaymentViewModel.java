package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.usecase.AddRecurringPaymentUseCase;
import com.example.economiza.domain.usecase.GetRecurringPaymentsUseCase;

import java.util.List;

public class RecurringPaymentViewModel extends ViewModel {
    private final AddRecurringPaymentUseCase addPayment;
    public final LiveData<List<RecurringPayment>> payments;

    public RecurringPaymentViewModel(GetRecurringPaymentsUseCase getPayments,
            AddRecurringPaymentUseCase addPayment) {
        this.payments = getPayments.execute();
        this.addPayment = addPayment;
    }

    public void addPayment(RecurringPayment payment) {
        new Thread(() -> addPayment.execute(payment)).start();
    }
}
