package com.example.spesafacile.data

import androidx.room.withTransaction
import com.example.spesafacile.data.model.*
import java.util.Locale
import com.example.spesafacile.util.TransferDocument
import kotlinx.coroutines.flow.first

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
        require(list.name.isNotBlank() && list.name.trim().length <= 500)
        return listDao.insert(list.copy(name = list.name.trim())).also { onChanged() }
    }
    suspend fun updateList(list: ShoppingList) {
        require(list.name.isNotBlank() && list.name.trim().length <= 500)
        listDao.update(list.copy(name = list.name.trim()))
        onChanged()
    }
    suspend fun deleteList(list: ShoppingList) { listDao.delete(list); onChanged() }
    suspend fun saveItem(item: ShoppingItem) {
        require(item.name.isNotBlank() && item.name.trim().length <= 500 && item.unit.length <= 20)
        require(item.quantity.isFinite() && item.quantity > 0 && item.quantity <= 1_000_000)
        require(item.priceCents == null || item.priceCents in 0..100_000_000L)
        require(Category.entries.any { it.name == item.category })
        val clean = item.copy(name = item.name.trim(), unit = item.unit.trim())
        db.withTransaction {
            if (clean.id == 0) itemDao.insert(clean) else check(itemDao.updateDetails(
                clean.id, clean.listId, clean.name, clean.category, clean.quantity, clean.unit, clean.priceCents
            ) == 1) { "Prodotto non più disponibile" }
            val key = clean.name.lowercase(Locale.ROOT)
            val favorite = itemDao.getSuggestion(key)?.isFavorite ?: false
            itemDao.saveSuggestion(ProductSuggestion(key, clean.name, clean.category, clean.unit, isFavorite = favorite))
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
            val newId = listDao.insert(ShoppingList(name = "${source.name} (copia)", budgetCents = source.budgetCents)).toInt()
            itemDao.snapshot(source.id).forEach { item ->
                itemDao.insert(item.copy(id = 0, listId = newId, isChecked = false, createdAt = System.currentTimeMillis()))
            }
            newId.toLong()
        }
        onChanged()
        return id
    }

    suspend fun saveProduct(name: String, category: String, unit: String, favorite: Boolean = true) {
        require(name.isNotBlank() && name.length <= 200)
        require(Category.entries.any { it.name == category })
        val clean = name.trim()
        itemDao.saveSuggestion(ProductSuggestion(clean.lowercase(Locale.ROOT), clean, category, unit, isFavorite = favorite))
    }
    suspend fun setFavorite(key: String, favorite: Boolean) = itemDao.setFavorite(key, favorite)
    suspend fun setBudget(id: Int, cents: Long?) {
        require(cents == null || cents in 0..100_000_000L)
        listDao.setBudget(id, cents)
    }
    suspend fun exportBackup(): TransferDocument = db.withTransaction {
        TransferDocument(listDao.snapshot().map { list ->
            com.example.spesafacile.util.TransferList(list.name, list.budgetCents, itemDao.snapshot(list.id))
        }, itemDao.getSuggestions().first())
    }
    suspend fun importDocument(document: TransferDocument, digest: String? = null): List<Int> {
        val ids = db.withTransaction {
            if (digest != null) listDao.findSharedImport(digest)?.let { return@withTransaction listOf(it) }
            val names = listDao.snapshot().map { it.name }.toMutableSet()
            val inserted = document.lists.map { list ->
                var name = list.name
                var suffix = 1
                while (name in names) { name = "${list.name} (importata $suffix)"; suffix++ }
                names.add(name)
                val id = listDao.insert(ShoppingList(name = name, budgetCents = list.budgetCents)).toInt()
                list.items.forEach { item -> itemDao.insert(item.copy(id = 0, listId = id)) }
                id
            }
            document.products.forEach { product ->
                val existing = itemDao.getSuggestion(product.normalizedName)
                itemDao.saveSuggestion(product.copy(isFavorite = product.isFavorite || existing?.isFavorite == true))
            }
            if (digest != null && inserted.size == 1) listDao.saveSharedImport(SharedImport(digest, inserted.single()))
            inserted
        }
        onChanged()
        return ids
    }
}
