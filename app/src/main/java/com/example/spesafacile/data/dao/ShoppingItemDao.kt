package com.example.spesafacile.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.data.model.ProductSuggestion
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isChecked ASC, createdAt DESC")
    fun getItemsByList(listId: Int): Flow<List<ShoppingItem>>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE listId = :listId AND isChecked = 0")
    fun getUncheckedCount(listId: Int): Flow<Int>

    @Query("SELECT * FROM product_suggestions ORDER BY isFavorite DESC, lastUsedAt DESC, name ASC")
    fun getSuggestions(): Flow<List<ProductSuggestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSuggestion(suggestion: ProductSuggestion)

    @Query("SELECT * FROM product_suggestions WHERE normalizedName = :key")
    suspend fun getSuggestion(key: String): ProductSuggestion?

    @Query("UPDATE product_suggestions SET isFavorite = :favorite WHERE normalizedName = :key")
    suspend fun setFavorite(key: String, favorite: Boolean)

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY createdAt, id")
    suspend fun snapshot(listId: Int): List<ShoppingItem>

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: Int, checked: Boolean)

    @Query("UPDATE shopping_items SET quantity = :quantity WHERE id = :id")
    suspend fun setQuantity(id: Int, quantity: Double)

    @Query("UPDATE shopping_items SET name = :name, category = :category, quantity = :quantity, unit = :unit, priceCents = :priceCents WHERE id = :id AND listId = :listId")
    suspend fun updateDetails(id: Int, listId: Int, name: String, category: String, quantity: Double, unit: String, priceCents: Long?): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun restore(item: ShoppingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItem)

    @Update
    suspend fun update(item: ShoppingItem)

    @Delete
    suspend fun delete(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE listId = :listId AND isChecked = 1")
    suspend fun deleteCheckedItems(listId: Int)
}
