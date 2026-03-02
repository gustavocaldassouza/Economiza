package com.example.economiza;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface RecurringPaymentDao {

    @Insert
    void insert(RecurringPayment recurringPayment);

    @Update
    void update(RecurringPayment recurringPayment);

    @Delete
    void delete(RecurringPayment recurringPayment);

    @Query("SELECT * FROM recurring_payments")
    Flow<List<RecurringPayment>> getAllRecurringPayments();

    @Query("SELECT * FROM recurring_payments WHERE is_active = 1")
    Flow<List<RecurringPayment>> getActiveRecurringPayments();
    
}
