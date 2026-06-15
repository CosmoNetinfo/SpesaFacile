package com.example.spesafacile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.spesafacile.data.AppDatabase
import com.example.spesafacile.data.ShoppingRepository
import com.example.spesafacile.data.model.Category
import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.data.model.ShoppingList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val repository: ShoppingRepository
    val listId: Int = savedStateHandle.get<Int>("listId") ?: 0

    val items: StateFlow<List<ShoppingItem>>
    val currentList: StateFlow<ShoppingList?>
    val allItemNames: StateFlow<List<String>>

    private val _showGrouped = MutableStateFlow(false)
    val showGrouped: StateFlow<Boolean> = _showGrouped.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ShoppingRepository(db.shoppingListDao(), db.shoppingItemDao())
        items = repository.getItemsByList(listId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        currentList = repository.getListById(listId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        allItemNames = repository.getAllItemNames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addItem(name: String, category: String = Category.ALTRO.name, quantity: Int = 1, unit: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertItem(
                ShoppingItem(
                    listId = listId,
                    name = name.trim(),
                    category = category,
                    quantity = quantity,
                    unit = unit
                )
            )
        }
    }

    fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun updateQuantity(item: ShoppingItem, newQuantity: Int) {
        if (newQuantity < 1) return
        viewModelScope.launch {
            repository.updateItem(item.copy(quantity = newQuantity))
        }
    }

    fun clearChecked() {
        viewModelScope.launch {
            repository.deleteCheckedItems(listId)
        }
    }

    fun toggleGrouped() {
        _showGrouped.value = !_showGrouped.value
    }
}
