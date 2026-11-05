package com.example.spesafacile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.spesafacile.data.AppContainer
import com.example.spesafacile.util.*
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedListScreen(token: String, onOpen: (Int) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AppContainer.repository(context) }
    var document by remember(token) { mutableStateOf<TransferDocument?>(null) }
    var error by remember(token) { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(token) {
        try { document = withContext(Dispatchers.Default) { TransferCodec.parseShare(token) } }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { error = "Il link non è valido o contiene una lista troppo grande." }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Una lista per te") }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Da un blocco all’altro.", style = MaterialTheme.typography.headlineMedium) }
            error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
            document?.lists?.singleOrNull()?.let { list ->
                item { NotebookSheet { Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(list.name, style = MaterialTheme.typography.titleLarge)
                    Text("${list.items.size} prodotti · quantità e spunte incluse")
                    list.items.take(8).forEach { item -> Text("${if (item.isChecked) "✓" else "○"} ${item.name} · ${ShoppingValues.quantity(item.quantity)} ${item.unit}") }
                    if (list.items.size > 8) Text("E altri ${list.items.size - 8} prodotti")
                } } }
                item { Text("Salverai una copia sul tuo telefono. Le modifiche restano nella tua copia; per inviarle basta condividere un nuovo link.") }
                item { Button(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                    val parsed = document ?: return@Button
                    scope.launch {
                        busy = true
                        try { onOpen(repository.importDocument(parsed, TransferCodec.digest(token)).single()) }
                        catch (e: CancellationException) { throw e }
                        catch (_: Exception) { error = "Impossibile salvare la lista. Riprova." }
                        finally { busy = false }
                    }
                }) { Text(if (busy) "Apertura…" else "Salva e apri la lista") } }
            }
            if (document == null && error == null) item { CircularProgressIndicator() }
            item { TextButton(onClick = onCancel, enabled = !busy) { Text("Torna alle mie liste") } }
        }
    }
}
