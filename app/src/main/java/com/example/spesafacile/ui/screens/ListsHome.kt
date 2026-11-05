package com.example.spesafacile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spesafacile.R
import com.example.spesafacile.data.dao.ListSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsHome(
    lists: List<ListSummary>?,
    onListClick: (Int) -> Unit,
    onCreate: () -> Unit,
    onRename: (Int) -> Unit,
    onDuplicate: (ListSummary) -> Unit,
    onDelete: (Int) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Image(painterResource(R.drawable.spesafacile_logo), null, Modifier.size(38.dp))
                        Text("SpesaFacile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (!lists.isNullOrEmpty()) {
                ExtendedFloatingActionButton(onClick = onCreate,
                    icon = { Icon(Icons.Outlined.Add, null) }, text = { Text("Nuova lista") })
            }
        }
    ) { padding ->
        if (lists == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).testTag("home-list"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "welcome") { WelcomeCard(lists) }
                if (lists.isEmpty()) {
                    item(key = "empty") { FirstListCard(onCreate) }
                } else {
                    item(key = "heading") {
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Le tue liste", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold)
                            Text(lists.size.toString(), style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(lists, key = { it.id }) { summary ->
                        ShoppingListCard(summary, { onListClick(summary.id) }, { onRename(summary.id) },
                            { onDuplicate(summary) }, { onDelete(summary.id) })
                    }
                }
                item(key = "footer") {
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PhoneAndroid, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(6.dp))
                        Text("Le tue liste, sempre con te. Anche offline.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(lists: List<ListSummary>) {
    val pending = lists.sumOf { it.uncheckedCount }
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(
            MaterialTheme.colorScheme.primaryContainer,
            lerp(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface, 0.35f)
        ))).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("UN PENSIERO IN MENO", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Cosa serve oggi?", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Icon(Icons.Outlined.ShoppingBag, null, Modifier.size(54.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f))
            }
            Text(when {
                lists.isEmpty() -> "Il foglietto può restare a casa. La tua prossima spesa comincia qui."
                pending > 0 -> "Organizza quello che manca e spuntalo, un prodotto alla volta."
                lists.any { it.totalCount > 0 } -> "Hai spuntato tutto. La prossima spesa è già più semplice."
                else -> "Le tue liste sono pronte: aggiungi quello che ti serve."
            }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (lists.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    SummaryValue(lists.size.toString(), if (lists.size == 1) "lista" else "liste", Modifier.weight(1f))
                    SummaryValue(pending.toString(), "da comprare", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryValue(value: String, label: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun FirstListCard(onCreate: () -> Unit) {
    OutlinedCard(shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                Icon(Icons.Outlined.PlaylistAdd, null, Modifier.padding(16.dp).size(32.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("La prima lista è un buon inizio.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Spesa della settimana, cena con amici o due cose al volo. Scegli un nome e parti da lì.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onCreate, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                Icon(Icons.Outlined.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Crea la tua prima lista")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.CheckCircle, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Aggiungi, organizza e spunta. Al resto pensi dopo.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ShoppingListCard(summary: ListSummary, onClick: () -> Unit, onRename: () -> Unit,
                             onDuplicate: () -> Unit, onDelete: () -> Unit) {
    var menu by rememberSaveable(summary.id) { mutableStateOf(false) }
    val completed = summary.totalCount > 0 && summary.uncheckedCount == 0
    val bought = (summary.totalCount - summary.uncheckedCount).coerceAtLeast(0)
    ElevatedCard(onClick = onClick, shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), shape = RoundedCornerShape(15.dp)) {
                    Icon(if (completed) Icons.Outlined.CheckCircle else Icons.Outlined.ShoppingBasket, null,
                        Modifier.padding(12.dp).size(24.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(summary.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(when {
                        summary.totalCount == 0 -> "Pronta da riempire"
                        completed -> "Tutto nel carrello!"
                        summary.uncheckedCount == 1 -> "1 prodotto da comprare"
                        else -> "${summary.uncheckedCount} prodotti da comprare"
                    }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, "Opzioni ${summary.name}") }
                    DropdownMenu(menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Rinomina") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                            onClick = { menu = false; onRename() })
                        DropdownMenuItem(text = { Text("Duplica") }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) },
                            onClick = { menu = false; onDuplicate() })
                        DropdownMenuItem(text = { Text("Elimina", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { menu = false; onDelete() })
                    }
                }
            }
            if (summary.totalCount > 0) {
                LinearProgressIndicator(progress = { bought.toFloat() / summary.totalCount }, modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (summary.totalCount == 0) "Aggiungi il primo prodotto" else "$bought di ${summary.totalCount} acquistati",
                    Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
