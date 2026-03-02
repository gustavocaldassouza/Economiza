package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.usecase.AddRecurringPaymentUseCase;
import com.example.economiza.domain.usecase.DeleteRecurringPaymentUseCase;
import com.example.economiza.domain.usecase.GetRecurringPaymentsUseCase;
import com.example.economiza.domain.usecase.ProcessRecurringPaymentsUseCase;
import com.example.economiza.domain.usecase.UpdateRecurringPaymentUseCase;

import java.util.List;

public class RecurringPaymentViewModel extends ViewModel {

    private final AddRecurringPaymentUseCase addPayment;
    private final UpdateRecurringPaymentUseCase updatePayment;
    private final DeleteRecurringPaymentUseCase deletePayment;
    private final ProcessRecurringPaymentsUseCase processRecurring;
    public final LiveData<List<RecurringPayment>> payments;

    public RecurringPaymentViewModel(
            GetRecurringPaymentsUseCase getPayments,
            AddRecurringPaymentUseCase addPayment,
            UpdateRecurringPaymentUseCase updatePayment,
            DeleteRecurringPaymentUseCase deletePayment,
            ProcessRecurringPaymentsUseCase processRecurring) {
        this.payments = getPayments.execute();
        this.addPayment = addPayment;
        this.updatePayment = updatePayment;
        this.deletePayment = deletePayment;
        this.processRecurring = processRecurring;
    }

    /**
     * Inserts the payment AND immediately runs the auto-processor so that any
     * past occurrence is posted as a Transaction right away.
     * Use this when the user confirms they want the current-month transaction.
     */
    public void addPayment(RecurringPayment payment) {
        new Thread(() -> {
            addPayment.execute(payment);
            processRecurring.execute();
        }).start();
    }

    /**
     * Inserts the payment WITHOUT running the auto-processor.
     * Use this when the user chose to skip the current-month transaction;
     * {@code payment.nextDueDate} should already be advanced to next period.
     */
    public void addPaymentOnly(RecurringPayment payment) {
        new Thread(() -> addPayment.execute(payment)).start();
    }

    public void updatePayment(RecurringPayment payment) {
        new Thread(() -> updatePayment.execute(payment)).start();
    }

    public void deletePayment(RecurringPayment payment) {
        new Thread(() -> deletePayment.execute(payment)).start();
    }

    /** Toggle active state and persist. */
    public void toggleActive(RecurringPayment payment) {
        payment.isActive = !payment.isActive;
        updatePayment(payment);
    }
}
