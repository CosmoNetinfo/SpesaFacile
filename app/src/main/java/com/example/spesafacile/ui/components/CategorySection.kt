package com.example.spesafacile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spesafacile.data.model.*

@Composable
fun CategorySection(category: Category, items: List<ShoppingItem>, onToggleItem: (ShoppingItem) -> Unit,
                    onDeleteItem: (ShoppingItem) -> Unit, onEditItem: (ShoppingItem) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    Column {
        Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(category.icon, null, tint = MaterialTheme.colorScheme.primary)
            Text("${category.displayName} (${items.size})", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, if (expanded) "Comprimi" else "Espandi")
        }
        if (expanded) items.forEach { item ->
            key(item.id) { ShoppingItemRow(item, onToggleItem, onDeleteItem, onEditItem) }
        }
    }
}
