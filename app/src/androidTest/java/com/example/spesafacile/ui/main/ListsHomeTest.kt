package com.example.spesafacile.ui.main

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.example.spesafacile.data.dao.ListSummary
import com.example.spesafacile.theme.SpesaFacileTheme
import com.example.spesafacile.ui.screens.ListsHome
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class ListsHomeTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val lists = listOf(
        ListSummary(1, "Spesa della settimana", 0, 10, 3),
        ListSummary(2, "Cena con amici", 0, 4, 1),
        ListSummary(3, "Colazione del weekend", 0, 0, 0)
    )

    @Test fun homeSummarizesShoppingAndOpensTheSelectedList() {
        var opened: Int? = null
        compose.setContent { SpesaFacileTheme(darkTheme = false) {
            ListsHome(lists, { opened = it }, {}, {}, {}, {})
        } }
        compose.onNodeWithText("4").assertExists()
        compose.onNodeWithText("7 di 10 acquistati").assertExists()
        screenshot("home-light")
        compose.onNodeWithText("Spesa della settimana").performClick()
        compose.runOnIdle { assertEquals(1, opened) }
    }

    @Test fun emptyHomeMakesCreatingTheFirstListAvailable() {
        var created = 0
        compose.setContent { SpesaFacileTheme(darkTheme = false) {
            ListsHome(emptyList(), {}, { created++ }, {}, {}, {})
        } }
        compose.onNodeWithTag("home-list").performScrollToNode(hasText("Crea la tua prima lista"))
        compose.onNodeWithText("Crea la tua prima lista").performClick()
        compose.runOnIdle { assertEquals(1, created) }
        compose.onNodeWithTag("home-list").performScrollToIndex(0)
        screenshot("home-empty")
    }

    @Test fun listActionsRemainAvailableInDarkTheme() {
        var duplicated: Int? = null
        compose.setContent { SpesaFacileTheme(darkTheme = true) {
            ListsHome(lists, {}, {}, {}, { duplicated = it.id }, {})
        } }
        screenshot("home-dark")
        compose.onNodeWithContentDescription("Opzioni Spesa della settimana").performClick()
        compose.onNodeWithText("Duplica").performClick()
        compose.runOnIdle { assertEquals(1, duplicated) }
    }

    @Test fun homeRemainsScrollableWithLargeText() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                SpesaFacileTheme(darkTheme = false) { ListsHome(lists, {}, {}, {}, {}, {}) }
            }
        }
        compose.onNodeWithTag("home-list").performScrollToNode(hasText("Colazione del weekend"))
        compose.onNodeWithText("Colazione del weekend").assertIsDisplayed()
        compose.onNodeWithTag("home-list").performScrollToIndex(0)
        screenshot("home-large-text")
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir("home-screenshots"))
        directory.mkdirs()
        val file = File(directory, "$name.png")
        file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        // Gradle uninstalls the test app after execution: retain previews outside its data directory.
        val command = "mkdir -p /data/local/tmp/spesafacile-home && cp ${file.absolutePath} /data/local/tmp/spesafacile-home/$name.png && echo copied"
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        val result = ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.bufferedReader().readText() }
        check(result.contains("copied")) { "Unable to retain home preview" }
    }
}
