package com.example.spesafacile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spesafacile.util.AppUpdateManager

@Composable
fun UpdatePrompt() {
    val context = LocalContext.current
    val manager = remember { AppUpdateManager.get(context) }
    val state by manager.state.collectAsStateWithLifecycle()
    var dismissed by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { manager.check() }
    val tag = state.release?.version
    if (tag != null && tag != dismissed) {
        AlertDialog(onDismissRequest = { dismissed = tag }, title = { Text("SpesaFacile $tag") },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { UpdateControls(manager) } },
            confirmButton = {}, dismissButton = { TextButton(onClick = { dismissed = tag }) { Text("Più tardi") } })
    }
}
