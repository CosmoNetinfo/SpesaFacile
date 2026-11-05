package com.example.spesafacile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "product_suggestions")
data class ProductSuggestion(
    @PrimaryKey val normalizedName: String,
    val name: String,
    val category: String,
    val unit: String,
    val lastUsedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false
)
