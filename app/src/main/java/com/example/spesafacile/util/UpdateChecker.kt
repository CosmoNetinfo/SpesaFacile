package com.example.spesafacile.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

object UpdateChecker {
    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/CosmoNetinfo/SpesaFacile/releases/latest"
    const val CURRENT_VERSION = "v1.0.0"

    suspend fun checkForUpdate(): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(LATEST_RELEASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "SpesaFacile-App")

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val matcher = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").matcher(responseText)
                if (matcher.find()) {
                    val latestVersion = matcher.group(1)
                    if (latestVersion != null && isNewerVersion(CURRENT_VERSION, latestVersion)) {
                        return@withContext latestVersion
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val cleanCurrent = current.removePrefix("v").split(".")
        val cleanLatest = latest.removePrefix("v").split(".")
        
        val length = maxOf(cleanCurrent.size, cleanLatest.size)
        for (i in 0 until length) {
            val currPart = cleanCurrent.getOrNull(i)?.toIntOrNull() ?: 0
            val latePart = cleanLatest.getOrNull(i)?.toIntOrNull() ?: 0
            if (latePart > currPart) return true
            if (currPart > latePart) return false
        }
        return false
    }
}
