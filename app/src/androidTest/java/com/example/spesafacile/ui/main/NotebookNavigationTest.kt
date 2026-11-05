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
        compose.onNodeWithText("Prodotti abituali").assertExists()
        compose.onNodeWithText("Nuovo prodotto").assertExists()
        saveNotebookPreview(compose, "notebook-products")
        compose.onNodeWithText("Il mio blocco", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Come lo preferisci?").assertExists()
        compose.onNodeWithText("Come il dispositivo").assertExists()
        saveNotebookPreview(compose, "notebook-settings")
        compose.onNodeWithText("Liste", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Crea la tua prima lista").assertExists()
    }
}
