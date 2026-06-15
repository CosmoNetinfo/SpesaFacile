package com.example.spesafacile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spesafacile.data.dao.ShoppingItemDao
import com.example.spesafacile.data.dao.ShoppingListDao
import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.data.model.ShoppingList

@Database(entities = [ShoppingList::class, ShoppingItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spesa_facile_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
