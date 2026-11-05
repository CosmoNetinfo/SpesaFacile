package com.example.spesafacile.util

import com.example.spesafacile.data.model.ShoppingItem
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object ShoppingValues {
    fun parseQuantity(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0 && it <= 1_000_000 }

    fun parsePriceCents(text: String): Long? = runCatching {
        val value = BigDecimal(text.trim().replace(',', '.'))
        if (value.signum() < 0 || value > BigDecimal("1000000")) return null
        value.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact()
    }.getOrNull()

    fun quantity(value: Double): String = BigDecimal.valueOf(value).stripTrailingZeros()
        .toPlainString().replace('.', ',')
    fun money(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.ITALY)
        .format(BigDecimal.valueOf(cents, 2))
    fun totalCents(items: List<ShoppingItem>): Long = items.fold(BigDecimal.ZERO) { total, item ->
        total + BigDecimal.valueOf(item.priceCents ?: 0).multiply(BigDecimal.valueOf(item.quantity))
    }.setScale(0, RoundingMode.HALF_UP).longValueExact()
    fun shareText(name: String, items: List<ShoppingItem>): String = buildString {
        appendLine(name)
        items.forEach { item ->
            appendLine("${if (item.isChecked) "✓" else "☐"} ${item.name} — ${quantity(item.quantity)} ${item.unit}".trimEnd())
        }
        if (items.any { it.priceCents != null }) appendLine("Totale stimato: ${money(totalCents(items))}")
    }.trimEnd()
}
