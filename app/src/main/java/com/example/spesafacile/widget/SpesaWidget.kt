package com.example.spesafacile.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.text.*
import com.example.spesafacile.MainActivity
import com.example.spesafacile.data.AppContainer
import com.example.spesafacile.data.AppDatabase
import com.example.spesafacile.util.ShoppingValues
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

val WidgetListKey = intPreferencesKey("list_id")
val WidgetItemKey = ActionParameters.Key<Int>("item_id")

class SpesaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val initialLists = db.shoppingListDao().getAllLists().first()
        provideContent {
            val lists by remember { db.shoppingListDao().getAllLists() }.collectAsState(initialLists)
            val selectedId = currentState<Preferences>()[WidgetListKey]
            val selected = lists.firstOrNull { it.id == selectedId } ?: lists.firstOrNull()
            val itemFlow = remember(selected?.id) {
                selected?.let { db.shoppingItemDao().getItemsByList(it.id) } ?: flowOf(emptyList())
            }
            val allItems by itemFlow.collectAsState(emptyList())
            GlanceTheme {
                Column(GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface).padding(12.dp)) {
                    Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(selected?.name ?: "SpesaFacile",
                            modifier = GlanceModifier.defaultWeight().clickable(openList(context, selected?.id, false)),
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GlanceTheme.colors.onSurface))
                        Button(text = "+", onClick = openList(context, selected?.id, true))
                    }
                    if (lists.size > 1) {
                        Button(text = "Cambia lista", onClick = actionRunCallback<NextListAction>())
                    }
                    val pending = allItems.filter { !it.isChecked }
                    if (selected == null) {
                        Text("Apri l’app per creare una lista", style = TextStyle(color = GlanceTheme.colors.onSurface))
                    } else if (pending.isEmpty()) {
                        Text(if (allItems.isEmpty()) "Lista vuota" else "✓ Tutto acquistato!",
                            style = TextStyle(color = GlanceTheme.colors.onSurface))
                    } else {
                        LazyColumn(GlanceModifier.fillMaxSize()) {
                            items(pending, itemId = { it.id.toLong() }) { item ->
                                CheckBox(
                                    checked = false,
                                    onCheckedChange = actionRunCallback<CheckItemAction>(actionParametersOf(WidgetItemKey to item.id)),
                                    text = "${item.name} · ${ShoppingValues.quantity(item.quantity)} ${item.unit}".trimEnd(),
                                    modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                                    style = TextStyle(color = GlanceTheme.colors.onSurface)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openList(context: Context, listId: Int?, add: Boolean): Action {
        val intent = Intent(context, MainActivity::class.java)
        if (listId != null) {
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("spesafacile://shopping/$listId?add=$add")
        }
        return androidx.glance.appwidget.action.actionStartActivity(intent)
    }
}

class CheckItemAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[WidgetItemKey] ?: return
        AppContainer.repository(context).setChecked(id, parameters[ToggleableStateKey] ?: true)
        SpesaWidget().update(context, glanceId)
    }
}

class NextListAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val lists = AppDatabase.getDatabase(context).shoppingListDao().getAllLists().first()
        if (lists.isEmpty()) return
        updateAppWidgetState(context, glanceId) { preferences ->
            val index = lists.indexOfFirst { it.id == preferences[WidgetListKey] }.coerceAtLeast(0)
            preferences[WidgetListKey] = lists[(index + 1) % lists.size].id
        }
        SpesaWidget().update(context, glanceId)
    }
}
