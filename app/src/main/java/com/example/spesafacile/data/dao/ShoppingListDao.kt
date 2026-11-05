package com.example.spesafacile.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.spesafacile.data.model.ShoppingList
import androidx.room.ColumnInfo
import com.example.spesafacile.data.model.SharedImport
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    suspend fun snapshot(): List<ShoppingList>

    @Query("UPDATE shopping_lists SET budgetCents = :budget WHERE id = :id")
    suspend fun setBudget(id: Int, budget: Long?)

    @Query("SELECT listId FROM shared_imports WHERE digest = :digest")
    suspend fun findSharedImport(digest: String): Int?

    @Insert
    suspend fun saveSharedImport(import: SharedImport)
    @Query("SELECT l.*, COUNT(i.id) AS totalCount, COALESCE(SUM(CASE WHEN i.isChecked = 0 THEN 1 ELSE 0 END), 0) AS uncheckedCount FROM shopping_lists l LEFT JOIN shopping_items i ON i.listId = l.id GROUP BY l.id ORDER BY l.createdAt DESC")
    fun getListSummaries(): Flow<List<ListSummary>>
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    fun getListById(listId: Int): Flow<ShoppingList?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingList): Long

    @Update
    suspend fun update(list: ShoppingList)

    @Delete
    suspend fun delete(list: ShoppingList)
}

data class ListSummary(
    val id: Int,
    val name: String,
    val createdAt: Long,
    @ColumnInfo(name = "totalCount") val totalCount: Int,
    @ColumnInfo(name = "uncheckedCount") val uncheckedCount: Int,
    val budgetCents: Long? = null
) {
    fun asList() = ShoppingList(id, name, createdAt, budgetCents)
}
