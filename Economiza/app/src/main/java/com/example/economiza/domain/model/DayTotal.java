package com.example.economiza.domain.model;

import androidx.room.ColumnInfo;

/**
 * Projection for grouping spending by day bucket (milliseconds / 86_400_000).
 * Used to populate the weekly BarChart on the dashboard.
 */
public class DayTotal {
    /** Day bucket: epoch millis / 86_400_000 (i.e. days since Unix epoch). */
    @ColumnInfo(name = "day_bucket")
    public long dayBucket;

    @ColumnInfo(name = "total")
    public long total; // in cents
}
