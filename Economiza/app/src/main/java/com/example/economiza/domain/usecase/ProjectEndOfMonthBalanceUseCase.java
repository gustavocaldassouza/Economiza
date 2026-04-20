package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.ForecastedBalance;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.RecurringPaymentRepository;
import com.example.economiza.domain.repository.TransactionRepository;

import java.util.Calendar;
import java.util.List;

public class ProjectEndOfMonthBalanceUseCase {
    private final TransactionRepository transactionRepository;
    private final RecurringPaymentRepository recurringPaymentRepository;

    public ProjectEndOfMonthBalanceUseCase(TransactionRepository transactionRepository,
                                           RecurringPaymentRepository recurringPaymentRepository) {
        this.transactionRepository = transactionRepository;
        this.recurringPaymentRepository = recurringPaymentRepository;
    }

    public ForecastedBalance execute() {
        Calendar cal = Calendar.getInstance();
        long today = cal.getTimeInMillis();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int daysLeft = daysInMonth - currentDay;

        // 1. Current Net Balance
        long totalIncome = transactionRepository.getTotalIncomeSync();
        long totalExpenses = transactionRepository.getTotalExpensesSync();
        long currentNetBalance = totalIncome - totalExpenses;

        // 2. Upcoming Fixed Costs (Recurring Payments)
        long upcomingRecurringSum = 0;
        List<RecurringPayment> recurringPayments = recurringPaymentRepository.getActiveRecurringPaymentsSync();
        for (RecurringPayment rp : recurringPayments) {
            if (rp.nextDueDate >= today && isSameMonth(rp.nextDueDate, today)) {
                upcomingRecurringSum += rp.amount;
            }
        }

        // 3. Projected Variable Costs
        // Fetch last 30 days of transactions to find a daily average
        long thirtyDaysAgo = today - (30L * 24 * 60 * 60 * 1000);
        List<Transaction> recentTransactions = transactionRepository.getAllTransactionsSync();
        long recentVariableSpend = 0;
        
        for (Transaction t : recentTransactions) {
            if (t.timestamp >= thirtyDaysAgo && !t.isIncome) {
                // Heuristic: If it matches a recurring payment amount EXACTLY, it might be fixed.
                // For a more robust app, we'd flag transactions as 'fixed' explicitly, but we'll approximate here
                boolean isLikelyFixed = false;
                for (RecurringPayment rp: recurringPayments) {
                    if (rp.amount == t.amount) {
                         isLikelyFixed = true;
                         break;
                    }
                }
                
                if (!isLikelyFixed) {
                    recentVariableSpend += t.amount;
                }
            }
        }

        long averageDailyVariableSpend = recentVariableSpend / 30;
        long projectedRemainingVariableSpend = averageDailyVariableSpend * daysLeft;

        // 4. Calculate Final Projection
        long projectedBalance = currentNetBalance - upcomingRecurringSum - projectedRemainingVariableSpend;

        // End of month timestamp
        cal.set(Calendar.DAY_OF_MONTH, daysInMonth);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        return new ForecastedBalance(projectedBalance, cal.getTimeInMillis());
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
