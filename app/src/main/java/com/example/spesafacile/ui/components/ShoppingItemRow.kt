package com.example.spesafacile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.spesafacile.data.model.*
import com.example.spesafacile.util.ShoppingValues

@Composable
fun ShoppingItemRow(item: ShoppingItem, onToggle: (ShoppingItem) -> Unit,
                    onDelete: (ShoppingItem) -> Unit, onEdit: (ShoppingItem) -> Unit) {
    val dismiss = rememberSwipeToDismissBoxState()
    val delete by rememberUpdatedState(onDelete)
    LaunchedEffect(dismiss.currentValue) {
        if (dismiss.currentValue == SwipeToDismissBoxValue.EndToStart) {
            delete(item)
            dismiss.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(state = dismiss, enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer).padding(16.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Outlined.Delete, "Elimina prodotto", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }) {
        Surface {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.isChecked, onCheckedChange = { onToggle(item) })
                Column(Modifier.weight(1f).clickable(onClickLabel = "Modifica prodotto") { onEdit(item) }.padding(8.dp)) {
                    Text(item.name, style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None)
                    Text("${ShoppingValues.quantity(item.quantity)} ${item.unit} · ${Category.entries.firstOrNull { it.name == item.category }?.displayName ?: "Altro"}".trim(),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    item.priceCents?.let { Text("${ShoppingValues.money(it)} per ${item.unit.ifEmpty { "unità" }}", style = MaterialTheme.typography.bodySmall) }
                }
                IconButton(onClick = { onDelete(item) }) { Icon(Icons.Outlined.Delete, "Elimina ${item.name}") }
            }
        }
    }
}
