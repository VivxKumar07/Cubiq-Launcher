package com.movtery.zalithlauncher.utils.capture

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CubiqScreenRecorder {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    var isRecording = false
        private set

    fun takeScreenshot(activity: Activity, onComplete: (File?) -> Unit) {
        val window = activity.window ?: return onComplete(null)
        val decorView = window.decorView
        val width = decorView.width.coerceAtLeast(1)
        val height = decorView.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val location = IntArray(2)
            decorView.getLocationInWindow(location)
            PixelCopy.request(
                window,
                Rect(location[0], location[1], location[0] + width, location[1] + height),
                bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        val file = saveBitmap(activity, bitmap)
                        onComplete(file)
                    } else {
                        onComplete(null)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } else {
            @Suppress("DEPRECATION")
            decorView.isDrawingCacheEnabled = true
            @Suppress("DEPRECATION")
            val cache = decorView.drawingCache
            val file = if (cache != null) saveBitmap(activity, cache) else null
            @Suppress("DEPRECATION")
            decorView.isDrawingCacheEnabled = false
            onComplete(file)
        }
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Screenshot_$timeStamp.png"

            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Cubiq")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Cubiq")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
            }

            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun startRecording(context: Context, resultCode: Int, data: Intent, resWidth: Int = 1280, resHeight: Int = 720, fps: Int = 30, bitrate: Int = 6): Boolean {
        if (isRecording) return true
        return try {
            val serviceIntent = Intent(context, CubiqScreenRecordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                ?: return false
            val projection = projectionManager.getMediaProjection(resultCode, data) ?: run {
                runCatching { context.stopService(serviceIntent) }
                return false
            }
            mediaProjection = projection

            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    stopRecording(context)
                }
            }, Handler(Looper.getMainLooper()))

            val metrics = context.resources.displayMetrics
            // Enforce even dimensions required by H.264
            val width = ((if (resWidth > 0) resWidth else metrics.widthPixels) / 2) * 2
            val height = ((if (resHeight > 0) resHeight else metrics.heightPixels) / 2) * 2
            val dpi = metrics.densityDpi

            val tempDir = File(context.cacheDir, "recordings").apply { if (!exists()) mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val tempFile = File(tempDir, "Recording_$timeStamp.mp4")
            currentOutputFile = tempFile

            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }
            mediaRecorder = recorder

            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            recorder.setVideoSize(width, height)
            recorder.setVideoFrameRate(fps.coerceIn(15, 60))
            recorder.setVideoEncodingBitRate((bitrate * 1_000_000).coerceIn(1_000_000, 20_000_000))
            recorder.setOutputFile(tempFile.absolutePath)
            recorder.prepare()

            val surface = recorder.surface
            virtualDisplay = projection.createVirtualDisplay(
                "CubiqRecorder",
                width,
                height,
                dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null
            )

            recorder.start()
            isRecording = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording(context)
            false
        }
    }

    private fun exportVideo(context: Context, sourceFile: File): File? {
        val fileName = sourceFile.name
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Cubiq")
        if (!dir.exists()) dir.mkdirs()
        val destFile = File(dir, fileName)

        try {
            sourceFile.inputStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (_: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Cubiq")
                }
                context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                    sourceFile.inputStream().use { input ->
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }

        MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), arrayOf("video/mp4"), null)
        return if (destFile.exists() && destFile.length() > 0) destFile else sourceFile
    }

    fun stopRecording(context: Context): File? {
        val file = currentOutputFile
        try {
            val serviceIntent = Intent(context, CubiqScreenRecordService::class.java)
            runCatching { context.stopService(serviceIntent) }

            if (isRecording) {
                runCatching { mediaRecorder?.stop() }
                runCatching { mediaRecorder?.reset() }
                runCatching { mediaRecorder?.release() }
                runCatching { virtualDisplay?.release() }
                runCatching { mediaProjection?.stop() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            virtualDisplay = null
            mediaProjection = null
            isRecording = false
            currentOutputFile = null
        }
        if (file != null && file.exists() && file.length() > 0) {
            val exported = exportVideo(context, file)
            runCatching { file.delete() }
            return exported
        }
        return null
    }
}
