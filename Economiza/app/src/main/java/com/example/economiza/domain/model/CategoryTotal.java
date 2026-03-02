package com.example.economiza.domain.model;

import androidx.room.ColumnInfo;

/**
 * Lightweight projection returned by analytics DAOqueries.
 * NOT a Room @Entity — never stored directly.
 */
public class CategoryTotal {
    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "total")
    public long total; // in cents
}
