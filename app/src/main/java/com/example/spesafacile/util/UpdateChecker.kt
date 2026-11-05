package com.example.spesafacile.util

import com.example.spesafacile.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    suspend fun checkForUpdate(): String? = withContext(Dispatchers.IO) {
        val connection = URL("https://api.github.com/repos/CosmoNetinfo/SpesaFacile/releases/latest")
            .openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "SpesaFacile/${BuildConfig.VERSION_NAME}")
            if (connection.responseCode != 200) return@withContext null
            val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val tag = release.optString("tag_name")
            tag.takeIf { !release.optBoolean("prerelease") && !release.optBoolean("draft") &&
                VersionComparator.isNewer(BuildConfig.VERSION_NAME, it) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
