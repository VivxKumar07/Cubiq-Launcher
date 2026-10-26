package com.movtery.zalithlauncher.context

import android.widget.Toast
import android.os.Handler
import android.os.Looper

object ContextExecutor {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JvmStatic
    fun getString(resId: Int): String {
        return runCatching { GlobalContext.getString(resId) }.getOrElse { "" }
    }

    @JvmStatic
    fun showToast(resId: Int, duration: Int) {
        mainHandler.post {
            runCatching {
                Toast.makeText(GlobalContext, GlobalContext.getString(resId), duration).show()
            }
        }
    }

    @JvmStatic
    fun showToast(text: String, duration: Int) {
        mainHandler.post {
            runCatching {
                Toast.makeText(GlobalContext, text, duration).show()
            }
        }
    }
}

