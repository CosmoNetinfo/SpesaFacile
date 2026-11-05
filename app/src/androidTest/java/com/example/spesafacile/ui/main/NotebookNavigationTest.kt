package com.example.spesafacile.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.spesafacile.theme.SpesaFacileTheme
import com.example.spesafacile.ui.screens.AppHomeShell
import org.junit.Rule
import org.junit.Test

class NotebookNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Test fun colorChoiceIsSavedAndUpdatesTheNotebook() {
        val preferences = com.example.spesafacile.data.AppPreferences.get(compose.activity)
        preferences.setNotebookColor(com.example.spesafacile.data.NotebookColor.ORANGE)
        compose.setContent {
            val color = preferences.notebookColor.collectAsState().value
            SpesaFacileTheme(darkTheme = false, notebookColor = color) { AppHomeShell({}) }
        }
        try {
            compose.onNodeWithText("Il mio blocco", useUnmergedTree = true).performClick()
            compose.onNodeWithText("Salvia").performScrollTo().performClick()
            compose.onNodeWithText("Salvia").assertIsSelected()
            org.junit.Assert.assertEquals("SAGE", compose.activity.getSharedPreferences("spesafacile_settings", android.content.Context.MODE_PRIVATE).getString("notebook_color", null))
            compose.onNodeWithText("Liste", useUnmergedTree = true).performClick()
            compose.waitUntil(10000) { compose.onAllNodesWithTag("home-list").fetchSemanticsNodes().isNotEmpty() }
            saveNotebookPreview(compose, "notebook-sage")
        } finally { preferences.setNotebookColor(com.example.spesafacile.data.NotebookColor.ORANGE) }
    }
    @Test fun appHasProductsAndUsableSettings() {
        compose.setContent { SpesaFacileTheme(darkTheme = false) { AppHomeShell({}) } }
        compose.waitUntil(10000) { compose.onAllNodesWithTag("home-list").fetchSemanticsNodes().isNotEmpty() }
        saveNotebookPreview(compose, "notebook-home")
        compose.onNodeWithText("Crea la tua prima lista").performClick()
        compose.onNodeWithText("Nuova lista").assertExists()
        saveNotebookPreview(compose, "dialog-list-light", dialog = true)
        compose.onNodeWithText("Salva").assertIsNotEnabled()
        compose.onNodeWithText("Nome della lista").performTextInput("Spesa di prova")
        compose.onNodeWithText("Salva").assertIsEnabled()
        compose.onNodeWithText("Annulla").performClick()
        compose.onNodeWithText("Prodotti", useUnmergedTree = true).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithTag("new-product").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Prodotti abituali").assertExists()
        saveNotebookPreview(compose, "notebook-products")
        compose.onNodeWithTag("new-product").performClick()
        compose.onNodeWithText("Nuovo prodotto abituale").assertExists()
        saveNotebookPreview(compose, "dialog-product-light", dialog = true)
        compose.onNodeWithText("Annulla").performClick()
        compose.onNodeWithText("Il mio blocco", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Come lo preferisci?").assertExists()
        compose.onNodeWithText("Come il dispositivo").assertExists()
        saveNotebookPreview(compose, "notebook-settings")
        compose.onNodeWithText("Liste", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Crea la tua prima lista").assertExists()
    }

    @Test fun creationDialogsAlsoUseTheDarkNotebookTheme() {
        compose.setContent { SpesaFacileTheme(darkTheme = true) { AppHomeShell({}) } }
        compose.waitUntil(10000) { compose.onAllNodesWithTag("home-list").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Crea la tua prima lista").performClick()
        compose.onNodeWithText("Nome della lista").assertExists()
        saveNotebookPreview(compose, "dialog-list-dark", dialog = true)
        compose.onNodeWithText("Annulla").performClick()
        compose.onNodeWithText("Prodotti", useUnmergedTree = true).performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithTag("new-product").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("new-product").performClick()
        compose.onNodeWithText("Nuovo prodotto abituale").assertExists()
        saveNotebookPreview(compose, "dialog-product-dark", dialog = true)
        compose.onNodeWithText("Annulla").performClick()
    }
}
