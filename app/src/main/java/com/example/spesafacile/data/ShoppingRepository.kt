package com.example.spesafacile.data

import androidx.room.withTransaction
import com.example.spesafacile.data.model.*
import java.util.Locale

class ShoppingRepository(
    private val db: AppDatabase,
    private val onChanged: suspend () -> Unit = {}
) {
    private val listDao = db.shoppingListDao()
    private val itemDao = db.shoppingItemDao()
    fun getAllLists() = listDao.getAllLists()
    fun getListSummaries() = listDao.getListSummaries()
    fun getListById(id: Int) = listDao.getListById(id)
    fun getItemsByList(id: Int) = itemDao.getItemsByList(id)
    fun getSuggestions() = itemDao.getSuggestions()

    suspend fun insertList(list: ShoppingList): Long {
        require(list.name.isNotBlank())
        return listDao.insert(list.copy(name = list.name.trim())).also { onChanged() }
    }
    suspend fun updateList(list: ShoppingList) {
        require(list.name.isNotBlank())
        listDao.update(list.copy(name = list.name.trim()))
        onChanged()
    }
    suspend fun deleteList(list: ShoppingList) { listDao.delete(list); onChanged() }
    suspend fun saveItem(item: ShoppingItem) {
        require(item.name.isNotBlank())
        require(item.quantity.isFinite() && item.quantity > 0 && item.quantity <= 1_000_000)
        require(item.priceCents == null || item.priceCents in 0..100_000_000L)
        require(Category.entries.any { it.name == item.category })
        val clean = item.copy(name = item.name.trim(), unit = item.unit.trim())
        db.withTransaction {
            if (clean.id == 0) itemDao.insert(clean) else check(itemDao.updateDetails(
                clean.id, clean.listId, clean.name, clean.category, clean.quantity, clean.unit, clean.priceCents
            ) == 1) { "Prodotto non più disponibile" }
            itemDao.saveSuggestion(ProductSuggestion(clean.name.lowercase(Locale.ROOT), clean.name, clean.category, clean.unit))
        }
        onChanged()
    }
    suspend fun setChecked(id: Int, checked: Boolean) { itemDao.setChecked(id, checked); onChanged() }
    suspend fun setQuantity(id: Int, quantity: Double) {
        require(quantity.isFinite() && quantity > 0 && quantity <= 1_000_000)
        itemDao.setQuantity(id, quantity)
        onChanged()
    }
    suspend fun deleteItem(item: ShoppingItem) { itemDao.delete(item); onChanged() }
    suspend fun restoreItem(item: ShoppingItem) { itemDao.restore(item); onChanged() }
    suspend fun deleteCheckedItems(id: Int) { itemDao.deleteCheckedItems(id); onChanged() }
    suspend fun duplicateList(source: ShoppingList): Long {
        val id = db.withTransaction {
            val newId = listDao.insert(ShoppingList(name = "${source.name} (copia)")).toInt()
            itemDao.snapshot(source.id).forEach { item ->
                itemDao.insert(item.copy(id = 0, listId = newId, isChecked = false, createdAt = System.currentTimeMillis()))
            }
            newId.toLong()
        }
        onChanged()
        return id
    }
}
