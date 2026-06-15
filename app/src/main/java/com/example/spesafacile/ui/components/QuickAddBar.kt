package com.example.spesafacile.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.spesafacile.data.model.Category

@Composable
fun QuickAddBar(
    allItemNames: List<String>,
    onAdd: (name: String, category: String, quantity: Int, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.ALTRO) }
    var quantity by remember { mutableIntStateOf(1) }
    var selectedUnit by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    val units = listOf("pz", "kg", "g", "l", "ml")

    val filteredSuggestions = remember(name, allItemNames) {
        if (name.length >= 2) {
            allItemNames.filter { it.contains(name, ignoreCase = true) }.take(5)
        } else {
            emptyList()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Campo di testo con suggerimenti
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    showSuggestions = it.length >= 2 && filteredSuggestions.isNotEmpty()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Aggiungi prodotto...") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Menu suggerimenti
            DropdownMenu(
                expanded = showSuggestions && filteredSuggestions.isNotEmpty(),
                onDismissRequest = { showSuggestions = false },
                properties = PopupProperties(focusable = false)
            ) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            name = suggestion
                            showSuggestions = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Riga categorie scorrevole
            Text(
                text = "Categoria",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Category.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getCategoryColor(category.name).copy(alpha = 0.2f),
                            selectedLabelColor = getCategoryColor(category.name)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selettore quantità e unità
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantità
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Qtà:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Diminuisci", modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = "$quantity",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Aumenta", modifier = Modifier.size(18.dp))
                    }
                }

                // Unità
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    units.forEach { unit ->
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = {
                                selectedUnit = if (selectedUnit == unit) "" else unit
                            },
                            label = { Text(unit, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pulsante Aggiungi
            FilledTonalButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, selectedCategory.name, quantity, selectedUnit)
                        name = ""
                        selectedCategory = Category.ALTRO
                        quantity = 1
                        selectedUnit = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aggiungi")
            }
        }
    }
}
