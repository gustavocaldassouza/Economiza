package com.example.economiza.domain.model;

import androidx.room.Embedded;
import androidx.room.Relation;

public class BudgetListItem {
    @Embedded
    public Budget budget;

    public String categoryName;
    public long currentSpending; // in cents
}
