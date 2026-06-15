package com.example.spesafacile.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_items",
    foreignKeys = [ForeignKey(
        entity = ShoppingList::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["listId"])]
)
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listId: Int,
    val name: String,
    val category: String = Category.ALTRO.name,
    val quantity: Int = 1,
    val unit: String = "",
    val isChecked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
