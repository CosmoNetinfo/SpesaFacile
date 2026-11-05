package com.example.spesafacile.util

import android.app.DownloadManager
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateFailureTest {
    @Test fun distinguishesStorageAndNetworkFailures() {
        assertTrue(AppUpdateManager.downloadFailureMessage(DownloadManager.ERROR_INSUFFICIENT_SPACE).contains("Spazio insufficiente"))
        assertTrue(AppUpdateManager.downloadFailureMessage(DownloadManager.ERROR_TOO_MANY_REDIRECTS).contains("browser"))
        assertTrue(AppUpdateManager.downloadFailureMessage(403).contains("403"))
    }
}
