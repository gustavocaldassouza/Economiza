package com.example.economiza.domain.model;

public class SafeToSpendLimit {
    public final long limit;
    public final long date;

    public SafeToSpendLimit(long limit, long date) {
        this.limit = limit;
        this.date = date;
    }
}
