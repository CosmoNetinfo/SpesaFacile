package com.example.spesafacile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.spesafacile.data.model.*
import com.example.spesafacile.util.ShoppingValues

@Composable
fun QuickAddBar(listId: Int, suggestions: List<ProductSuggestion>, focusAdd: Boolean, saving: Boolean,
                onSave: (ShoppingItem) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
            ProductForm(ShoppingItem(listId = listId, name = ""), suggestions, focusAdd, saving, true, onSave)
        }
    }
}

@Composable
fun ProductEditor(item: ShoppingItem, saving: Boolean, onDismiss: () -> Unit, onSave: (ShoppingItem) -> Unit) {
    AlertDialog(onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Modifica prodotto") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) {
            ProductForm(item, emptyList(), false, saving, false, onSave)
        } },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Annulla") } })
}

@Composable
private fun ProductForm(initial: ShoppingItem, suggestions: List<ProductSuggestion>, focusAdd: Boolean,
                        saving: Boolean, compact: Boolean, onSave: (ShoppingItem) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var category by rememberSaveable { mutableStateOf(initial.category) }
    var quantity by rememberSaveable { mutableStateOf(ShoppingValues.quantity(initial.quantity)) }
    var unit by rememberSaveable { mutableStateOf(initial.unit) }
    var price by rememberSaveable { mutableStateOf(initial.priceCents?.let { java.math.BigDecimal.valueOf(it, 2).toPlainString().replace('.', ',') } ?: "") }
    var expanded by rememberSaveable { mutableStateOf(!compact) }
    var showSuggestions by remember { mutableStateOf(false) }
    var unitMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var focusConsumed by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusAdd) {
        if (focusAdd && !focusConsumed) { focusRequester.requestFocus(); focusConsumed = true }
    }
    val parsedQuantity = ShoppingValues.parseQuantity(quantity)
    val parsedPrice = ShoppingValues.parsePriceCents(price)
    val valid = name.isNotBlank() && parsedQuantity != null && (price.isBlank() || parsedPrice != null)
    val submit = {
        if (valid && !saving) onSave(initial.copy(name = name, category = category,
            quantity = parsedQuantity!!, unit = unit, priceCents = parsedPrice))
    }
    val matches = if (showSuggestions && name.trim().length >= 2) suggestions
        .filter { it.name.contains(name.trim(), ignoreCase = true) }.take(5) else emptyList()

    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(500); showSuggestions = true },
                    label = { Text("Prodotto") }, singleLine = true, enabled = !saving,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }))
                DropdownMenu(expanded = matches.isNotEmpty(), onDismissRequest = { showSuggestions = false },
                    properties = androidx.compose.ui.window.PopupProperties(focusable = false)) {
                    matches.forEach { suggestion ->
                        DropdownMenuItem(text = { Text(suggestion.name) }, onClick = {
                            name = suggestion.name; category = suggestion.category; unit = suggestion.unit
                            showSuggestions = false
                        })
                    }
                }
            }
            if (compact) {
                IconButton(onClick = { submit() }, enabled = valid && !saving) {
                    Icon(Icons.Default.Add, "Aggiungi prodotto")
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        if (expanded) "Nascondi dettagli" else "Quantità, categoria e prezzo")
                }
            }
        }
        if (expanded) {
            Box {
                OutlinedButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth(), enabled = !saving) {
                    Text(Category.entries.firstOrNull { it.name == category }?.displayName ?: "Altro")
                }
                DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    Category.entries.forEach { value ->
                        DropdownMenuItem(text = { Text(value.displayName) }, onClick = { category = value.name; categoryMenu = false })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantità") },
                    isError = parsedQuantity == null, enabled = !saving, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                Box(Modifier.weight(1f)) {
                    OutlinedButton(onClick = { unitMenu = true }, modifier = Modifier.fillMaxWidth(), enabled = !saving) {
                        Text(unit.ifEmpty { "Unità" })
                    }
                    DropdownMenu(expanded = unitMenu, onDismissRequest = { unitMenu = false }) {
                        listOf("", "pz", "kg", "g", "l", "ml").forEach { value ->
                            DropdownMenuItem(text = { Text(value.ifEmpty { "Nessuna" }) }, onClick = { unit = value; unitMenu = false })
                        }
                    }
                }
            }
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Prezzo per unità (€), facoltativo") },
                isError = price.isNotBlank() && parsedPrice == null, enabled = !saving, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            if (parsedQuantity == null) Text("Inserisci una quantità maggiore di zero", color = MaterialTheme.colorScheme.error)
            if (price.isNotBlank() && parsedPrice == null) Text("Inserisci un prezzo valido con al massimo due decimali", color = MaterialTheme.colorScheme.error)
        }
        if (!compact) Button(onClick = { submit() }, enabled = valid && !saving, modifier = Modifier.fillMaxWidth()) {
            Text(if (saving) "Salvataggio…" else "Salva")
        }
    }
}
