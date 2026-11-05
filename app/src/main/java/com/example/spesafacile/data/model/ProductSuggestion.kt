package com.example.spesafacile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_suggestions")
data class ProductSuggestion(
    @PrimaryKey val normalizedName: String,
    val name: String,
    val category: String,
    val unit: String,
    val lastUsedAt: Long = System.currentTimeMillis()
)
