package com.example.economiza.domain.model;

public class ForecastedBalance {
    public final long amount;
    public final long forDate;

    public ForecastedBalance(long amount, long forDate) {
        this.amount = amount;
        this.forDate = forDate;
    }
}
