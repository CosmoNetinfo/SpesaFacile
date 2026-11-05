package com.example.spesafacile.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.spesafacile.data.NotebookColor
import org.junit.Assert.assertTrue
import org.junit.Test

class NotebookColorsTest {
    private fun contrast(a: Color, b: Color): Float {
        val first = a.luminance()
        val second = b.luminance()
        return (maxOf(first, second) + 0.05f) / (minOf(first, second) + 0.05f)
    }

    @Test fun allPalettesKeepTextReadableInBothAppearances() {
        for (dark in listOf(false, true)) for (color in NotebookColor.entries) {
            val scheme = notebookColors(dark, color)
            assertTrue("$color/$dark button text", contrast(scheme.primary, scheme.onPrimary) >= 4.5f)
            assertTrue("$color/$dark card text", contrast(scheme.primaryContainer, scheme.onPrimaryContainer) >= 4.5f)
            assertTrue("$color/$dark dialog text", contrast(scheme.surfaceContainerHigh, scheme.onSurface) >= 4.5f)
        }
    }
}
