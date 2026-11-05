package com.example.spesafacile.util

import android.util.Base64
import com.example.spesafacile.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class TransferList(val name: String, val budgetCents: Long?, val items: List<ShoppingItem>)
data class TransferDocument(val lists: List<TransferList>, val products: List<ProductSuggestion> = emptyList())

object TransferCodec {
    const val MAX_BACKUP_BYTES = 2_000_000
    const val MAX_LINK_LENGTH = 12_000
    private const val SITE = "https://cosmonetinfo.github.io/SpesaFacile-landing-page/condividi/#"

    fun backup(document: TransferDocument): String = JSONObject().put("format", "spesafacile-backup")
        .put("version", 1).put("lists", JSONArray(document.lists.map(::listJson)))
        .put("products", JSONArray(document.products.map { product ->
            JSONObject().put("name", product.name).put("category", product.category).put("unit", product.unit).put("favorite", product.isFavorite)
        })).toString(2).also { require(it.toByteArray().size <= MAX_BACKUP_BYTES) { "Backup troppo grande" } }

    fun parseBackup(text: String): TransferDocument {
        require(text.toByteArray().size <= MAX_BACKUP_BYTES) { "Backup troppo grande" }
        val json = JSONObject(text)
        require(json.optString("format") == "spesafacile-backup" && json.optInt("version") == 1) { "Formato backup non supportato" }
        val array = json.getJSONArray("lists")
        require(array.length() <= 1000)
        val lists = (0 until array.length()).map { parseList(array.getJSONObject(it)) }
        require(lists.sumOf { it.items.size } <= 10_000)
        val products = linkedMapOf<String, ProductSuggestion>()
        lists.flatMap { it.items }.forEach { item ->
            val key = item.name.lowercase(Locale.ROOT)
            products[key] = ProductSuggestion(key, item.name, item.category, item.unit)
        }
        val history = json.optJSONArray("products") ?: JSONArray()
        require(history.length() <= 10_000)
        repeat(history.length()) { index ->
            val product = history.getJSONObject(index)
            val name = name(product)
            val category = category(product)
            val unit = unit(product)
            val key = name.lowercase(Locale.ROOT)
            products[key] = ProductSuggestion(key, name, category, unit, isFavorite = product.optBoolean("favorite"))
        }
        return TransferDocument(lists, products.values.toList())
    }

    fun share(list: TransferList): String {
        val json = JSONObject().put("format", "spesafacile-list").put("version", 1).put("list", listJson(list)).toString()
        val bytes = ByteArrayOutputStream().also { output -> GZIPOutputStream(output).use { it.write(json.toByteArray(Charsets.UTF_8)) } }.toByteArray()
        val token = Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        require(token.length <= MAX_LINK_LENGTH) { "Lista troppo lunga per un link: condividila come testo o usa il backup" }
        return SITE + token
    }

    fun parseShare(token: String): TransferDocument {
        require(token.length in 1..MAX_LINK_LENGTH && Regex("[A-Za-z0-9_-]+").matches(token)) { "Link non valido" }
        val bytes = Base64.decode(token, Base64.URL_SAFE or Base64.NO_WRAP)
        val json = GZIPInputStream(ByteArrayInputStream(bytes)).use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                require(output.size() + count <= 256_000) { "Lista troppo grande" }
                output.write(buffer, 0, count)
            }
            JSONObject(output.toString("UTF-8"))
        }
        require(json.optString("format") == "spesafacile-list" && json.optInt("version") == 1) { "Formato link non supportato" }
        val list = parseList(json.getJSONObject("list"))
        val products = list.items.associate { item ->
            val key = item.name.lowercase(Locale.ROOT)
            key to ProductSuggestion(key, item.name, item.category, item.unit)
        }.values.toList()
        return TransferDocument(listOf(list), products)
    }

    fun digest(token: String): String = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
        .joinToString("") { "%02x".format(it.toInt() and 255) }

    private fun listJson(list: TransferList): JSONObject = JSONObject().put("name", list.name)
        .put("budgetCents", list.budgetCents ?: JSONObject.NULL)
        .put("items", JSONArray(list.items.map { item ->
            JSONObject().put("name", item.name).put("category", item.category).put("quantity", item.quantity)
                .put("unit", item.unit).put("priceCents", item.priceCents ?: JSONObject.NULL).put("checked", item.isChecked)
        }))

    private fun parseList(json: JSONObject): TransferList {
        val listName = name(json)
        val budget = cents(json, "budgetCents")
        val items = json.getJSONArray("items")
        require(items.length() <= 1000)
        return TransferList(listName, budget, (0 until items.length()).map { index ->
            val item = items.getJSONObject(index)
            val quantity = item.getDouble("quantity")
            require(quantity.isFinite() && quantity > 0 && quantity <= 1_000_000)
            ShoppingItem(listId = 0, name = name(item), category = category(item), quantity = quantity,
                unit = unit(item), priceCents = cents(item, "priceCents"), isChecked = item.optBoolean("checked"))
        })
    }
    private fun name(json: JSONObject): String = json.getString("name").trim().also { require(it.isNotEmpty() && it.length <= 500) }
    private fun category(json: JSONObject): String = json.optString("category", Category.ALTRO.name).also { value -> require(Category.entries.any { it.name == value }) }
    private fun unit(json: JSONObject): String = json.optString("unit").also { require(it.length <= 20) }
    private fun cents(json: JSONObject, key: String): Long? {
        if (!json.has(key) || json.isNull(key)) return null
        return checkNotNull(json.get(key).toString().toLongOrNull()).also { require(it in 0..100_000_000L) }
    }
}
