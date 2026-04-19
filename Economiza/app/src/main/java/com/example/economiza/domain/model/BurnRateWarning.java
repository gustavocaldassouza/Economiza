package com.example.economiza.domain.model;

public class BurnRateWarning {
    public final int categoryId;
    public final double currentSpendPercentage;
    public final long projectedExceedDate;
    public final String categoryName;

    public BurnRateWarning(int categoryId, double currentSpendPercentage, long projectedExceedDate, String categoryName) {
        this.categoryId = categoryId;
        this.currentSpendPercentage = currentSpendPercentage;
        this.projectedExceedDate = projectedExceedDate;
        this.categoryName = categoryName;
    }
}
