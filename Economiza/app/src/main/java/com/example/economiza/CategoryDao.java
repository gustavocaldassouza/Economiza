package com.example.economiza;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import kotlinx.coroutines.flow.Flow;

@Dao
public interface CategoryDao {
    @Insert
    void insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories")
    Flow<List<Category>> getAllCategories();

    @Query("SELECT name FROM categories WHERE id = :id")
    Flow<String> getCategoryNameById(int id);
}