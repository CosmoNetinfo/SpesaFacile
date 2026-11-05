package com.example.spesafacile.data.model

import androidx.room.*

@Entity(tableName = "shared_imports", foreignKeys = [ForeignKey(
    entity = ShoppingList::class, parentColumns = ["id"], childColumns = ["listId"], onDelete = ForeignKey.CASCADE
)], indices = [Index("listId")])
data class SharedImport(@PrimaryKey val digest: String, val listId: Int)
