package com.example.spesafacile.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.spesafacile.data.model.*
import com.example.spesafacile.ui.components.CategorySection
import com.example.spesafacile.ui.components.ProductEditor
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MainScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun categoryCanBeCollapsedAndExpanded() {
        compose.setContent { MaterialTheme {
            CategorySection(Category.FRUTTA_VERDURA,
                listOf(ShoppingItem(id = 1, listId = 1, name = "Mele")), {}, {}, {})
        } }
        compose.onNodeWithText("Mele").assertExists()
        compose.onNodeWithContentDescription("Comprimi").performClick()
        compose.onNodeWithText("Mele").assertDoesNotExist()
        compose.onNodeWithContentDescription("Espandi").performClick()
        compose.onNodeWithText("Mele").assertExists()
    }

    @Test fun editorSavesFractionalQuantityAndPrice() {
        var saved: ShoppingItem? = null
        compose.setContent { MaterialTheme {
            ProductEditor(ShoppingItem(id = 1, listId = 1, name = "Mele"), false, {}, { saved = it })
        } }
        compose.onNodeWithText("Quantità").performTextReplacement("0,5")
        compose.onNodeWithText("Prezzo per unità (€), facoltativo").performTextReplacement("2,49")
        compose.onNodeWithText("Salva").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(0.5, saved!!.quantity, 0.0)
            assertEquals(249L, saved!!.priceCents)
            assertEquals(1, saved!!.id)
        }
    }
}
