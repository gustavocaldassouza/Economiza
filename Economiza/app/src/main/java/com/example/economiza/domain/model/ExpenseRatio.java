package com.example.economiza.domain.model;

public class ExpenseRatio {
    public final double fixedPercentage;
    public final double variablePercentage;

    public ExpenseRatio(double fixed, double variable) {
        this.fixedPercentage = fixed;
        this.variablePercentage = variable;
    }
}
