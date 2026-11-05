package com.example.spesafacile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spesafacile.data.AppPreferences
import com.example.spesafacile.data.Appearance
import com.example.spesafacile.ui.components.UpdatePrompt
import com.example.spesafacile.theme.SpesaFacileTheme
import com.example.spesafacile.ui.navigation.SpesaFacileNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val appearance by AppPreferences.get(applicationContext).appearance.collectAsStateWithLifecycle()
            val dark = when (appearance) {
                Appearance.SYSTEM -> isSystemInDarkTheme()
                Appearance.LIGHT -> false
                Appearance.DARK -> true
            }
            SpesaFacileTheme(darkTheme = dark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpesaFacileNavGraph()
                    UpdatePrompt()
                }
            }
        }
    }
}
