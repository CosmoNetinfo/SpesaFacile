package com.example.spesafacile.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Appearance(val label: String) { SYSTEM("Come il dispositivo"), LIGHT("Chiaro"), DARK("Scuro") }
enum class NotebookColor(val label: String) { ORANGE("Arancio"), SAGE("Salvia"), BLUE("Azzurro"), ROSE("Rosa") }

class AppPreferences private constructor(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("spesafacile_settings", Context.MODE_PRIVATE)
    private val _appearance = MutableStateFlow(Appearance.entries.firstOrNull { it.name == preferences.getString("appearance", "SYSTEM") } ?: Appearance.SYSTEM)
    val appearance = _appearance.asStateFlow()
    private val _notebookColor = MutableStateFlow(NotebookColor.entries.firstOrNull { it.name == preferences.getString("notebook_color", "ORANGE") } ?: NotebookColor.ORANGE)
    val notebookColor = _notebookColor.asStateFlow()
    fun setNotebookColor(value: NotebookColor) {
        preferences.edit().putString("notebook_color", value.name).apply()
        _notebookColor.value = value
    }
    fun setAppearance(value: Appearance) {
        preferences.edit().putString("appearance", value.name).apply()
        _appearance.value = value
    }
    companion object {
        @Volatile private var instance: AppPreferences? = null
        fun get(context: Context): AppPreferences = instance ?: synchronized(this) {
            instance ?: AppPreferences(context).also { instance = it }
        }
    }
}
