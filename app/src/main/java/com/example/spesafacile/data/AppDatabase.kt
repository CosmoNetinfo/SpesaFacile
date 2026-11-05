package com.example.spesafacile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spesafacile.data.dao.ShoppingItemDao
import com.example.spesafacile.data.dao.ShoppingListDao
import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.data.model.ShoppingList
import com.example.spesafacile.data.model.ProductSuggestion
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ShoppingList::class, ShoppingItem::class, ProductSuggestion::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE shopping_items_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, listId INTEGER NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, priceCents INTEGER, isChecked INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(listId) REFERENCES shopping_lists(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO shopping_items_new (id,listId,name,category,quantity,unit,isChecked,createdAt) SELECT id,listId,name,category,CAST(quantity AS REAL),unit,isChecked,createdAt FROM shopping_items")
                db.execSQL("DROP TABLE shopping_items")
                db.execSQL("ALTER TABLE shopping_items_new RENAME TO shopping_items")
                db.execSQL("CREATE INDEX index_shopping_items_listId ON shopping_items(listId)")
                db.execSQL("CREATE TABLE product_suggestions (normalizedName TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, category TEXT NOT NULL, unit TEXT NOT NULL, lastUsedAt INTEGER NOT NULL)")
                db.execSQL("INSERT OR REPLACE INTO product_suggestions SELECT LOWER(TRIM(name)), TRIM(name), category, unit, createdAt FROM shopping_items ORDER BY createdAt ASC, id ASC")
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spesa_facile_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
