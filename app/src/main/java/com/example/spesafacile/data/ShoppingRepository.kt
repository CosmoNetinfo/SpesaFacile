package com.example.spesafacile.data

import com.example.spesafacile.data.dao.ShoppingItemDao
import com.example.spesafacile.data.dao.ShoppingListDao
import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.data.model.ShoppingList

class ShoppingRepository(
    private val listDao: ShoppingListDao,
    private val itemDao: ShoppingItemDao
) {
    // Liste
    fun getAllLists() = listDao.getAllLists()
    fun getListById(id: Int) = listDao.getListById(id)
    suspend fun insertList(list: ShoppingList) = listDao.insert(list)
    suspend fun updateList(list: ShoppingList) = listDao.update(list)
    suspend fun deleteList(list: ShoppingList) = listDao.delete(list)

    // Articoli
    fun getItemsByList(listId: Int) = itemDao.getItemsByList(listId)
    fun getUncheckedCount(listId: Int) = itemDao.getUncheckedCount(listId)
    fun getAllItemNames() = itemDao.getAllItemNames()
    suspend fun insertItem(item: ShoppingItem) = itemDao.insert(item)
    suspend fun updateItem(item: ShoppingItem) = itemDao.update(item)
    suspend fun deleteItem(item: ShoppingItem) = itemDao.delete(item)
    suspend fun deleteCheckedItems(listId: Int) = itemDao.deleteCheckedItems(listId)
}
