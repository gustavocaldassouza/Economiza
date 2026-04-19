package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Budget;
import com.example.economiza.domain.model.BudgetListItem;
import com.example.economiza.domain.model.BurnRateWarning;
import com.example.economiza.domain.repository.BudgetRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalculateCategoryBurnRateUseCase {
    private final BudgetRepository budgetRepository;

    public CalculateCategoryBurnRateUseCase(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public List<BurnRateWarning> execute() {
        List<BurnRateWarning> warnings = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        double monthElapsedPercentage = (double) currentDay / daysInMonth;

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfMonth = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long endOfMonth = cal.getTimeInMillis() - 1;

        List<BudgetListItem> budgets = budgetRepository.getBudgetsWithSpendingSync(startOfMonth, endOfMonth);

        for (BudgetListItem budgetItem : budgets) {
            if (budgetItem.budget.monthlyLimit <= 0) continue;

            double currentSpendPercentage = (double) budgetItem.currentSpending / budgetItem.budget.monthlyLimit;

            // Generating a warning if spending pace is 15% ahead of time elapsed OR already exceeded.
            if (currentSpendPercentage >= 1.0) {
                 warnings.add(new BurnRateWarning(
                        budgetItem.budget.categoryId,
                        currentSpendPercentage,
                        cal.getTimeInMillis(), // Exceeded *now* or in the past
                        budgetItem.categoryName
                ));
            } else if (currentSpendPercentage > monthElapsedPercentage + 0.15) {
                // Calculate projected exceed date
                double dailySpendVelocity = (double) budgetItem.currentSpending / currentDay;
                long remainingLimit = budgetItem.budget.monthlyLimit - budgetItem.currentSpending;
                int daysUntilExceed = dailySpendVelocity > 0 ? (int) (remainingLimit / dailySpendVelocity) : 0;

                Calendar exceedCal = Calendar.getInstance();
                exceedCal.add(Calendar.DAY_OF_YEAR, daysUntilExceed);

                warnings.add(new BurnRateWarning(
                        budgetItem.budget.categoryId,
                        currentSpendPercentage,
                        exceedCal.getTimeInMillis(),
                        budgetItem.categoryName
                ));
            }
        }

        return warnings;
    }
}
