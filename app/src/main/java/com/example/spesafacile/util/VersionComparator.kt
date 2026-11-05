package com.example.spesafacile.util

object VersionComparator {
    fun isNewer(current: String, latest: String): Boolean {
        fun parts(version: String): List<Int>? {
            val clean = version.removePrefix("v")
            if (!Regex("[0-9]+(\\.[0-9]+){0,3}").matches(clean)) return null
            return clean.split('.').map { it.toIntOrNull() ?: return null }
        }
        val a = parts(current) ?: return false
        val b = parts(latest) ?: return false
        for (i in 0 until maxOf(a.size, b.size)) {
            val comparison = (b.getOrElse(i) { 0 }).compareTo(a.getOrElse(i) { 0 })
            if (comparison != 0) return comparison > 0
        }
        return false
    }
}
