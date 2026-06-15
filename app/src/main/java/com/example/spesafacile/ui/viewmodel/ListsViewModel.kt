package com.example.spesafacile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spesafacile.data.AppDatabase
import com.example.spesafacile.data.ShoppingRepository
import com.example.spesafacile.data.model.ShoppingList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ShoppingRepository

    val allLists: StateFlow<List<ShoppingList>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ShoppingRepository(db.shoppingListDao(), db.shoppingItemDao())
        allLists = repository.getAllLists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addList(name: String) {
        viewModelScope.launch {
            repository.insertList(ShoppingList(name = name))
        }
    }

    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {
            repository.deleteList(list)
        }
    }

    fun renameList(list: ShoppingList, newName: String) {
        viewModelScope.launch {
            repository.updateList(list.copy(name = newName))
        }
    }

    fun getUncheckedCount(listId: Int) = repository.getUncheckedCount(listId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
