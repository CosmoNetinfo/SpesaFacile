package com.example.spesafacile.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun AppHomeShell(onListClick: (Int) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val bottom: @Composable () -> Unit = {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Outlined.Checklist, null) }, label = { Text("Liste") })
            NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Outlined.Inventory2, null) }, label = { Text("Prodotti") })
            NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Outlined.Settings, null) }, label = { Text("Il mio blocco") })
        }
    }
    when (tab) {
        0 -> ListsScreen(onListClick, bottomBar = bottom)
        1 -> ProductsScreen(bottom, onGoToLists = { tab = 0 })
        2 -> SettingsScreen(bottom)
    }
}
