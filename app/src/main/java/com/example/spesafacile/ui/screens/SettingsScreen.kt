package com.example.spesafacile.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spesafacile.BuildConfig
import com.example.spesafacile.data.*
import com.example.spesafacile.util.*
import com.example.spesafacile.ui.components.UpdateControls
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(bottomBar: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { AppPreferences.get(context) }
    val appearance by preferences.appearance.collectAsStateWithLifecycle()
    val notebookColor by preferences.notebookColor.collectAsStateWithLifecycle()
    val repository = remember { AppContainer.repository(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<TransferDocument?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    fun operation(action: suspend () -> Unit) {
        scope.launch {
            busy = true
            try { action() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { snackbar.showSnackbar("Operazione non riuscita. Controlla il file o la connessione e riprova.") }
            finally { busy = false }
        }
    }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) operation {
            val document = repository.exportBackup()
            withContext(Dispatchers.IO) {
                checkNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter().use { it.write(TransferCodec.backup(document)) }
            }
            snackbar.showSnackbar("Backup salvato")
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) operation {
            pending = withContext(Dispatchers.IO) {
                val text = checkNotNull(context.contentResolver.openInputStream(uri)).use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(4096)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        require(output.size() + count <= TransferCodec.MAX_BACKUP_BYTES)
                        output.write(buffer, 0, count)
                    }
                    output.toString("UTF-8")
                }
                TransferCodec.parseBackup(text)
            }
        }
    }
    Scaffold(bottomBar = bottomBar, snackbarHost = { SnackbarHost(snackbar) }, topBar = { TopAppBar(title = { Text("Il tuo blocco") }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item { NotebookSheet { Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Come lo preferisci?", style = MaterialTheme.typography.titleLarge)
                Text("Scegli l’aspetto del tuo blocco notes.")
                Appearance.entries.forEach { value ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(appearance == value, onClick = { preferences.setAppearance(value) })
                        TextButton(onClick = { preferences.setAppearance(value) }) { Text(value.label) }
                    }
                }
                HorizontalDivider()
                Text("Il colore del tuo blocco", style = MaterialTheme.typography.titleMedium)
                Text("Il colore cambia subito e viene ricordato anche al prossimo avvio.")
                NotebookColor.entries.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { value ->
                            FilterChip(selected = notebookColor == value,
                                onClick = { preferences.setNotebookColor(value) },
                                label = { Text(value.label) }, modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Surface(color = com.example.spesafacile.theme.notebookColors(false, value).primary,
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        modifier = Modifier.size(16.dp)) {}
                                })
                        }
                    }
                }
            } } }
            item { NotebookSheet { Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Le tue liste al sicuro", style = MaterialTheme.typography.titleLarge)
                Text("Esporta liste, prezzi, budget e prodotti abituali in un file. Tienilo in un posto sicuro: contiene i tuoi dati della spesa.")
                Button(onClick = { export.launch("SpesaFacile-backup.json") }, enabled = !busy) { Text("Esporta backup") }
                OutlinedButton(onClick = { import.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, enabled = !busy) { Text("Importa backup") }
                Text("L’importazione aggiunge copie: le liste presenti restano intatte.", style = MaterialTheme.typography.bodySmall)
            } } }
            item { NotebookSheet { Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("SpesaFacile ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleLarge)
                Text("Liste sul telefono, senza account. Per inviare una lista in famiglia usa il link dal pulsante Condividi.")
                Text("Licenza proprietaria · © 2026 CosmoNetinfo", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { operation {
                    notice = withContext(Dispatchers.IO) { context.assets.open("licenses/PROPRIETARY.txt").bufferedReader().use { it.readText() } + "\n\n" + context.assets.open("licenses/NOTICE.txt").bufferedReader().use { it.readText() } + "\n\n" + context.assets.open("licenses/APACHE-2.0.txt").bufferedReader().use { it.readText() } }
                } }) { Text("Licenza e attribuzioni") }
                UpdateControls(AppUpdateManager.get(context))
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/CosmoNetinfo/SpesaFacile"))) }) { Text("Progetto e assistenza") }
            } } }
            if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }
    }
    pending?.let { document -> AlertDialog(onDismissRequest = { if (!busy) pending = null }, title = { Text("Importare il backup?") },
        text = { Text("Aggiungerai ${document.lists.size} liste con ${document.lists.sumOf { it.items.size }} prodotti e ripristinerai ${document.products.size} prodotti abituali. Le liste esistenti non verranno sovrascritte.") },
        confirmButton = { TextButton(enabled = !busy, onClick = { operation { repository.importDocument(document); pending = null; snackbar.showSnackbar("Backup importato") } }) { Text("Importa copie") } },
        dismissButton = { TextButton(enabled = !busy, onClick = { pending = null }) { Text("Annulla") } }) }
    notice?.let { text -> AlertDialog(onDismissRequest = { notice = null }, title = { Text("Licenza e attribuzioni") },
        text = { Text(text, modifier = Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = { notice = null }) { Text("Chiudi") } }) }
}
