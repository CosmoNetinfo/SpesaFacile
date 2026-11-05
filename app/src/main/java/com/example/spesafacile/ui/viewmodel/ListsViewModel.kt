package com.example.spesafacile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spesafacile.data.AppContainer
import com.example.spesafacile.data.model.ShoppingList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ListsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppContainer.repository(application)
    private val _errors = Channel<String>(Channel.BUFFERED)
    val errors = _errors.receiveAsFlow()
    val allLists = repository.getListSummaries()
        .catch { _errors.send("Impossibile caricare le liste") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun change(action: suspend () -> Unit) {
        viewModelScope.launch {
            try { action() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _errors.send("Operazione non riuscita. Riprova.") }
        }
    }
    fun addList(name: String) = change { repository.insertList(ShoppingList(name = name)) }
    fun deleteList(list: ShoppingList) = change { repository.deleteList(list) }
    fun renameList(list: ShoppingList, name: String) = change { repository.updateList(list.copy(name = name)) }
    fun duplicateList(list: ShoppingList) = change { repository.duplicateList(list) }
}
