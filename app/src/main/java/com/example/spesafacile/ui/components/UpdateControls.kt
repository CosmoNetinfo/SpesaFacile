package com.example.spesafacile.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spesafacile.util.AppUpdateManager

@Composable
fun UpdateControls(manager: AppUpdateManager) {
    val context = LocalContext.current
    val state by manager.state.collectAsStateWithLifecycle()
    val progress = state.progress
    val automatic by manager.autoDownload.collectAsStateWithLifecycle()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (context.packageManager.canRequestPackageInstalls()) manager.install()
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(state.message.ifEmpty { "Controllo automatico all’avvio. Puoi cercare anche manualmente." })
        if (state.downloading) {
            if (progress != null) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            else LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        OutlinedButton(onClick = manager::check, enabled = !state.checking && !state.downloading) { Text("Cerca aggiornamenti") }
        if (state.release != null && !state.downloading && state.readyFile == null) {
            Button(onClick = { manager.download() }) { Text("Scarica ${state.release!!.version} (${state.release!!.bytes / 1_000_000} MB)") }
        }
        if (state.readyFile != null) {
            Button(onClick = {
                if (context.packageManager.canRequestPackageInstalls()) manager.install()
                else permission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
            }) { Text("Installa aggiornamento") }
            Text("Android richiede la conferma dell’installazione.", style = MaterialTheme.typography.bodySmall)
        }
        state.release?.let { release ->
            TextButton(onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.url)))
                } catch (_: android.content.ActivityNotFoundException) {
                    android.widget.Toast.makeText(context, "Nessun browser disponibile sul telefono", android.widget.Toast.LENGTH_LONG).show()
                }
            }) { Text("Scarica dal browser") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Switch(checked = automatic, onCheckedChange = manager::setAutoDownload,
                modifier = Modifier.semantics { contentDescription = "Scarica automaticamente con Wi-Fi" })
            Text("Scarica automaticamente con Wi-Fi", modifier = Modifier.weight(1f))
        }
    }
}
