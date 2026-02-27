package com.example.economiza;

import androidx.room.TypeConverter;

import java.util.Date;

public class TypeConverters {
    @TypeConverter
    public static Long fromDate(Date date){
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp){
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static String fromFrequency(Frequency frequency){
        return frequency == null ? null : frequency.name();
    }

    @TypeConverter
    public static Frequency toFrequency(String frequency){
        return frequency == null ? null : Frequency.valueOf(frequency);
    }
}
