package com.example.economiza.data.local;

import androidx.room.TypeConverter;
import com.example.economiza.domain.model.Frequency;

public class TypeConverters {
    @TypeConverter
    public static String fromFrequency(Frequency frequency) {
        return frequency == null ? null : frequency.name();
    }

    @TypeConverter
    public static Frequency toFrequency(String frequency) {
        return frequency == null ? null : Frequency.valueOf(frequency);
    }
}
