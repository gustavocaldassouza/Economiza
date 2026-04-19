package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.ExpenseRatio;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.RecurringPaymentRepository;
import com.example.economiza.domain.repository.TransactionRepository;

import java.util.Calendar;
import java.util.List;

public class CalculateExpenseRatioUseCase {
    private final TransactionRepository transactionRepository;
    private final RecurringPaymentRepository recurringPaymentRepository;

    public CalculateExpenseRatioUseCase(TransactionRepository transactionRepository,
                                        RecurringPaymentRepository recurringPaymentRepository) {
        this.transactionRepository = transactionRepository;
        this.recurringPaymentRepository = recurringPaymentRepository;
    }

    public ExpenseRatio execute() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfMonth = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        long endOfMonth = cal.getTimeInMillis() - 1;

        List<Transaction> transactions = transactionRepository.getAllTransactionsSync();
        List<RecurringPayment> recurringPayments = recurringPaymentRepository.getActiveRecurringPaymentsSync();

        long fixedExpensesThisMonth = 0;
        long variableExpensesThisMonth = 0;

        for (Transaction t : transactions) {
            if (!t.isIncome && t.timestamp >= startOfMonth && t.timestamp <= endOfMonth) {
                boolean isFixed = false;
                // Heuristic: Check if transaction amount matches an active recurring payment
                for (RecurringPayment rp : recurringPayments) {
                    if (rp.amount == t.amount) {
                        isFixed = true;
                        break;
                    }
                }

                if (isFixed) {
                    fixedExpensesThisMonth += t.amount;
                } else {
                    variableExpensesThisMonth += t.amount;
                }
            }
        }

        long totalExpenses = fixedExpensesThisMonth + variableExpensesThisMonth;

        if (totalExpenses == 0) {
            return new ExpenseRatio(0.0, 0.0);
        }

        double fixedRatio = (double) fixedExpensesThisMonth / totalExpenses;
        double variableRatio = (double) variableExpensesThisMonth / totalExpenses;

        return new ExpenseRatio(fixedRatio, variableRatio);
    }
}
