package com.example.spesafacile.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.spesafacile.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isChecked ASC, createdAt DESC")
    fun getItemsByList(listId: Int): Flow<List<ShoppingItem>>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId AND isChecked = 0")
    fun getUncheckedCount(listId: Int): Flow<Int>

    @Query("SELECT DISTINCT name FROM shopping_items ORDER BY name ASC")
    fun getAllItemNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItem)

    @Update
    suspend fun update(item: ShoppingItem)

    @Delete
    suspend fun delete(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE listId = :listId AND isChecked = 1")
    suspend fun deleteCheckedItems(listId: Int)
}
