package com.v2ray.ang.handler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.v2ray.ang.AppConfig
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.dto.GitHubRelease
import com.v2ray.ang.dto.UrlContentRequest
import com.v2ray.ang.util.HttpUtil
import com.v2ray.ang.util.JsonUtil
import com.v2ray.ang.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

/** Looks for a newer release, downloads the APK that fits this phone and hands it to the installer. */
object UpdateManager {

    data class UpdateInfo(
        val version: String,
        val notes: String,
        val downloadUrl: String,
        val fileName: String,
        val sizeBytes: Long
    )

    /** Returns the newer release for this device, or null when the app is current or nothing could be read. */
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        val url = AppConfig.UPDATE_API_URL
        var response = HttpUtil.getUrlContent(UrlContentRequest(url = url, timeout = 8000))
        if (response.isNullOrEmpty()) {
            response = HttpUtil.getUrlContent(
                UrlContentRequest(
                    url = url,
                    timeout = 8000,
                    httpPort = SettingsManager.getHttpPort(),
                    proxyUsername = SettingsManager.getSocksUsername(),
                    proxyPassword = SettingsManager.getSocksPassword()
                )
            )
        }
        if (response.isNullOrEmpty()) return@withContext null

        val release = JsonUtil.fromJsonSafe(response, GitHubRelease::class.java)
            ?: return@withContext null
        if (release.prerelease) return@withContext null

        val latest = release.tagName.trim().removePrefix("v").removePrefix("V")
        LogUtil.i(AppConfig.TAG, "Latest release: $latest (current: ${BuildConfig.VERSION_NAME})")
        if (compareVersions(latest, BuildConfig.VERSION_NAME) <= 0) return@withContext null

        val asset = pickAsset(release) ?: return@withContext null
        UpdateInfo(
            version = latest,
            notes = cleanNotes(release.body),
            downloadUrl = asset.browserDownloadUrl,
            fileName = asset.name,
            sizeBytes = asset.size
        )
    }

    /** Downloads the APK into the app cache; [onProgress] gets values from 0 to 1. */
    suspend fun download(
        context: Context,
        info: UpdateInfo,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "update").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val part = File(dir, info.fileName + ".part")
        val target = File(dir, info.fileName)
        try {
            fetch(info.downloadUrl, part, 0, info.sizeBytes, onProgress)
        } catch (e: IOException) {
            LogUtil.e(AppConfig.TAG, "Direct update download failed, retrying through the local proxy", e)
            part.delete()
            val port = SettingsManager.getHttpPort()
            if (port <= 0) throw e
            fetch(info.downloadUrl, part, port, info.sizeBytes, onProgress)
        }
        if (!part.renameTo(target)) throw IOException("Cannot save the update file")
        target
    }

    /** Opens the system installer. Returns false when the user first has to allow installs from this app. */
    fun install(context: Context, file: File): Boolean {
        if (!context.packageManager.canRequestPackageInstalls()) {
            val settings = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settings)
            return false
        }
        val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.cache", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
        return true
    }

    private suspend fun fetch(
        url: String,
        out: File,
        httpPort: Int,
        expectedSize: Long,
        onProgress: (Float) -> Unit
    ) {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
        if (httpPort > 0) {
            builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", httpPort)))
        }
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Matixray/${BuildConfig.VERSION_NAME}")
            .build()
        builder.build().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val body = response.body ?: throw IOException("Empty response")
            val total = body.contentLength().takeIf { it > 0 } ?: expectedSize
            body.byteStream().use { input ->
                out.outputStream().use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var done = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        done += read
                        if (total > 0) onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
        }
    }

    private fun pickAsset(release: GitHubRelease): GitHubRelease.Asset? {
        val apks = release.assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        val fdroid = BuildConfig.APPLICATION_ID.contains("fdroid", ignoreCase = true)
        val sameFlavor = apks.filter { it.name.contains("fdroid", ignoreCase = true) == fdroid }
            .ifEmpty { apks }
        for (abi in Build.SUPPORTED_ABIS) {
            sameFlavor.firstOrNull { it.name.contains(abi, ignoreCase = true) }?.let { return it }
        }
        return sameFlavor.firstOrNull { it.name.contains("universal", ignoreCase = true) }
            ?: sameFlavor.singleOrNull()
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val numberRegex = Regex("\\d+")
        val v1 = numberRegex.findAll(version1).map { it.value.toInt() }.toList()
        val v2 = numberRegex.findAll(version2).map { it.value.toInt() }.toList()
        for (i in 0 until maxOf(v1.size, v2.size)) {
            val a = v1.getOrElse(i) { 0 }
            val b = v2.getOrElse(i) { 0 }
            if (a != b) return a - b
        }
        return 0
    }

    private fun cleanNotes(body: String?): String {
        if (body.isNullOrBlank()) return ""
        val text = body
            .replace("\r", "")
            .lines()
            .joinToString("\n") { it.trim().trimStart('#', '*', '-', '>', ' ').replace("`", "") }
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
        return if (text.length > 600) text.take(600).trimEnd() + "…" else text
    }
}
