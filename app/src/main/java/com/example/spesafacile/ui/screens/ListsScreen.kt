package com.example.spesafacile.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spesafacile.ui.viewmodel.ListsViewModel
import com.example.spesafacile.util.UpdateChecker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(onListClick: (Int) -> Unit, viewModel: ListsViewModel = viewModel()) {
    val lists by viewModel.allLists.collectAsStateWithLifecycle()
    var createDialog by rememberSaveable { mutableStateOf(false) }
    var renameId by rememberSaveable { mutableStateOf<Int?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<Int?>(null) }
    var version by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) { viewModel.errors.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(Unit) { version = UpdateChecker.checkForUpdate() }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text("Le mie liste") }) },
        floatingActionButton = { FloatingActionButton(onClick = { createDialog = true }) { Icon(Icons.Default.Add, "Nuova lista") } }) { padding ->
        when {
            lists == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            lists!!.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Tocca + per creare la tua prima lista") }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lists.orEmpty(), key = { it.id }) { summary ->
                    var menu by remember { mutableStateOf(false) }
                    ElevatedCard(Modifier.fillMaxWidth().clickable { onListClick(summary.id) }) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(summary.name, style = MaterialTheme.typography.titleLarge)
                                Text(when {
                                    summary.totalCount == 0 -> "Lista vuota"
                                    summary.uncheckedCount == 0 -> "Tutto acquistato!"
                                    summary.uncheckedCount == 1 -> "1 prodotto da comprare"
                                    else -> "${summary.uncheckedCount} prodotti da comprare"
                                }, style = MaterialTheme.typography.bodyMedium)
                            }
                            Box {
                                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Opzioni ${summary.name}") }
                                DropdownMenu(menu, onDismissRequest = { menu = false }) {
                                    DropdownMenuItem(text = { Text("Rinomina") }, onClick = { menu = false; renameId = summary.id })
                                    DropdownMenuItem(text = { Text("Duplica") }, onClick = { menu = false; viewModel.duplicateList(summary.asList()) })
                                    DropdownMenuItem(text = { Text("Elimina", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; deleteId = summary.id })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (createDialog) ListNameDialog("Nuova lista", "", { createDialog = false }) {
        viewModel.addList(it); createDialog = false
    }
    lists?.firstOrNull { it.id == renameId }?.let { summary ->
        ListNameDialog("Rinomina lista", summary.name, { renameId = null }) {
            viewModel.renameList(summary.asList(), it); renameId = null
        }
    }
    lists?.firstOrNull { it.id == deleteId }?.let { summary ->
        AlertDialog(onDismissRequest = { deleteId = null }, title = { Text("Eliminare ${summary.name}?") },
            text = { Text("La lista e i suoi ${summary.totalCount} prodotti verranno eliminati definitivamente.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteList(summary.asList()); deleteId = null }) { Text("Elimina") } },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Annulla") } })
    }
    version?.let { tag ->
        AlertDialog(onDismissRequest = { version = null }, title = { Text("Aggiornamento disponibile") },
            text = { Text("È disponibile SpesaFacile $tag.") },
            confirmButton = { TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/CosmoNetinfo/SpesaFacile/releases/latest")))
                version = null
            }) { Text("Scarica") } },
            dismissButton = { TextButton(onClick = { version = null }) { Text("Più tardi") } })
    }
}

@Composable
private fun ListNameDialog(title: String, initial: String, dismiss: () -> Unit, confirm: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) },
        text = { OutlinedTextField(name, onValueChange = { name = it }, label = { Text("Nome della lista") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { confirm(name.trim()) }, enabled = name.isNotBlank()) { Text("Salva") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("Annulla") } })
}
