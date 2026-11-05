package com.example.spesafacile.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.spesafacile.theme.SpesaFacileTheme
import com.example.spesafacile.ui.screens.AppHomeShell
import org.junit.Rule
import org.junit.Test

class NotebookNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun appHasProductsAndUsableSettings() {
        compose.setContent { SpesaFacileTheme(darkTheme = false) { AppHomeShell({}) } }
        compose.waitUntil(10000) { compose.onAllNodesWithTag("home-list").fetchSemanticsNodes().isNotEmpty() }
        saveNotebookPreview(compose, "notebook-home")
        compose.onNodeWithText("Prodotti", useUnmergedTree = true).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithTag("new-product").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Prodotti abituali").assertExists()
        saveNotebookPreview(compose, "notebook-products")
        compose.onNodeWithTag("new-product").performClick()
        compose.onNodeWithText("Nuovo prodotto abituale").assertExists()
        compose.onNodeWithText("Annulla").performClick()
        compose.onNodeWithText("Il mio blocco", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Come lo preferisci?").assertExists()
        compose.onNodeWithText("Come il dispositivo").assertExists()
        saveNotebookPreview(compose, "notebook-settings")
        compose.onNodeWithText("Liste", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Crea la tua prima lista").assertExists()
    }
}
