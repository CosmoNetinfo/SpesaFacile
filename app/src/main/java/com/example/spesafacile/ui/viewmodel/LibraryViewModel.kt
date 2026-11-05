package com.example.spesafacile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spesafacile.data.AppContainer
import com.example.spesafacile.data.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppContainer.repository(application)
    val products = repository.getSuggestions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lists = repository.getAllLists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()
    private fun change(action: suspend () -> Unit) = viewModelScope.launch {
        try { action() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { _messages.send("Operazione non riuscita. Riprova.") }
    }
    fun favorite(product: ProductSuggestion) = change { repository.setFavorite(product.normalizedName, !product.isFavorite) }
    fun save(name: String, category: String, unit: String) = change { repository.saveProduct(name, category, unit) }
    fun addToList(product: ProductSuggestion, listId: Int) = change {
        repository.saveItem(ShoppingItem(listId = listId, name = product.name, category = product.category, unit = product.unit))
        _messages.send("${product.name} aggiunto alla lista")
    }
}
