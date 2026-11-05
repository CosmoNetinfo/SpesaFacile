package com.example.spesafacile.ui.main

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.spesafacile.data.*
import com.example.spesafacile.data.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DatabaseTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun suggestionsSurviveClearingAndDuplicationResetsPurchasedState() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repo = ShoppingRepository(db)
            val listId = repo.insertList(ShoppingList(name = "Settimana")).toInt()
            repo.saveItem(ShoppingItem(listId = listId, name = "Mele", category = Category.FRUTTA_VERDURA.name, quantity = 0.5, unit = "kg", priceCents = 199))
            val item = db.shoppingItemDao().snapshot(listId).single()
            repo.setChecked(item.id, true)
            val copyId = repo.duplicateList(ShoppingList(id = listId, name = "Settimana")).toInt()
            val copy = db.shoppingItemDao().snapshot(copyId).single()
            assertFalse(copy.isChecked)
            assertEquals(0.5, copy.quantity, 0.0)
            assertEquals(199L, copy.priceCents)
            repo.deleteCheckedItems(listId)
            assertTrue(db.shoppingItemDao().snapshot(listId).isEmpty())
            assertEquals("Mele", repo.getSuggestions().first().single().name)
            repo.deleteItem(copy)
            repo.restoreItem(copy)
            assertEquals(copy, db.shoppingItemDao().snapshot(copyId).single())
            repo.deleteList(ShoppingList(id = copyId, name = "copia"))
            assertTrue(db.shoppingItemDao().snapshot(copyId).isEmpty())
        } finally { db.close() }
    }

    @Test fun migrationPreservesExistingListsAndProducts() = runTest {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val helper = FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name).callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE shopping_lists (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE shopping_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, listId INTEGER NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, quantity INTEGER NOT NULL, unit TEXT NOT NULL, isChecked INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(listId) REFERENCES shopping_lists(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                    db.execSQL("CREATE INDEX index_shopping_items_listId ON shopping_items(listId)")
                    db.execSQL("INSERT INTO shopping_lists VALUES (1, 'Originale', 100)")
                    db.execSQL("INSERT INTO shopping_items VALUES (1, 1, 'Latte', 'LATTICINI', 2, 'l', 1, 100)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }).build())
        helper.writableDatabase
        helper.close()
        val db = Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(AppDatabase.MIGRATION_1_2).build()
        try {
            val item = db.shoppingItemDao().snapshot(1).single()
            assertEquals("Latte", item.name)
            assertEquals(2.0, item.quantity, 0.0)
            assertTrue(item.isChecked)
            assertNull(item.priceCents)
            assertEquals("Originale", db.shoppingListDao().getListById(1).first()!!.name)
            assertEquals("l", db.shoppingItemDao().getSuggestions().first().single().unit)
            db.shoppingListDao().delete(ShoppingList(id = 1, name = "Originale"))
            assertTrue(db.shoppingItemDao().snapshot(1).isEmpty())
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
