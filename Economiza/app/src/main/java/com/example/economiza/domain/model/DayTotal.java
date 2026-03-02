package com.example.economiza.domain.model;

import androidx.room.ColumnInfo;

/**
 * Projection for grouping spending by day bucket (milliseconds / 86_400_000).
 * Used to populate the weekly BarChart on the dashboard.
 */
public class DayTotal {
    /** Day bucket: formatted date string "YYYY-MM-DD" in local time. */
    @ColumnInfo(name = "day_bucket")
    public String dayBucket;

    @ColumnInfo(name = "total")
    public long total; // in cents
}
