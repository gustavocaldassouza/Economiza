package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.BudgetListItem;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.model.SafeToSpendLimit;
import com.example.economiza.domain.repository.BudgetRepository;
import com.example.economiza.domain.repository.RecurringPaymentRepository;
import com.example.economiza.domain.repository.TransactionRepository;

import java.util.Calendar;
import java.util.List;

public class CalculateSafeToSpendUseCase {
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringPaymentRepository recurringPaymentRepository;

    public CalculateSafeToSpendUseCase(TransactionRepository transactionRepository,
                                       BudgetRepository budgetRepository,
                                       RecurringPaymentRepository recurringPaymentRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.recurringPaymentRepository = recurringPaymentRepository;
    }

    public SafeToSpendLimit execute() {
        Calendar cal = Calendar.getInstance();
        long today = cal.getTimeInMillis();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int daysLeft = daysInMonth - currentDay + 1; // Include today

        // Calculate current net balance
        long totalIncome = transactionRepository.getTotalIncomeSync();
        long totalExpenses = transactionRepository.getTotalExpensesSync();
        long currentNetBalance = totalIncome - totalExpenses;

        // Sum upcoming recurring payments for the rest of the month
        long upcomingRecurringSum = 0;
        List<RecurringPayment> recurringPayments = recurringPaymentRepository.getActiveRecurringPaymentsSync();
        for (RecurringPayment rp : recurringPayments) {
            if (rp.nextDueDate >= today && isSameMonth(rp.nextDueDate, today)) {
                upcomingRecurringSum += rp.amount;
            }
        }

        // Sum remaining allocated budgets
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfMonth = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long endOfMonth = cal.getTimeInMillis() - 1;

        long remainingBudgetSum = 0;
        List<BudgetListItem> budgets = budgetRepository.getBudgetsWithSpendingSync(startOfMonth, endOfMonth);
        for (BudgetListItem budgetItem : budgets) {
            long remaining = budgetItem.budget.monthlyLimit - budgetItem.currentSpending;
            if (remaining > 0) {
                remainingBudgetSum += remaining;
            }
        }

        long availableNow = currentNetBalance - upcomingRecurringSum - remainingBudgetSum;
        long dailyLimit = daysLeft > 0 ? availableNow / daysLeft : 0;

        return new SafeToSpendLimit(Math.max(0, dailyLimit), today);
    }

    private boolean isSameMonth(long date1, long date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }
}
