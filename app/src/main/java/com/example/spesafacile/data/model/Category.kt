package com.example.spesafacile.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EggAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.Soap
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val displayName: String, val icon: ImageVector) {
    FRUTTA_VERDURA("Frutta e Verdura", Icons.Outlined.Eco),
    LATTICINI("Latticini", Icons.Outlined.EggAlt),
    CARNE_PESCE("Carne e Pesce", Icons.Outlined.SetMeal),
    PANE_PASTA("Pane e Pasta", Icons.Outlined.BakeryDining),
    BEVANDE("Bevande", Icons.Outlined.LocalCafe),
    SURGELATI("Surgelati", Icons.Outlined.AcUnit),
    SNACK_DOLCI("Snack e Dolci", Icons.Outlined.Cookie),
    IGIENE("Igiene", Icons.Outlined.Soap),
    CASA("Casa", Icons.Outlined.Home),
    ALTRO("Altro", Icons.Outlined.MoreHoriz)
}
