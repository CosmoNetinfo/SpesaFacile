package com.example.spesafacile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spesafacile.data.model.*
import com.example.spesafacile.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(bottomBar: @Composable () -> Unit, onGoToLists: () -> Unit, model: LibraryViewModel = viewModel()) {
    val products by model.products.collectAsStateWithLifecycle()
    val lists by model.lists.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var favorites by rememberSaveable { mutableStateOf(false) }
    var newProduct by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ProductSuggestion?>(null) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(model) { model.messages.collect { snackbar.showSnackbar(it) } }
    val visible = products.filter { (!favorites || it.isFavorite) && it.name.contains(query.trim(), ignoreCase = true) }
    Scaffold(bottomBar = bottomBar, snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text("Prodotti abituali") }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { newProduct = true }, modifier = Modifier.testTag("new-product"), icon = { Icon(Icons.Outlined.Add, null) }, text = { Text("Nuovo prodotto") }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Il tuo piccolo promemoria", style = MaterialTheme.typography.headlineSmall)
                Text("Ritrova i prodotti usati e aggiungili alle liste senza riscriverli.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { OutlinedTextField(query, onValueChange = { query = it }, singleLine = true, label = { Text("Cerca un prodotto") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, modifier = Modifier.fillMaxWidth()) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!favorites, onClick = { favorites = false }, label = { Text("Tutti") })
                FilterChip(favorites, onClick = { favorites = true }, label = { Text("Preferiti") }, leadingIcon = { Icon(Icons.Outlined.Star, null, Modifier.size(18.dp)) })
            } }
            if (visible.isEmpty()) item {
                NotebookSheet(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Inventory2, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(if (products.isEmpty()) "Le buone abitudini cominciano qui." else "Nessun prodotto trovato.", style = MaterialTheme.typography.titleMedium)
                        Text(if (products.isEmpty()) "Aggiungi un prodotto o compilalo in una lista: lo ritroverai qui. La stella lo tiene tra i preferiti." else "Prova un altro nome o cambia filtro.")
                    }
                }
            }
            items(visible, key = { it.normalizedName }) { product ->
                NotebookSheet(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { model.favorite(product) }) { Icon(if (product.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            if (product.isFavorite) "Rimuovi ${product.name} dai preferiti" else "Aggiungi ${product.name} ai preferiti", tint = MaterialTheme.colorScheme.primary) }
                        Column(Modifier.weight(1f).padding(8.dp)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text(Category.entries.firstOrNull { it.name == product.category }?.displayName ?: "Altro", style = MaterialTheme.typography.bodySmall)
                            if (product.unit.isNotBlank()) Text(product.unit, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { selected = product }) { Icon(Icons.Outlined.AddCircleOutline, "Aggiungi ${product.name} a una lista") }
                    }
                }
            }
        }
    }
    if (newProduct) ProductNameDialog(onDismiss = { newProduct = false }) { name, category, unit ->
        model.save(name, category, unit); newProduct = false
    }
    selected?.let { product ->
        AlertDialog(onDismissRequest = { selected = null }, title = { Text("Dove aggiungo ${product.name}?") },
            text = {
                if (lists.isEmpty()) Text("Crea prima una lista della spesa.")
                else LazyColumn { items(lists, key = { it.id }) { list ->
                    TextButton(onClick = { model.addToList(product, list.id); selected = null }) { Text(list.name) }
                } }
            }, confirmButton = {
                if (lists.isEmpty()) TextButton(onClick = { selected = null; onGoToLists() }) { Text("Vai alle liste") }
            }, dismissButton = { TextButton(onClick = { selected = null }) { Text("Annulla") } })
    }
}

@Composable
private fun ProductNameDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(Category.ALTRO.name) }
    var unit by rememberSaveable { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Nuovo prodotto abituale") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name, onValueChange = { name = it.take(200) }, label = { Text("Nome") }, singleLine = true)
            Box {
                OutlinedButton(onClick = { menu = true }) { Text(Category.valueOf(category).displayName) }
                DropdownMenu(menu, onDismissRequest = { menu = false }) { Category.entries.forEach { value ->
                    DropdownMenuItem(text = { Text(value.displayName) }, onClick = { category = value.name; menu = false })
                } }
            }
            OutlinedTextField(unit, onValueChange = { unit = it.take(20) }, label = { Text("Unità (es. kg, pz)") }, singleLine = true)
            Text("Verrà aggiunto ai preferiti.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(onClick = { onSave(name.trim(), category, unit.trim()) }, enabled = name.isNotBlank()) { Text("Salva") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } })
}
