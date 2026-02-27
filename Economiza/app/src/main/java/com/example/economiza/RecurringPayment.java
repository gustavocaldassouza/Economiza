package com.example.economiza;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "recurring_payments",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "category_id",
        onDelete = ForeignKey.CASCADE
    )
)
public class RecurringPayment {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "amount")
    public long amount;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "frequency")
    public Frequency frequency;

    @ColumnInfo(name = "next_due_date")
    public long nextDueDate;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    public RecurringPayment(){}
}
