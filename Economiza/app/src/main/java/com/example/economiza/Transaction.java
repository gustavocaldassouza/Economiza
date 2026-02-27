package com.example.economiza;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "Transactions",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "category_id",
        onDelete = ForeignKey.CASCADE
    )
)
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "amount")
    public long amount;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "date")
    public long timestamp;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "is_income")
    public boolean isIncome;

    public Transaction(){}

}
