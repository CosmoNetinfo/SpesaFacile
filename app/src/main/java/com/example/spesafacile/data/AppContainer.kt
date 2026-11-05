package com.example.spesafacile.data

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.example.spesafacile.widget.SpesaWidget
import kotlinx.coroutines.CancellationException

object AppContainer {
    fun repository(context: Context): ShoppingRepository {
        val app = context.applicationContext
        return ShoppingRepository(AppDatabase.getDatabase(app)) {
            try {
                SpesaWidget().updateAll(app)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("SpesaFacile", "Aggiornamento widget non riuscito", e)
            }
        }
    }
}
