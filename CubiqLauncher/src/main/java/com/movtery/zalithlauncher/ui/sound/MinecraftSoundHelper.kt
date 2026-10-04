package com.movtery.zalithlauncher.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.movtery.zalithlauncher.R
import java.util.concurrent.atomic.AtomicBoolean

object MinecraftSoundHelper {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private val isLoaded = AtomicBoolean(false)

    fun init(context: Context) {
        if (soundPool != null) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == clickSoundId) {
                    isLoaded.set(true)
                }
            }

            clickSoundId = pool.load(context.applicationContext, R.raw.minecraft_click, 1)
            soundPool = pool
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClickSound() {
        try {
            val pool = soundPool ?: return
            pool.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
