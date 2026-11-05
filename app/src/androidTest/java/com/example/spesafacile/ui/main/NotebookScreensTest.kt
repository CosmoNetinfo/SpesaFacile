package com.example.spesafacile.ui.main

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.spesafacile.data.*
import com.example.spesafacile.data.model.*
import com.example.spesafacile.theme.SpesaFacileTheme
import com.example.spesafacile.ui.screens.*
import com.example.spesafacile.util.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NotebookScreensTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun shoppingShowsBudgetAndShareOptions() = runBlocking<Unit> {
        val db = AppDatabase.getDatabase(context)
        val repo = ShoppingRepository(db)
        val id = repo.insertList(ShoppingList(name = "Spesa della settimana", budgetCents = 2500)).toInt()
        try {
            repo.saveItem(ShoppingItem(listId = id, name = "Mele", quantity = 0.5, unit = "kg", category = Category.FRUTTA_VERDURA.name, priceCents = 249))
            repo.saveItem(ShoppingItem(listId = id, name = "Latte", quantity = 1.0, unit = "l", category = Category.LATTICINI.name, priceCents = 159))
            compose.setContent { SpesaFacileTheme(darkTheme = false) { ShoppingScreen(id, {}) } }
            compose.waitUntil(10000) { compose.onAllNodes(hasText("Budget:", substring = true)).fetchSemanticsNodes().isNotEmpty() }
            saveNotebookPreview(compose, "notebook-shopping")
            compose.onNodeWithContentDescription("Condividi lista").performClick()
            compose.onNodeWithText("Invia link all’app").assertExists()
            compose.onNodeWithText("Invia come testo").assertExists()
        } finally { repo.deleteList(ShoppingList(id = id, name = "Spesa della settimana")) }
    }

    @Test fun sharedListCanBeSavedAndOpened() = runBlocking<Unit> {
        val token = android.net.Uri.parse(TransferCodec.share(TransferList("Cena in famiglia", 2000,
            listOf(ShoppingItem(listId = 0, name = "Pomodori", quantity = 0.5, unit = "kg", category = Category.FRUTTA_VERDURA.name))))).fragment!!
        var opened: Int? = null
        compose.setContent { SpesaFacileTheme(darkTheme = false) { SharedListScreen(token, { opened = it }, {}) } }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Salva e apri la lista").fetchSemanticsNodes().isNotEmpty() }
        saveNotebookPreview(compose, "notebook-share")
        compose.onNodeWithText("Salva e apri la lista").performClick()
        compose.waitUntil(10000) { opened != null }
        val id = checkNotNull(opened)
        val db = AppDatabase.getDatabase(context)
        try { assertEquals("Pomodori", db.shoppingItemDao().snapshot(id).single().name) }
        finally { db.shoppingListDao().delete(ShoppingList(id = id, name = "Cena in famiglia")) }
    }
}
