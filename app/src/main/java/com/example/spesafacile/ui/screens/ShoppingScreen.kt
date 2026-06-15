package com.example.spesafacile.ui.screens

import android.app.Application
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spesafacile.data.model.Category
import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.ui.components.CategorySection
import com.example.spesafacile.ui.components.QuickAddBar
import com.example.spesafacile.ui.components.ShoppingItemRow
import com.example.spesafacile.ui.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    listId: Int,
    onBack: () -> Unit
) {
    val viewModel: ShoppingViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val handle = extras.createSavedStateHandle()
                handle["listId"] = listId
                @Suppress("UNCHECKED_CAST")
                return ShoppingViewModel(app, handle) as T
            }
        }
    )

    val items by viewModel.items.collectAsState()
    val currentList by viewModel.currentList.collectAsState()
    val allItemNames by viewModel.allItemNames.collectAsState()
    val showGrouped by viewModel.showGrouped.collectAsState()

    var showOverflowMenu by remember { mutableStateOf(false) }

    val uncheckedItems = items.filter { !it.isChecked }
    val checkedItems = items.filter { it.isChecked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentList?.name ?: "Lista",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    // Toggle vista raggruppata/piatta
                    IconButton(onClick = { viewModel.toggleGrouped() }) {
                        Icon(
                            if (showGrouped) Icons.Outlined.ViewList else Icons.Outlined.ViewModule,
                            contentDescription = if (showGrouped) "Vista lista" else "Vista raggruppata"
                        )
                    }
                    // Menu overflow
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "Altro")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Svuota acquistati") },
                                onClick = {
                                    viewModel.clearChecked()
                                    showOverflowMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                                },
                                enabled = checkedItems.isNotEmpty()
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra di aggiunta rapida
            QuickAddBar(
                allItemNames = allItemNames,
                onAdd = { name, category, quantity, unit ->
                    viewModel.addItem(name, category, quantity, unit)
                }
            )

            if (items.isEmpty()) {
                // Stato vuoto
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "La lista è vuota",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Aggiungi il primo prodotto!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!showGrouped) {
                        // Vista piatta
                        items(uncheckedItems, key = { it.id }) { item ->
                            ShoppingItemRow(
                                item = item,
                                onToggle = viewModel::toggleItem,
                                onDelete = viewModel::deleteItem,
                                onQuantityChange = viewModel::updateQuantity
                            )
                        }
                    } else {
                        // Vista raggruppata per categoria
                        val groupedItems = uncheckedItems.groupBy { it.category }
                        Category.entries.forEach { category ->
                            val categoryItems = groupedItems[category.name]
                            if (!categoryItems.isNullOrEmpty()) {
                                item(key = "cat_${category.name}") {
                                    CategorySection(
                                        category = category,
                                        items = categoryItems,
                                        onToggleItem = viewModel::toggleItem,
                                        onDeleteItem = viewModel::deleteItem,
                                        onQuantityChange = viewModel::updateQuantity
                                    )
                                }
                            }
                        }
                    }

                    // Sezione acquistati
                    if (checkedItems.isNotEmpty()) {
                        item(key = "checked_header") {
                            CheckedSectionHeader(count = checkedItems.size)
                        }
                        items(checkedItems, key = { "checked_${it.id}" }) { item ->
                            ShoppingItemRow(
                                item = item,
                                onToggle = viewModel::toggleItem,
                                onDelete = viewModel::deleteItem,
                                onQuantityChange = viewModel::updateQuantity
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CheckedSectionHeader(count: Int) {
    var expanded by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Acquistati ($count)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Comprimi" else "Espandi",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
