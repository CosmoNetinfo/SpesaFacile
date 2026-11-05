package com.example.spesafacile.ui.screens

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spesafacile.data.model.*
import com.example.spesafacile.ui.components.*
import com.example.spesafacile.ui.viewmodel.*
import com.example.spesafacile.util.ShoppingValues
import com.example.spesafacile.util.TransferCodec
import com.example.spesafacile.util.TransferList
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.math.BigDecimal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(listId: Int, onBack: () -> Unit, focusAdd: Boolean = false) {
    val model: ShoppingViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
            val state = extras.createSavedStateHandle().apply { set("listId", listId) }
            @Suppress("UNCHECKED_CAST")
            return ShoppingViewModel(app, state) as T
        }
    })
    val items by model.items.collectAsStateWithLifecycle()
    val list by model.currentList.collectAsStateWithLifecycle()
    val suggestions by model.suggestions.collectAsStateWithLifecycle()
    val grouped by model.showGrouped.collectAsStateWithLifecycle()
    var checkedExpanded by rememberSaveable { mutableStateOf(true) }
    var clearDialog by rememberSaveable { mutableStateOf(false) }
    var editId by rememberSaveable { mutableStateOf<Int?>(null) }
    var reset by rememberSaveable { mutableIntStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var shareMenu by remember { mutableStateOf(false) }
    var optionsMenu by remember { mutableStateOf(false) }
    var budgetDialog by remember { mutableStateOf(false) }
    fun shareText(text: String) {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
        }, "Condividi lista"))
    }
    val unchecked = items.filter { !it.isChecked }
    val checked = items.filter { it.isChecked }
    LaunchedEffect(model) {
        model.events.collect { event ->
            when (event) {
                ShoppingEvent.Saved -> {
                    saving = false
                    if (editId == null) reset++ else editId = null
                }
                is ShoppingEvent.Message -> {
                    saving = false
                    launch { snackbar.showSnackbar(event.text) }
                }
                is ShoppingEvent.Deleted -> launch {
                    snackbar.currentSnackbarData?.dismiss()
                    if (snackbar.showSnackbar("${event.item.name} eliminato", "Annulla", withDismissAction = true) == SnackbarResult.ActionPerformed) {
                        model.restoreItem(event.item)
                    }
                }
            }
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, topBar = {
        TopAppBar(title = { Text(list?.name ?: "Lista", maxLines = 1) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro") } },
            actions = {
                IconButton(onClick = model::toggleGrouped) { Icon(if (grouped) Icons.Default.ViewList else Icons.Default.Category, if (grouped) "Vista lista" else "Raggruppa per categoria") }
                Box {
                    IconButton(onClick = { shareMenu = true }, enabled = list != null) { Icon(Icons.Default.Share, "Condividi lista") }
                    DropdownMenu(shareMenu, onDismissRequest = { shareMenu = false }) {
                        DropdownMenuItem(text = { Text("Invia link all’app") }, onClick = {
                            shareMenu = false
                            scope.launch {
                                try {
                                    val link = TransferCodec.share(TransferList(list?.name ?: "Spesa", list?.budgetCents, items))
                                    shareText("Apri la lista della spesa in SpesaFacile:\n$link")
                                } catch (_: IllegalArgumentException) { snackbar.showSnackbar("Lista troppo lunga per un link. Usa la condivisione come testo.") }
                            }
                        })
                        DropdownMenuItem(text = { Text("Invia come testo") }, onClick = {
                            shareMenu = false; shareText(ShoppingValues.shareText(list?.name ?: "Spesa", items))
                        })
                    }
                }
                Box {
                    IconButton(onClick = { optionsMenu = true }) { Icon(Icons.Default.MoreVert, "Opzioni della lista") }
                    DropdownMenu(optionsMenu, onDismissRequest = { optionsMenu = false }) {
                        DropdownMenuItem(text = { Text("Imposta budget") }, onClick = { optionsMenu = false; budgetDialog = true })
                        DropdownMenuItem(text = { Text("Svuota acquistati") }, enabled = checked.isNotEmpty(), onClick = { optionsMenu = false; clearDialog = true })
                    }
                }
            })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            key(reset) {
                QuickAddBar(listId, suggestions, focusAdd || reset > 0, saving) {
                    saving = true; model.saveItem(it)
                }
            }
            if (items.any { it.priceCents != null }) {
                Text("Totale stimato: ${ShoppingValues.money(ShoppingValues.totalCents(items))}" +
                    if (items.any { it.priceCents == null }) " · alcuni prezzi mancanti" else "",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium)
            }
            list?.budgetCents?.let { budget ->
                val total = ShoppingValues.totalCents(items)
                val difference = budget - total
                Text("Budget: "+ShoppingValues.money(budget)+" · "+
                    (if (difference >= 0) "Rimangono "+ShoppingValues.money(difference) else "Oltre di "+ShoppingValues.money(-difference)),
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (difference < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(progress = { if (budget > 0) (total.toFloat() / budget).coerceIn(0f, 1f) else if (total > 0) 1f else 0f },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
            if (items.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("Aggiungi il primo prodotto") }
            } else {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
                    if (grouped) {
                        val categories = unchecked.groupBy { it.category }
                        Category.entries.forEach { category ->
                            val values = categories[category.name].orEmpty()
                            if (values.isNotEmpty()) item(key = "category_${category.name}") {
                                CategorySection(category, values, { model.toggleItem(it) }, { model.deleteItem(it) }, { editId = it.id })
                            }
                        }
                    } else {
                        items(unchecked, key = { it.id }) { item ->
                            ShoppingItemRow(item, { model.toggleItem(it) }, { model.deleteItem(it) }, { editId = it.id })
                        }
                    }
                    if (checked.isNotEmpty()) {
                        item(key = "checked_header") {
                            Row(Modifier.fillMaxWidth().clickable { checkedExpanded = !checkedExpanded }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Acquistati (${checked.size})", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                                Icon(if (checkedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    if (checkedExpanded) "Comprimi acquistati" else "Espandi acquistati")
                            }
                        }
                        if (checkedExpanded) items(checked, key = { it.id }) { item ->
                            ShoppingItemRow(item, { model.toggleItem(it) }, { model.deleteItem(it) }, { editId = it.id })
                        }
                    }
                }
            }
        }
    }
    items.firstOrNull { it.id == editId }?.let { item ->
        ProductEditor(item, saving, { editId = null }) { saving = true; model.saveItem(it) }
    }
    if (budgetDialog) {
        var amount by remember(list?.budgetCents) { mutableStateOf(list?.budgetCents?.let { BigDecimal.valueOf(it, 2).toPlainString().replace('.', ',') } ?: "") }
        val cents = ShoppingValues.parsePriceCents(amount)
        AlertDialog(onDismissRequest = { budgetDialog = false }, title = { Text("Budget della lista") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(amount, onValueChange = { amount = it }, label = { Text("Quanto vuoi spendere (€)?") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = amount.isNotBlank() && cents == null)
                Text("Il confronto usa i prezzi inseriti. I prodotti senza prezzo non sono inclusi nel totale.")
            } }, confirmButton = { TextButton(onClick = { model.setBudget(cents); budgetDialog = false }, enabled = amount.isBlank() || cents != null) { Text("Salva") } },
            dismissButton = { TextButton(onClick = { model.setBudget(null); budgetDialog = false }) { Text("Rimuovi budget") } })
    }
    if (clearDialog) AlertDialog(onDismissRequest = { clearDialog = false },
        title = { Text("Eliminare gli acquistati?") },
        text = { Text("Verranno eliminati ${checked.size} prodotti. Lo storico dei suggerimenti verrà conservato.") },
        confirmButton = { TextButton(onClick = { model.clearChecked(); clearDialog = false }) { Text("Elimina") } },
        dismissButton = { TextButton(onClick = { clearDialog = false }) { Text("Annulla") } })
}
