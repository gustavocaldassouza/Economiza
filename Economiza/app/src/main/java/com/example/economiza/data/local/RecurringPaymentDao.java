package com.example.economiza.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.economiza.domain.model.RecurringPayment;

import androidx.lifecycle.LiveData;
import java.util.List;

@Dao
public interface RecurringPaymentDao {

    @Insert
    void insert(RecurringPayment recurringPayment);

    @Update
    void update(RecurringPayment recurringPayment);

    @Delete
    void delete(RecurringPayment recurringPayment);

    @Query("SELECT * FROM recurring_payments")
    LiveData<List<RecurringPayment>> getAllRecurringPayments();

    @Query("SELECT * FROM recurring_payments WHERE is_active = 1")
    LiveData<List<RecurringPayment>> getActiveRecurringPayments();
}
