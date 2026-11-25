package com.movtery.zalithlauncher.utils

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.activities.SplashActivity

object ShortcutHelper {
    fun createGameShortcut(context: Context, versionName: String, isRust: Boolean = false) {
        try {
            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                Toast.makeText(context, R.string.cubiq_progress_shortcut_unsupported, Toast.LENGTH_SHORT).show()
                return
            }

            val launchIntent = Intent(context, SplashActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                if (isRust) {
                    putExtra("EXTRA_DIRECT_LAUNCH_RUST", true)
                } else {
                    putExtra("EXTRA_DIRECT_LAUNCH_VERSION", versionName)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.minecraft_launcher_logo)
            val icon = if (bitmap != null) {
                IconCompat.createWithBitmap(bitmap)
            } else {
                IconCompat.createWithResource(context, R.mipmap.ic_launcher)
            }

            val label = if (isRust) "Minecraft Rust" else versionName
            val shortcutId = "cubiq_shortcut_${if (isRust) "rust" else versionName.replace(" ", "_")}"

            val shortcut = ShortcutInfoCompat.Builder(context, shortcutId)
                .setShortLabel(label)
                .setLongLabel(context.getString(R.string.cubiq_progress_shortcut_play, label))
                .setIcon(icon)
                .setIntent(launchIntent)
                .build()

            ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
            Toast.makeText(context, context.getString(R.string.cubiq_progress_shortcut_requested, label), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.cubiq_progress_shortcut_failed, e.message), Toast.LENGTH_SHORT).show()
        }
    }
}

