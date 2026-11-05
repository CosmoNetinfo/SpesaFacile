package com.example.spesafacile.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Appearance(val label: String) { SYSTEM("Come il dispositivo"), LIGHT("Chiaro"), DARK("Scuro") }

class AppPreferences private constructor(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("spesafacile_settings", Context.MODE_PRIVATE)
    private val _appearance = MutableStateFlow(Appearance.entries.firstOrNull { it.name == preferences.getString("appearance", "SYSTEM") } ?: Appearance.SYSTEM)
    val appearance = _appearance.asStateFlow()
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
