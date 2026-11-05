package com.example.spesafacile.ui.main

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.spesafacile.data.*
import com.example.spesafacile.data.model.*
import com.example.spesafacile.util.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class NotebookDataTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun favoritesAndBudgetSurviveShoppingAndBackup() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val restored = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repo = ShoppingRepository(db)
            val id = repo.insertList(ShoppingList(name = "Settimana", budgetCents = 4000)).toInt()
            repo.saveProduct("Latte", Category.LATTICINI.name, "l", true)
            repo.saveItem(ShoppingItem(listId = id, name = "Latte", quantity = 0.5, unit = "l", category = Category.LATTICINI.name, priceCents = 199))
            assertTrue(repo.getSuggestions().first().single().isFavorite)
            val item = db.shoppingItemDao().snapshot(id).single()
            repo.setChecked(item.id, true)
            val encoded = TransferCodec.backup(repo.exportBackup())
            val parsed = TransferCodec.parseBackup(encoded)
            val target = ShoppingRepository(restored)
            val ids = target.importDocument(parsed)
            val copy = restored.shoppingItemDao().snapshot(ids.single()).single()
            assertEquals(0.5, copy.quantity, 0.0)
            assertEquals(199L, copy.priceCents)
            assertTrue(copy.isChecked)
            assertEquals(4000L, restored.shoppingListDao().getListById(ids.single()).first()!!.budgetCents)
            assertTrue(target.getSuggestions().first().single().isFavorite)
            target.importDocument(parsed)
            assertEquals(2, target.getAllLists().first().size)
        } finally { db.close(); restored.close() }
    }

    @Test fun sharedLinkRoundTripsAndReopeningDoesNotDuplicate() = runTest {
        val list = TransferList("Cena in famiglia", 3000, listOf(ShoppingItem(listId = 1, name = "Mele", quantity = 0.5, unit = "kg", isChecked = true)))
        val token = Uri.parse(TransferCodec.share(list)).fragment!!
        val decoded = TransferCodec.parseShare(token)
        assertEquals(list.name, decoded.lists.single().name)
        assertEquals(0.5, decoded.lists.single().items.single().quantity, 0.0)
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repo = ShoppingRepository(db)
            val digest = TransferCodec.digest(token)
            val first = repo.importDocument(decoded, digest)
            val second = repo.importDocument(decoded, digest)
            assertEquals(first, second)
            assertEquals(1, repo.getAllLists().first().size)
            val parent = repo.getAllLists().first().single()
            repo.deleteList(parent)
            assertEquals(1, repo.importDocument(decoded, digest).size)
        } finally { db.close() }
    }

    @Test fun malformedTransfersAreRejectedBeforeDatabaseWrites() {
        assertThrows(Exception::class.java) { TransferCodec.parseShare("not-a-real-list") }
        assertThrows(Exception::class.java) { TransferCodec.parseShare("a".repeat(12001)) }
        assertThrows(Exception::class.java) { TransferCodec.parseBackup("{\"format\":\"unknown\"}") }
        assertThrows(Exception::class.java) { TransferCodec.parseBackup("{\"format\":\"spesafacile-backup\",\"version\":1,\"lists\":[{\"name\":\"Test\",\"items\":[{\"name\":\"Latte\",\"quantity\":-1}]}]}") }
    }

    @Test fun updaterRejectsCurrentVersionAndWrongChecksum() {
        val file = java.io.File(context.applicationInfo.sourceDir)
        val hash = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input -> val buffer = ByteArray(8192); while (true) { val count = input.read(buffer); if (count < 0) break; hash.update(buffer, 0, count) } }
        val digest = hash.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        val manager = AppUpdateManager.get(context)
        assertThrows(IllegalArgumentException::class.java) { manager.verify(file, AppRelease("v99.0.0", "", digest, file.length())) }
        assertThrows(IllegalArgumentException::class.java) { manager.verify(file, AppRelease("v99.0.0", "", "0".repeat(64), file.length())) }
        // The verified cache path must remain readable by the package parser and shareable with Android's installer.
        val cached = java.io.File(context.cacheDir, "updates/current.apk")
        cached.parentFile!!.mkdirs()
        try {
            file.copyTo(cached, overwrite = true)
            val rejected = assertThrows(IllegalArgumentException::class.java) {
                manager.verify(cached, AppRelease("v99.0.0", "", digest, cached.length()))
            }
            assertTrue(rejected.message!!.contains("già installata"))
            val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".updates", cached)
            context.contentResolver.openInputStream(uri)!!.use { input -> assertEquals(0x50, input.read()) }
        } finally { cached.delete() }
    }
}
