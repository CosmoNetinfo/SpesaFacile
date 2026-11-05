package com.example.spesafacile.ui.main

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

fun saveNotebookPreview(compose: ComposeContentTestRule, name: String) {
    compose.waitForIdle()
    val image = compose.onRoot().captureToImage().asAndroidBitmap()
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val directory = checkNotNull(instrumentation.targetContext.getExternalFilesDir("home-screenshots"))
    directory.mkdirs()
    val file = File(directory, "$name.png")
    file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
    fun shell(command: String): String = ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
        .use { it.bufferedReader().readText() }
    shell("mkdir -p /data/local/tmp/spesafacile-home")
    shell("cp ${file.absolutePath} /data/local/tmp/spesafacile-home/$name.png")
    check(shell("ls /data/local/tmp/spesafacile-home/$name.png").contains("$name.png"))
}
