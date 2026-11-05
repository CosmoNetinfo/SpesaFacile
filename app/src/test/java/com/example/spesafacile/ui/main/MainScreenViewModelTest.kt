package com.example.spesafacile.ui.main

import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.util.ShoppingValues
import com.example.spesafacile.util.VersionComparator
import org.junit.Assert.*
import org.junit.Test

class ShoppingValuesTest {
    @Test fun quantitiesAcceptItalianDecimals() {
        assertEquals(0.5, ShoppingValues.parseQuantity("0,5")!!, 0.0)
        assertEquals("1,5", ShoppingValues.quantity(1.5))
        assertEquals("2", ShoppingValues.quantity(2.0))
    }
    @Test fun invalidQuantitiesAreRejected() {
        listOf("", "0", "-1", "NaN", "Infinity", "1000001").forEach { assertNull(ShoppingValues.parseQuantity(it)) }
    }
    @Test fun pricesAreStoredAsExactCents() {
        assertEquals(199L, ShoppingValues.parsePriceCents("1,99"))
        assertEquals(0L, ShoppingValues.parsePriceCents("0"))
        listOf("-1", "1,999", "NaN", "", "1000001").forEach { assertNull(ShoppingValues.parsePriceCents(it)) }
    }
    @Test fun totalRoundsOnceAndIgnoresMissingPrices() {
        val values = listOf(
            ShoppingItem(listId = 1, name = "Mele", quantity = 0.5, priceCents = 199),
            ShoppingItem(listId = 1, name = "Pere", quantity = 0.5, priceCents = 199),
            ShoppingItem(listId = 1, name = "Pane"))
        assertEquals(199L, ShoppingValues.totalCents(values))
        assertEquals(100L, ShoppingValues.totalCents(values.take(1)))
    }
    @Test fun shareIncludesStatusQuantityAndUnits() {
        val text = ShoppingValues.shareText("Sabato", listOf(ShoppingItem(listId = 1, name = "Mele", quantity = 0.5, unit = "kg", isChecked = true)))
        assertTrue(text.contains("Sabato"))
        assertTrue(text.contains("✓ Mele — 0,5 kg"))
    }
    @Test fun releaseVersionsAreComparedNumerically() {
        assertTrue(VersionComparator.isNewer("1.1.0", "v1.10.0"))
        assertTrue(VersionComparator.isNewer("1.1.0", "v2.0.0"))
        assertFalse(VersionComparator.isNewer("1.1.0", "v1.1"))
        assertFalse(VersionComparator.isNewer("1.1.0", "v1.0.9"))
        assertFalse(VersionComparator.isNewer("1.1.0", "v2.0.0-beta"))
        assertFalse(VersionComparator.isNewer("1.1.0", "unexpected"))
    }
}
