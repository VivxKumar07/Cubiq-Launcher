/*
 * Cubiq Launcher
 * Update Available Notification Dialog
 * Prompts user on app launch when GitHub has a new release.
 * Features:
 * - "Read / Download on GitHub" action at top
 * - Direct "Download In-App" pipeline with live progress and automatic APK installer launch
 * - "Not Now" dismiss
 */

package com.movtery.zalithlauncher.ui.screens.content.elements

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.versioninfo.CubiqReleaseItem
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CubiqUpdateDialog(
    release: CubiqReleaseItem,
    onDownloadNow: (String) -> Unit,
    onNotNow: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadStatusText by remember { mutableStateOf("") }
    var downloadError by remember { mutableStateOf<String?>(null) }

    fun startInAppDownload(url: String) {
        if (isDownloading) return
        isDownloading = true
        downloadProgress = 0f
        downloadStatusText = "Connecting..."
        downloadError = null

        scope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "CubiqLauncher-Updater")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw RuntimeException("HTTP ${response.code}: ${response.message}")
                }

                val body = response.body ?: throw RuntimeException("Empty response body")
                val contentLength = body.contentLength()
                val cacheDir = context.externalCacheDir ?: context.cacheDir
                val apkFile = File(cacheDir, "Cubiq-Update-${release.tagName}.apk")
                if (apkFile.exists()) apkFile.delete()

                var totalRead = 0L
                val buffer = ByteArray(8192)

                body.byteStream().use { input ->
                    FileOutputStream(apkFile).use { output ->
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            totalRead += read

                            if (contentLength > 0) {
                                val frac = totalRead.toFloat() / contentLength.toFloat()
                                val mbRead = totalRead / (1024f * 1024f)
                                val mbTotal = contentLength / (1024f * 1024f)
                                withContext(Dispatchers.Main) {
                                    downloadProgress = frac
                                    downloadStatusText = "Downloading: ${(frac * 100).toInt()}% (%.1f MB / %.1f MB)".format(mbRead, mbTotal)
                                }
                            } else {
                                val mbRead = totalRead / (1024f * 1024f)
                                withContext(Dispatchers.Main) {
                                    downloadStatusText = "Downloading: %.1f MB".format(mbRead)
                                }
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    downloadStatusText = "Download complete! Opening installer..."
                    isDownloading = false
                    installApk(context, apkFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    downloadError = e.localizedMessage ?: "Download failed"
                    Toast.makeText(context, "In-app download failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    BasicAlertDialog(
        onDismissRequest = {
            if (!isDownloading) onNotNow()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1E20))
                .border(BorderStroke(2.dp, Color(0xFF3C8527)), RoundedCornerShape(4.dp))
                .padding(18.dp)
        ) {
            // Top Bar: Read / Download on GitHub link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF28282B))
                        .border(BorderStroke(1.dp, Color(0xFF444446)), RoundedCornerShape(3.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_public),
                        contentDescription = "GitHub",
                        tint = Color(0xFF55FFFF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Read / Download on GitHub",
                        color = Color(0xFF55FFFF),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 10.5.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1E3A5F))
                        .border(BorderStroke(1.dp, Color(0xFF3B82F6)), RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = release.tagName.uppercase(),
                        color = Color(0xFF93C5FD),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // Header: Update Available
            Column {
                Text(
                    text = "NEW VERSION AVAILABLE",
                    color = Color(0xFF55FF55),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = release.title,
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Changelog container with slider
            Text(
                text = "CHANGELOG & RELEASE NOTES:",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF151517))
                    .border(BorderStroke(1.dp, Color(0xFF2E2E30)), RoundedCornerShape(3.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScrollWithBar(rememberScrollState())
                ) {
                    Text(
                        text = release.body.ifBlank { "New version released with performance optimizations and bug fixes." },
                        color = Color(0xFFE2E2E2),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // In-App Download Progress Area
            AnimatedVisibility(visible = isDownloading || downloadStatusText.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Text(
                        text = downloadStatusText,
                        color = if (downloadError != null) Color(0xFFFF5555) else Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF3C8527),
                        trackColor = Color(0xFF2A2A2D)
                    )
                }
            }

            // Buttons: [ DOWNLOAD IN-APP ] [ BROWSER ] [ NOT NOW ]
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Option: Download In-App
                MinecraftButton(
                    onClick = {
                        val targetUrl = release.downloadUrl ?: release.htmlUrl
                        if (targetUrl.endsWith(".apk", ignoreCase = true)) {
                            startInAppDownload(targetUrl)
                        } else {
                            // If direct APK url not detected, fall back to browser or download url
                            onDownloadNow(targetUrl)
                        }
                    },
                    style = MinecraftButtonStyle.GREEN,
                    text = if (isDownloading) "DOWNLOADING..." else "⬇ DOWNLOAD UPDATE IN-APP",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftButton(
                        onClick = {
                            onDownloadNow(release.htmlUrl)
                        },
                        style = MinecraftButtonStyle.STONE,
                        text = "READ / DOWNLOAD ON GITHUB",
                        fontSize = 10.5.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    )

                    MinecraftButton(
                        onClick = onNotNow,
                        style = MinecraftButtonStyle.STONE,
                        text = "NOT NOW",
                        fontSize = 10.5.sp,
                        modifier = Modifier
                            .width(96.dp)
                            .height(34.dp)
                    )
                }
            }
        }
    }
}

private fun installApk(context: Context, apkFile: File) {
    runCatching {
        val authority = "${context.packageName}.provider"
        val contentUri = FileProvider.getUriForFile(context, authority, apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }.onFailure { e ->
        Toast.makeText(context, "Could not launch package installer: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
