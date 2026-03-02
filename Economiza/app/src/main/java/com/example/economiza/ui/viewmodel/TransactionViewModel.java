package com.example.economiza.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.usecase.AddTransactionUseCase;
import com.example.economiza.domain.usecase.GetTransactionsUseCase;

import java.util.List;

public class TransactionViewModel extends ViewModel {
    private final GetTransactionsUseCase getTransactionsUseCase;
    private final AddTransactionUseCase addTransactionUseCase;

    public final LiveData<List<Transaction>> transactions;

    public TransactionViewModel(GetTransactionsUseCase getTransactionsUseCase,
            AddTransactionUseCase addTransactionUseCase) {
        this.getTransactionsUseCase = getTransactionsUseCase;
        this.addTransactionUseCase = addTransactionUseCase;
        this.transactions = getTransactionsUseCase.execute();
    }

    public void addTransaction(Transaction transaction) {
        new Thread(() -> {
            try {
                addTransactionUseCase.execute(transaction);
            } catch (Exception e) {
                // Handle error
            }
        }).start();
    }
}
