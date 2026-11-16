package com.movtery.zalithlauncher.ui.subassembly.view

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.view.View
import android.widget.TextView
import com.getkeepsafe.taptargetview.TapTargetView
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.NewbieGuideUtils
import com.movtery.zalithlauncher.utils.platform.getTotalMemory
import com.movtery.zalithlauncher.utils.platform.getUsedMemory
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.assist.helper.FxScopeHelper
import com.petterp.floatingx.listener.IFxViewLifecycle
import com.petterp.floatingx.listener.control.IFxScopeControl
import com.petterp.floatingx.view.FxViewHolder
import org.lwjgl.glfw.CallbackBridge
import java.util.Timer
import java.util.TimerTask

class GameMenuViewWrapper(
    private val activity: Activity,
    private val listener: View.OnClickListener,
    private val showInfo: Boolean
) {
    companion object {
        private const val TAG = "GameMenuViewWrapper"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private val memoryText: String = AllSettings.gameMenuMemoryText.getValue()
    private var showMemory: Boolean = false
    private var showFPS: Boolean = false
    private var visible: Boolean = false

    private var scopeFx: IFxScopeControl? = null

    init {
        refreshState()
    }

    private fun getWindow(): IFxScopeControl {
        return FxScopeHelper.Builder().apply {
            setLayout(R.layout.view_game_menu_window)
            setOnClickListener(0L, listener)
            setEnableEdgeAdsorption(false)
            val storage = object : com.petterp.floatingx.listener.IFxConfigStorage {
                override fun getX(): Float {
                    val raw = AllSettings.zl1GameMenuPos.getValue()
                    return raw.substringBefore(",", "-1").toFloatOrNull() ?: -1f
                }

                override fun getY(): Float {
                    val raw = AllSettings.zl1GameMenuPos.getValue()
                    return raw.substringAfter(",", "-1").toFloatOrNull() ?: -1f
                }

                override fun update(x: Float, y: Float) {
                    if (x >= 0f && y >= 0f) {
                        AllSettings.zl1GameMenuPos.save("$x,$y")
                    }
                }

                override fun hasConfig(): Boolean {
                    return getX() >= 0f && getY() >= 0f
                }

                override fun clear() {
                    AllSettings.zl1GameMenuPos.save("")
                }
            }
            setSaveDirectionImpl(storage)
            if (!storage.hasConfig()) {
                setGravity(getCurrentGravity())
            }
            addViewLifecycle(object : IFxViewLifecycle {
                override fun initView(holder: FxViewHolder) {
                    holder.view.alpha = AllSettings.gameMenuAlpha.getValue().toFloat() / 100f
                    val savedX = storage.getX()
                    val savedY = storage.getY()
                    if (savedX >= 0f && savedY >= 0f) {
                        holder.view.post {
                            holder.view.x = savedX
                            holder.view.y = savedY
                        }
                    }
                    holder.view.setOnTouchListener { v, event ->
                        if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                            if (v.x >= 0f && v.y >= 0f) {
                                storage.update(v.x, v.y)
                            }
                        }
                        false // Pass to FloatingX for click & drag processing
                    }
                    updateInfoText(holder.view)
                }

                override fun detached(view: View) {
                    cancelInfoTimer()
                }
            })
        }.build().toControl(activity)
    }

    private fun startNewbieGuide(mainView: View) {
        if (NewbieGuideUtils.showOnlyOne(TAG)) return
        TapTargetView.showFor(
            activity,
            NewbieGuideUtils.getSimpleTarget(
                activity,
                mainView,
                activity.getString(R.string.setting_category_game_menu),
                activity.getString(R.string.newbie_guide_game_menu)
            )
        )
    }

    fun setVisibility(visible: Boolean) {
        this.visible = visible
        thinkForVisibility()
    }

    fun refreshSettingsState() {
        refreshState()
        thinkForVisibility()
    }

    private fun thinkForVisibility() {
        val v1 = visible || showMemory || showFPS
        if (v1) {
            val current = scopeFx
            if (current != null) {
                current.getView()?.let { view ->
                    view.findViewById<TextView>(R.id.memory_text)?.visibility = if (showMemory && showInfo) View.VISIBLE else View.GONE
                    view.findViewById<TextView>(R.id.fps_text)?.visibility = if (showFPS && showInfo) View.VISIBLE else View.GONE
                    updateInfoText(view)
                }
            } else {
                scopeFx = getWindow().apply {
                    updateInfoText()
                    show()
                    getView()?.let { startNewbieGuide(it) }
                }
            }
        } else {
            scopeFx?.cancel()
            scopeFx = null
            cancelInfoTimer()
        }
    }

    private fun refreshState() {
        showMemory = AllSettings.gameMenuShowMemory.getValue()
        showFPS = AllSettings.gameMenuShowFPS.getValue()
    }

    private fun updateInfoText() {
        scopeFx?.getView()?.let { updateInfoText(it) }
    }

    private fun updateInfoText(view: View) {
        cancelInfoTimer()

        val memoryTextView: TextView = view.findViewById(R.id.memory_text)
        val fpsTextView: TextView = view.findViewById(R.id.fps_text)

        fun updateText() {
            if (showMemory) {
                val raw = "${this@GameMenuViewWrapper.memoryText} ${getUsedDeviceMemory()}/${getTotalDeviceMemory()}"
                val text = if (raw.length > 40) raw.take(40) else raw
                mainHandler.post { memoryTextView.text = text }
            }
            if (showFPS) {
                val fpsString = "FPS: ${CallbackBridge.getCurrentFps()}"
                mainHandler.post { fpsTextView.text = fpsString }
            }
        }

        updateText()

        if (showInfo) {
            memoryTextView.visibility = if (showMemory) View.VISIBLE else View.GONE
            fpsTextView.visibility = if (showFPS) View.VISIBLE else View.GONE

            if (showMemory || showFPS) {
                timer = Timer().apply {
                    schedule(object : TimerTask() {
                        override fun run() {
                            updateText()
                        }
                    }, 0, AllSettings.gameMenuInfoRefreshRate.getValue().toLong().coerceAtLeast(100L))
                }
            }
        }
    }

    private fun getUsedDeviceMemory(): String = Formatter.formatShortFileSize(activity, getUsedMemory(activity))

    private fun getTotalDeviceMemory(): String = Formatter.formatShortFileSize(activity, getTotalMemory(activity))

    private fun cancelInfoTimer() {
        timer?.cancel()
        timer = null
    }

    fun release() {
        cancelInfoTimer()
        scopeFx?.cancel()
        scopeFx = null
    }

    private fun getCurrentGravity(): FxGravity {
        return when (AllSettings.gameMenuLocation.getValue()) {
            "left_or_top" -> FxGravity.LEFT_OR_TOP
            "left_or_bottom" -> FxGravity.LEFT_OR_BOTTOM
            "right_or_top" -> FxGravity.RIGHT_OR_TOP
            "right_or_bottom" -> FxGravity.RIGHT_OR_BOTTOM
            "top_or_center" -> FxGravity.TOP_OR_CENTER
            "bottom_or_center" -> FxGravity.BOTTOM_OR_CENTER
            "center" -> FxGravity.CENTER
            else -> FxGravity.CENTER
        }
    }
}

