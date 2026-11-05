package com.example.spesafacile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.spesafacile.data.AppContainer
import com.example.spesafacile.data.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ShoppingEvent {
    data class Message(val text: String) : ShoppingEvent
    data class Deleted(val item: ShoppingItem) : ShoppingEvent
    data object Saved : ShoppingEvent
}

class ShoppingViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {
    private val repository = AppContainer.repository(application)
    val listId: Int = checkNotNull(state["listId"])
    private val _events = Channel<ShoppingEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    val items = repository.getItemsByList(listId)
        .catch { _events.send(ShoppingEvent.Message("Impossibile caricare i prodotti")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentList = repository.getListById(listId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val suggestions = repository.getSuggestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val showGrouped = state.getStateFlow("grouped", false)

    private fun change(action: suspend () -> Unit) = viewModelScope.launch {
        try { action() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { _events.send(ShoppingEvent.Message("Operazione non riuscita. Riprova.")) }
    }
    fun saveItem(item: ShoppingItem) = change {
        repository.saveItem(item)
        _events.send(ShoppingEvent.Saved)
    }
    fun toggleItem(item: ShoppingItem) = change { repository.setChecked(item.id, !item.isChecked) }
    fun deleteItem(item: ShoppingItem) = change {
        repository.deleteItem(item)
        _events.send(ShoppingEvent.Deleted(item))
    }
    fun restoreItem(item: ShoppingItem) = change { repository.restoreItem(item) }
    fun clearChecked() = change { repository.deleteCheckedItems(listId) }
    fun setBudget(cents: Long?) = change {
        repository.setBudget(listId, cents)
        _events.send(ShoppingEvent.Message("Budget aggiornato"))
    }
    fun toggleGrouped() { state["grouped"] = !showGrouped.value }
}
