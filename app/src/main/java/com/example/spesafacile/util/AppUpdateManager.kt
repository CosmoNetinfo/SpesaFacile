package com.example.spesafacile.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.spesafacile.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class AppRelease(val version: String, val url: String, val sha256: String, val bytes: Long)
data class UpdateState(val checking: Boolean = false, val downloading: Boolean = false,
    val progress: Float? = null, val release: AppRelease? = null, val readyFile: File? = null, val message: String = "")

class AppUpdateManager private constructor(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val preferences = context.getSharedPreferences("spesafacile_updates", Context.MODE_PRIVATE)
    private val downloads = context.getSystemService(DownloadManager::class.java)
    private val _state = MutableStateFlow(UpdateState())
    val state = _state.asStateFlow()
    private var monitor: Job? = null
    private val _autoDownload = MutableStateFlow(preferences.getBoolean("auto_wifi", false))
    val autoDownload = _autoDownload.asStateFlow()

    init {
        val id = preferences.getLong("download_id", -1)
        val tag = preferences.getString("version", null)
        val hash = preferences.getString("sha256", null)
        if (id > 0 && tag != null && hash != null && VersionComparator.isNewer(BuildConfig.VERSION_NAME, tag)) {
            val release = AppRelease(tag, preferences.getString("url", "").orEmpty(), hash, preferences.getLong("bytes", 0))
            _state.value = UpdateState(downloading = true, release = release)
            watch(id, release)
        } else if (id > 0) { downloads.remove(id); preferences.edit().remove("download_id").apply() }
    }

    fun setAutoDownload(value: Boolean) {
        preferences.edit().putBoolean("auto_wifi", value).apply()
        _autoDownload.value = value
        if (!value && preferences.getBoolean("pending_auto", false) && state.value.downloading) {
            monitor?.cancel()
            downloads.remove(preferences.getLong("download_id", -1))
            preferences.edit().remove("download_id").apply()
            _state.update { it.copy(downloading = false, progress = null, message = "Download automatico disattivato") }
        }
        if (value) { state.value.release?.let { download(it, true) } ?: check() }
    }

    fun check() {
        if (state.value.checking || state.value.downloading) return
        scope.launch {
            _state.update { it.copy(checking = true, message = "Controllo aggiornamenti…") }
            try {
                val json = JSONObject(readUrl("https://api.github.com/repos/CosmoNetinfo/SpesaFacile/releases/latest"))
                val tag = json.getString("tag_name")
                require(Regex("v[0-9]+\\.[0-9]+\\.[0-9]+").matches(tag))
                if (!VersionComparator.isNewer(BuildConfig.VERSION_NAME, tag)) {
                    _state.value = UpdateState(message = "Stai usando l’ultima versione disponibile")
                    return@launch
                }
                val name = "SpesaFacile-$tag.apk"
                val assets = json.getJSONArray("assets")
                val apk = (0 until assets.length()).map { assets.getJSONObject(it) }.first { it.getString("name") == name }
                val url = apk.getString("browser_download_url")
                require(url == "https://github.com/CosmoNetinfo/SpesaFacile/releases/download/$tag/$name")
                val bytes = apk.getLong("size")
                require(bytes in 1..60_000_000)
                var hash = apk.optString("digest").removePrefix("sha256:")
                if (!Regex("[a-fA-F0-9]{64}").matches(hash)) {
                    val checksums = readUrl("https://github.com/CosmoNetinfo/SpesaFacile/releases/download/$tag/SHA256SUMS.txt")
                    hash = checksums.lineSequence().first { it.trim().endsWith(name) }.trim().split(Regex("\\s+"))[0]
                }
                require(Regex("[a-fA-F0-9]{64}").matches(hash))
                val release = AppRelease(tag, url, hash.lowercase(), bytes)
                val ready = state.value.readyFile?.takeIf { state.value.release?.version == tag }
                _state.value = UpdateState(release = release, readyFile = ready, message = "Disponibile $tag")
                if (autoDownload.value && ready == null) download(release, true)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _state.update { it.copy(checking = false, message = "Controllo non riuscito. Verifica la connessione e riprova.") } }
        }
    }

    fun download(requested: AppRelease? = null, wifiOnly: Boolean = false) {
        val release = requested ?: state.value.release ?: return
        synchronized(this) {
            if (state.value.downloading || state.value.readyFile != null) return
            _state.update { it.copy(downloading = true, release = release, message = "Preparazione download…") }
        }
        scope.launch {
            try {
                val filename = "SpesaFacile-${release.version}.apk"
                val target = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)
                if (target.exists()) target.delete()
                val request = DownloadManager.Request(Uri.parse(release.url)).setTitle("SpesaFacile ${release.version}")
                    .setDescription("Aggiornamento dell’app")
                    .setMimeType("application/vnd.android.package-archive")
                    .addRequestHeader("User-Agent", "SpesaFacile/${BuildConfig.VERSION_NAME}")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename)
                if (wifiOnly) request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI).setAllowedOverMetered(false)
                val id = downloads.enqueue(request)
                preferences.edit().putLong("download_id", id).putString("version", release.version).putString("sha256", release.sha256)
                    .putString("url", release.url).putLong("bytes", release.bytes).putBoolean("pending_auto", wifiOnly).apply()
                _state.value = UpdateState(downloading = true, release = release, message = if (wifiOnly) "Download automatico in attesa del Wi-Fi" else "Download avviato")
                watch(id, release)
            } catch (e: Exception) {
                Log.w("SpesaFacileUpdate", "Cannot enqueue update", e)
                _state.update { it.copy(downloading = false, message = "Impossibile avviare il gestore download. Puoi scaricare l’APK dal browser.") }
            }
        }
    }

    private fun watch(id: Long, release: AppRelease) {
        monitor?.cancel()
        monitor = scope.launch {
            try {
                while (isActive) {
                    val cursor = downloads.query(DownloadManager.Query().setFilterById(id))
                    val (status, reason) = cursor.use {
                        check(it.moveToFirst()) { "Il gestore download non trova più il file. Scaricalo nuovamente o usa il browser." }
                        val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val done = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        _state.update { state -> state.copy(progress = if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else null) }
                        it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) to
                            it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    }
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        // Read the file owned by DownloadManager, rather than assuming its on-disk path.
                        val directory = File(context.cacheDir, "updates").apply { mkdirs() }
                        val file = File(directory, "SpesaFacile-${release.version}.apk")
                        downloads.openDownloadedFile(id).use { descriptor ->
                            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
                                file.outputStream().use { output ->
                                    val buffer = ByteArray(8192)
                                    var bytes = 0L
                                    while (true) {
                                        val count = input.read(buffer)
                                        if (count < 0) break
                                        bytes += count
                                        require(bytes <= release.bytes) { "Il file scaricato ha dimensioni inattese. Riprova il download." }
                                        output.write(buffer, 0, count)
                                    }
                                }
                            }
                        }
                        verify(file, release)
                        _state.value = UpdateState(release = release, readyFile = file, message = "Aggiornamento verificato e pronto da installare")
                        return@launch
                    }
                    check(status != DownloadManager.STATUS_FAILED) { downloadFailureMessage(reason) }
                    if (status == DownloadManager.STATUS_PAUSED) {
                        _state.update { it.copy(message = when (reason) {
                            DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "Download in attesa della connessione."
                            DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Download in attesa del Wi-Fi."
                            else -> "Download in pausa: il telefono riproverà. Puoi usare il browser."
                        }) }
                    }
                    delay(1000)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                Log.w("SpesaFacileUpdate", "Update download or verification failed", e)
                downloads.remove(id)
                preferences.edit().remove("download_id").apply()
                _state.value = UpdateState(release = release, message = e.message
                    ?: "Download o verifica non riusciti. Riprova oppure scarica dal browser.")
            }
        }
    }

    @Suppress("DEPRECATION")
    internal fun verify(file: File, release: AppRelease) {
        require(file.length() == release.bytes && file.length() in 1..60_000_000) { "Download incompleto. Scarica nuovamente l’aggiornamento." }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input -> val buffer = ByteArray(8192); while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) } }
        require(digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) } == release.sha256) { "Il file scaricato non corrisponde all’originale. Riprova il download." }
        val flag = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val installed = context.packageManager.getPackageInfo(context.packageName, flag)
        val archive = checkNotNull(context.packageManager.getPackageArchiveInfo(file.absolutePath, flag)) { "Android non riesce a leggere l’APK. Riprova o scaricalo dal browser." }
        require(archive.packageName == context.packageName) { "Questo APK non appartiene a SpesaFacile." }
        val oldCode = if (Build.VERSION.SDK_INT >= 28) installed.longVersionCode else installed.versionCode.toLong()
        val newCode = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else archive.versionCode.toLong()
        require(newCode > oldCode) { "Questa versione è già installata o è precedente a quella presente." }
        val current = if (Build.VERSION.SDK_INT >= 28) installed.signingInfo!!.apkContentsSigners else installed.signatures!!
        val incoming = if (Build.VERSION.SDK_INT >= 28) archive.signingInfo!!.apkContentsSigners else archive.signatures!!
        require(current.map { it.toCharsString() }.toSet() == incoming.map { it.toCharsString() }.toSet()) { "La firma è diversa da quella dell’app installata. Prima di cambiare installazione, esporta un backup delle liste." }
    }

    fun install() {
        val file = state.value.readyFile ?: return
        val release = state.value.release ?: return
        scope.launch {
            try {
                verify(file, release)
                val uri = FileProvider.getUriForFile(context, context.packageName + ".updates", file)
                withContext(Dispatchers.Main) {
                    context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION))
                }
            } catch (_: Exception) { _state.update { it.copy(message = "Impossibile aprire l’installazione. Controlla l’autorizzazione e riprova.") } }
        }
    }

    private fun readUrl(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 10000; connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "SpesaFacile/${BuildConfig.VERSION_NAME}")
            check(connection.responseCode == 200)
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally { connection.disconnect() }
    }
    companion object {
        internal fun downloadFailureMessage(reason: Int): String = when (reason) {
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Spazio insufficiente sul telefono. Libera spazio e riprova."
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "La memoria di destinazione non è disponibile. Prova dal browser."
            DownloadManager.ERROR_HTTP_DATA_ERROR, DownloadManager.ERROR_TOO_MANY_REDIRECTS,
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Il gestore download non riesce a scaricare da GitHub. Prova dal browser."
            DownloadManager.ERROR_FILE_ERROR, DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "Il gestore download non riesce a salvare l’APK. Prova dal browser."
            else -> "Download non riuscito (codice $reason). Riprova oppure scarica dal browser."
        }
        @Volatile private var instance: AppUpdateManager? = null
        fun get(context: Context): AppUpdateManager = instance ?: synchronized(this) {
            instance ?: AppUpdateManager(context.applicationContext).also { instance = it }
        }
    }
}
