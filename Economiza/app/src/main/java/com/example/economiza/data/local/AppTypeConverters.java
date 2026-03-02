package com.example.economiza.data.local;

import androidx.room.TypeConverter;

import com.example.economiza.domain.model.Frequency;

/**
 * Room TypeConverter methods for non-primitive types stored in the database.
 *
 * NOTE: This class is intentionally named AppTypeConverters (not
 * TypeConverters)
 * to avoid a name clash with androidx.room.TypeConverters annotation class,
 * which would cause Room to fail to resolve the reference at compile time.
 */
public class AppTypeConverters {

    @TypeConverter
    public static String fromFrequency(Frequency frequency) {
        return frequency == null ? null : frequency.name();
    }

    @TypeConverter
    public static Frequency toFrequency(String value) {
        return value == null ? null : Frequency.valueOf(value);
    }
}
