package com.example.economiza.domain.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets", foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "category_id", onDelete = ForeignKey.CASCADE), indices = {
        @Index("category_id") })
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "monthly_limit")
    public long monthlyLimit;

    @ColumnInfo(name = "spent_so_far")
    public long spentSoFar;

    public Budget() {
    }
}
