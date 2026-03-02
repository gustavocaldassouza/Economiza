package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.usecase.AddTransactionUseCase;
import com.example.economiza.domain.usecase.DeleteTransactionUseCase;
import com.example.economiza.domain.usecase.UpdateTransactionUseCase;
import com.example.economiza.domain.usecase.GetTransactionByIdUseCase;
import com.example.economiza.domain.usecase.GetTransactionsUseCase;

import java.util.List;

public class TransactionViewModel extends ViewModel {
    private final GetTransactionsUseCase getTransactionsUseCase;
    private final AddTransactionUseCase addTransactionUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final GetTransactionByIdUseCase getTransactionByIdUseCase;

    public final LiveData<List<Transaction>> transactions;

    public TransactionViewModel(GetTransactionsUseCase getTransactionsUseCase,
            AddTransactionUseCase addTransactionUseCase,
            UpdateTransactionUseCase updateTransactionUseCase,
            DeleteTransactionUseCase deleteTransactionUseCase,
            GetTransactionByIdUseCase getTransactionByIdUseCase) {
        this.getTransactionsUseCase = getTransactionsUseCase;
        this.addTransactionUseCase = addTransactionUseCase;
        this.updateTransactionUseCase = updateTransactionUseCase;
        this.deleteTransactionUseCase = deleteTransactionUseCase;
        this.getTransactionByIdUseCase = getTransactionByIdUseCase;
        this.transactions = getTransactionsUseCase.execute();
    }

    public void addTransaction(Transaction transaction) {
        new Thread(() -> {
            try {
                addTransactionUseCase.execute(transaction);
            } catch (Exception e) {
            }
        }).start();
    }

    public void updateTransaction(Transaction transaction) {
        new Thread(() -> {
            try {
                updateTransactionUseCase.execute(transaction);
            } catch (Exception e) {
            }
        }).start();
    }

    public void deleteTransaction(Transaction transaction) {
        new Thread(() -> {
            try {
                deleteTransactionUseCase.execute(transaction);
            } catch (Exception e) {
            }
        }).start();
    }

    public Transaction getTransactionById(int id) {
        // This is a synchronous DB call, should ideally return LiveData or be handled
        // via Callback
        // Given existing pattern, we'll keep it simple but run it carefully.
        // For editing, a sync call on a worker thread is usually fine if the UI waits.
        return getTransactionByIdUseCase.execute(id);
    }
}
