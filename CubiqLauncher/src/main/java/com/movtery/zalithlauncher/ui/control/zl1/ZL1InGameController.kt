package com.movtery.zalithlauncher.ui.control.zl1

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.ZLNativeInvoker
import com.movtery.zalithlauncher.databinding.ViewGameMenuBinding
import com.movtery.zalithlauncher.databinding.ViewZl1ControlMenuBinding
import com.movtery.zalithlauncher.databinding.ViewZl1GameRootBinding
import com.movtery.zalithlauncher.event.single.RefreshHotbarEvent
import com.movtery.zalithlauncher.event.value.HotbarChangeEvent
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.game.input.EfficientAndroidLWJGLKeycode
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.AllStaticSettings
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.ui.dialog.KeyboardDialog
import com.movtery.zalithlauncher.ui.dialog.SelectControlsDialog
import com.movtery.zalithlauncher.ui.dialog.SelectMouseDialog
import com.movtery.zalithlauncher.ui.dialog.TipDialog
import com.movtery.zalithlauncher.ui.subassembly.adapter.ObjectSpinnerAdapter
import com.movtery.zalithlauncher.ui.subassembly.hotbar.HotbarType
import com.movtery.zalithlauncher.ui.subassembly.hotbar.HotbarUtils
import com.movtery.zalithlauncher.ui.subassembly.menu.ControlMenu
import com.movtery.zalithlauncher.ui.subassembly.menu.MenuUtils
import com.movtery.zalithlauncher.ui.subassembly.view.GameMenuViewWrapper
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.GrabListener
import net.kdt.pojavlaunch.LwjglGlfwKeycode
import net.kdt.pojavlaunch.MinecraftGLSurface
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.customcontrols.ControlButtonMenuListener
import net.kdt.pojavlaunch.customcontrols.CustomControls
import net.kdt.pojavlaunch.customcontrols.EditorExitable
import net.kdt.pojavlaunch.customcontrols.keyboard.LwjglCharSender
import net.kdt.pojavlaunch.customcontrols.mouse.GyroControl
import org.greenrobot.eventbus.EventBus
import org.lwjgl.glfw.CallbackBridge
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Self-contained host for the authentic Zalith Launcher 1 (ZL1) in-game control system:
 * ControlLayout, MinecraftGLSurface input processors, Touchpad virtual cursor, HotbarView,
 * TouchCharInput, FloatingX gear menu, view_game_menu drawer + nested dialogs, live ControlMenu
 * editor, GyroControl, and physical Gamepad handler.
 */
class ZL1InGameController(
    private val activity: Activity,
    private val lifecycleScope: CoroutineScope,
    private val useTouchControllerProxy: Boolean,
    useSurfaceView: Boolean,
    surfaceCallback: SurfaceHolder.Callback,
    textureListener: TextureView.SurfaceTextureListener,
    insetsListener: View.OnApplyWindowInsetsListener,
    renderViewCreatedListener: MinecraftGLSurface.RenderViewCreatedListener,
    private val onToggleLog: () -> Unit
) : ControlButtonMenuListener, EditorExitable {

    companion object {
        @Volatile
        private var activeController: WeakReference<ZL1InGameController>? = null

        @JvmStatic
        fun toggleKeyboard(): Boolean {
            val controller = activeController?.get() ?: return false
            controller.switchKeyboardState()
            return true
        }

        @JvmStatic
        fun toggleMouse(@Suppress("UNUSED_PARAMETER") context: android.content.Context): Boolean {
            val controller = activeController?.get() ?: return false
            controller.toggleMouse()
            return true
        }
    }

    val rootBinding: ViewZl1GameRootBinding = ViewZl1GameRootBinding.inflate(activity.layoutInflater)
    private val gameMenuBinding: ViewGameMenuBinding = ViewGameMenuBinding.inflate(activity.layoutInflater)
    private val controlMenuBinding: ViewZl1ControlMenuBinding = ViewZl1ControlMenuBinding.inflate(activity.layoutInflater)

    private val gyroControl: GyroControl
    private val gameMenuWrapper: GameMenuViewWrapper
    private val menuSettingsInitListener: MenuSettingsInitListener
    private val keyboardDialog: KeyboardDialog = KeyboardDialog(activity).setShowSpecialButtons(false)

    var isInEditor: Boolean = false
        private set

    private var lastDrawerHeight: Int = 0

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val visibleBounds = android.graphics.Rect()
        val rootView = activity.window?.decorView ?: rootBinding.root
        rootView.getWindowVisibleDisplayFrame(visibleBounds)
        val screenHeight = rootView.height
        val keypadHeight = screenHeight - visibleBounds.bottom
        val touchInput = rootBinding.mainTouchCharInput
        val previewLayout = rootBinding.inputPreviewLayout

        if (touchInput.hasFocus()) {
            if (keypadHeight > screenHeight * 0.15f) {
                if (previewLayout.visibility != View.VISIBLE) {
                    previewLayout.visibility = View.VISIBLE
                }
                previewLayout.y = (visibleBounds.bottom - previewLayout.height).coerceAtLeast(0).toFloat()
            } else if (previewLayout.visibility == View.VISIBLE) {
                previewLayout.visibility = View.GONE
            }
        } else if (previewLayout.visibility == View.VISIBLE) {
            previewLayout.visibility = View.GONE
        }

        val currentHeight = rootBinding.mainDrawerOptions.height
        if (currentHeight > 0 && currentHeight != lastDrawerHeight) {
            lastDrawerHeight = currentHeight
            Tools.updateWindowSize(activity)
            EventBus.getDefault().post(RefreshHotbarEvent())
        }
    }

    private val grabListener = GrabListener { isGrabbing ->
        rootBinding.mainTouchpad.post {
            if (isGrabbing && rootBinding.mainTouchpad.displayState) {
                rootBinding.mainTouchpad.disable()
            } else if (!isGrabbing && AllSettings.virtualMouseStart.getValue() && !rootBinding.mainTouchpad.displayState) {
                rootBinding.mainTouchpad.enable(true)
            }
        }
    }

    init {
        activeController = WeakReference(this)

        Tools.updateWindowSize(activity)
        AllStaticSettings.refresh(useTouchControllerProxy)

        gyroControl = GyroControl(activity)

        rootBinding.mainNavigationView.addView(gameMenuBinding.root)
        menuSettingsInitListener = MenuSettingsInitListener(gameMenuBinding)
        rootBinding.mainDrawerOptions.addDrawerListener(menuSettingsInitListener)
        rootBinding.mainDrawerOptions.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        ControlMenu(activity, this, controlMenuBinding, rootBinding.mainControlLayout, true)

        gameMenuWrapper = GameMenuViewWrapper(
            activity,
            { onClickedMenu() },
            true
        )

        rootBinding.mainControlLayout.setMenuListener(this)

        rootBinding.mainTouchCharInput.setCharacterSender(LwjglCharSender())
        rootBinding.mainTouchCharInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val raw = s?.toString().orEmpty()
                val filler = net.kdt.pojavlaunch.customcontrols.keyboard.TouchCharInput.TEXT_FILLER
                val stripped = if (raw.startsWith(filler)) raw.substring(filler.length) else raw.trimStart()
                rootBinding.inputPreview.text = stripped
            }
        })

        rootBinding.mainGameRenderView.setTouchCharInput(rootBinding.mainTouchCharInput)
        rootBinding.mainGameRenderView.setOnRenderingStartedListener {
            rootBinding.backgroundView.post {
                rootBinding.backgroundView.visibility = View.GONE
            }
        }
        rootBinding.mainGameRenderView.start(
            useSurfaceView,
            rootBinding.mainTouchpad,
            surfaceCallback,
            textureListener,
            insetsListener,
            renderViewCreatedListener
        )

        loadConfiguredControlLayout()

        CallbackBridge.addGrabListener(rootBinding.mainTouchpad)
        CallbackBridge.addGrabListener(grabListener)
        rootBinding.root.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        if (AllSettings.gyroscopeControl.getValue()) {
            gyroControl.enable()
        }
    }

    private fun loadConfiguredControlLayout() {
        try {
            val selectedFile = ControlManager.getSelectedLayoutFile()
            if (selectedFile != null && selectedFile.exists()) {
                rootBinding.mainControlLayout.loadLayout(selectedFile.absolutePath)
            } else {
                rootBinding.mainControlLayout.loadLayout(null as String?)
            }
        } catch (e: IOException) {
            Tools.showError(activity, e)
        }
        gameMenuWrapper.setVisibility(!rootBinding.mainControlLayout.hasMenuButton())
    }

    fun switchKeyboardState() {
        rootBinding.mainTouchCharInput.switchKeyboardState()
    }

    fun toggleMouse() {
        rootBinding.mainTouchpad.switchState()
    }

    override fun onClickedMenu() {
        val drawerLayout = rootBinding.mainDrawerOptions
        val navigationView = rootBinding.mainNavigationView
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView)
        } else {
            drawerLayout.openDrawer(navigationView)
        }
        navigationView.requestLayout()
    }

    override fun exitEditor() {
        try {
            rootBinding.mainControlLayout.loadLayout(null as CustomControls?)
            rootBinding.mainControlLayout.setModifiable(false)
            System.gc()
            loadConfiguredControlLayout()
        } catch (e: Exception) {
            Tools.showError(activity, e)
        }
        rootBinding.mainNavigationView.removeAllViews()
        rootBinding.mainNavigationView.addView(gameMenuBinding.root)
        isInEditor = false
    }

    fun handleBackPressed(): Boolean {
        if (isInEditor) {
            rootBinding.mainControlLayout.askToExit(this)
            return true
        }
        if (rootBinding.mainDrawerOptions.isDrawerOpen(GravityCompat.END) ||
            rootBinding.mainDrawerOptions.isDrawerOpen(rootBinding.mainNavigationView)
        ) {
            rootBinding.mainDrawerOptions.closeDrawer(rootBinding.mainNavigationView)
            return true
        }
        if (rootBinding.mainTouchCharInput.hasFocus()) {
            rootBinding.mainTouchCharInput.disable()
            return true
        }
        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ESCAPE.toInt())
        return true
    }

    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return rootBinding.mainGameRenderView.processKeyEvent(event)
    }

    fun dispatchTrackballEvent(ev: MotionEvent): Boolean {
        val eventSource = ev.source
        return if ((eventSource and InputDevice.SOURCE_MOUSE_RELATIVE) != 0 ||
            (eventSource and InputDevice.SOURCE_MOUSE) != 0
        ) {
            rootBinding.mainGameRenderView.dispatchCapturedPointerEvent(ev)
        } else {
            false
        }
    }

    fun onResume() {
        AllStaticSettings.refresh(useTouchControllerProxy)
        Tools.updateWindowSize(activity)
        gameMenuWrapper.refreshSettingsState()
        gyroControl.updateOrientation()
        if (AllSettings.gyroscopeControl.getValue()) {
            gyroControl.enable()
        } else {
            gyroControl.disable()
        }
        EventBus.getDefault().post(RefreshHotbarEvent())
    }

    fun onPause() {
        gyroControl.disable()
    }

    fun onConfigurationChanged(@Suppress("UNUSED_PARAMETER") newConfig: Configuration) {
        Tools.updateWindowSize(activity)
        gyroControl.updateOrientation()
        rootBinding.mainControlLayout.refreshControlButtonPositions()
        EventBus.getDefault().post(RefreshHotbarEvent())
    }

    fun onDestroy() {
        if (activeController?.get() === this) {
            activeController = null
        }
        gyroControl.disable()
        gameMenuWrapper.release()
        rootBinding.mainGameRenderView.destroy()
        CallbackBridge.removeGrabListener(rootBinding.mainTouchpad)
        CallbackBridge.removeGrabListener(grabListener)
        if (rootBinding.root.viewTreeObserver.isAlive) {
            rootBinding.root.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
        if (keyboardDialog.isShowing) {
            keyboardDialog.dismiss()
        }
    }

    private fun getResolutionRatioPreview(progress: Int): String {
        val dm = Tools.currentDisplayMetrics
        val baseW = dm?.let { kotlin.math.max(it.widthPixels, it.heightPixels) } ?: 1920
        val baseH = dm?.let { kotlin.math.min(it.widthPixels, it.heightPixels) } ?: 1080
        val factor = progress / 100f
        val w = Tools.getDisplayFriendlyRes(baseW, factor)
        val h = Tools.getDisplayFriendlyRes(baseH, factor)
        return "$w × $h"
    }

    private inner class MenuSettingsInitListener(
        private val binding: ViewGameMenuBinding
    ) : View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        CompoundButton.OnCheckedChangeListener,
        OnSpinnerItemSelectedListener<HotbarType>,
        DrawerLayout.DrawerListener {

        init {
            val dm = Tools.currentDisplayMetrics
            val maxHotbarW = (dm?.let { kotlin.math.max(it.widthPixels, it.heightPixels) } ?: 1920) / 2
            val maxHotbarH = (dm?.let { kotlin.math.min(it.widthPixels, it.heightPixels) } ?: 1080) / 2
            binding.hotbarWidth.max = maxHotbarW.coerceAtLeast(100)
            binding.hotbarHeight.max = maxHotbarH.coerceAtLeast(100)

            MenuUtils.initSeekBarValue(
                binding.resolutionScaler,
                AllSettings.resolutionRatio.getValue(),
                binding.resolutionScalerValue,
                "%"
            )
            binding.resolutionScalerPreview.text = getResolutionRatioPreview(AllSettings.resolutionRatio.getValue())

            MenuUtils.initSeekBarValue(
                binding.timeLongPressTrigger,
                AllSettings.timeLongPressTrigger.getValue(),
                binding.timeLongPressTriggerValue,
                "ms"
            )
            MenuUtils.initSeekBarValue(
                binding.mouseSpeed,
                AllSettings.mouseSpeed.getValue(),
                binding.mouseSpeedValue,
                "%"
            )
            MenuUtils.initSeekBarValue(
                binding.gyroSensitivity,
                AllSettings.gyroscopeSensitivity.getValue(),
                binding.gyroSensitivityValue,
                "%"
            )
            MenuUtils.initSeekBarValue(
                binding.hotbarHeight,
                AllSettings.zl1HotbarHeight.getValue().coerceIn(0, binding.hotbarHeight.max),
                binding.hotbarHeightValue,
                "px"
            )
            MenuUtils.initSeekBarValue(
                binding.hotbarWidth,
                AllSettings.zl1HotbarWidth.getValue().coerceIn(0, binding.hotbarWidth.max),
                binding.hotbarWidthValue,
                "px"
            )

            binding.openMemoryInfo.isChecked = AllSettings.gameMenuShowMemory.getValue()
            binding.openFpsInfo.isChecked = AllSettings.gameMenuShowFPS.getValue()
            binding.disableGestures.isChecked = AllSettings.disableGestures.getValue()
            binding.disableDoubleTap.isChecked = AllSettings.disableDoubleTap.getValue()
            binding.enableGyro.isChecked = AllSettings.gyroscopeControl.getValue()
            binding.gyroInvertX.isChecked = AllSettings.gyroscopeInvertX.getValue()
            binding.gyroInvertY.isChecked = AllSettings.gyroscopeInvertY.getValue()

            refreshLayoutVisible(binding.timeLongPressTriggerLayout, !AllSettings.disableGestures.getValue())
            refreshLayoutVisible(binding.gyroLayout, AllSettings.gyroscopeControl.getValue())

            binding.forceClose.setOnClickListener(this)
            binding.logOutput.setOnClickListener(this)
            binding.sendCustomKey.setOnClickListener(this)
            binding.openMemoryInfo.setOnCheckedChangeListener(this)
            binding.openMemoryInfoLayout.setOnClickListener(this)
            binding.openFpsInfo.setOnCheckedChangeListener(this)
            binding.openFpsInfoLayout.setOnClickListener(this)

            binding.resolutionScaler.setOnSeekBarChangeListener(this)
            binding.resolutionScalerRemove.setOnClickListener(this)
            binding.resolutionScalerAdd.setOnClickListener(this)

            binding.disableGestures.setOnCheckedChangeListener(this)
            binding.disableGesturesLayout.setOnClickListener(this)

            binding.disableDoubleTap.setOnCheckedChangeListener(this)
            binding.disableDoubleTapLayout.setOnClickListener(this)

            binding.timeLongPressTrigger.setOnSeekBarChangeListener(this)
            binding.timeLongPressTriggerRemove.setOnClickListener(this)
            binding.timeLongPressTriggerAdd.setOnClickListener(this)

            binding.mouseSpeed.setOnSeekBarChangeListener(this)
            binding.mouseSpeedRemove.setOnClickListener(this)
            binding.mouseSpeedAdd.setOnClickListener(this)

            binding.customMouse.setOnClickListener(this)
            binding.replacementCustomcontrol.setOnClickListener(this)
            binding.editControl.setOnClickListener(this)

            binding.enableGyro.setOnCheckedChangeListener(this)
            binding.enableGyroLayout.setOnClickListener(this)

            binding.gyroSensitivity.setOnSeekBarChangeListener(this)
            binding.gyroSensitivityRemove.setOnClickListener(this)
            binding.gyroSensitivityAdd.setOnClickListener(this)

            binding.gyroInvertX.setOnCheckedChangeListener(this)
            binding.gyroInvertXLayout.setOnClickListener(this)

            binding.gyroInvertY.setOnCheckedChangeListener(this)
            binding.gyroInvertYLayout.setOnClickListener(this)

            val hotbarTypeAdapter = ObjectSpinnerAdapter<HotbarType>(binding.hotbarType) { hotbarType ->
                activity.getString(hotbarType.nameId)
            }
            hotbarTypeAdapter.setItems(HotbarType.entries)
            binding.hotbarType.setSpinnerAdapter(hotbarTypeAdapter)
            binding.hotbarType.isFocusable = true
            binding.hotbarType.setOnSpinnerItemSelectedListener(this)
            binding.hotbarType.selectItemByIndex(HotbarUtils.getCurrentTypeIndex())

            binding.hotbarHeight.setOnSeekBarChangeListener(this)
            binding.hotbarHeightRemove.setOnClickListener(this)
            binding.hotbarHeightAdd.setOnClickListener(this)

            binding.hotbarWidth.setOnSeekBarChangeListener(this)
            binding.hotbarWidthRemove.setOnClickListener(this)
            binding.hotbarWidthAdd.setOnClickListener(this)
        }

        private fun showForceCloseDialog() {
            TipDialog.Builder(activity)
                .setTitle(R.string.option_force_close)
                .setMessage(R.string.game_menu_option_force_close_text)
                .setWarning()
                .setConfirmClickListener {
                    Terracotta.setWaiting(false)
                    try {
                        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ESCAPE.toInt(), 0, true)
                        CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ESCAPE.toInt(), 0, false)
                        CallbackBridge.nativeSetWindowAttrib(0x00020001, 1)
                    } catch (_: Throwable) {}
                    lifecycleScope.launch {
                        delay(1200)
                        ZLNativeInvoker.jvmExit(0, false)
                    }
                }
                .showDialog()
        }

        private fun dialogSendCustomKey() {
            keyboardDialog.setOnMultiKeycodeSelectListener { selectedKeycodes ->
                lifecycleScope.launch {
                    selectedKeycodes.forEach { keycode -> sendKeyPress(keycode, true) }
                    delay(50)
                    selectedKeycodes.forEach { keycode -> sendKeyPress(keycode, false) }
                }
            }.show()
        }

        private fun sendKeyPress(keycode: Int, isDown: Boolean) {
            val lwjglKeycode = EfficientAndroidLWJGLKeycode.getValueByIndex(keycode).toInt()
            if (keycode >= LwjglGlfwKeycode.GLFW_KEY_UNKNOWN) {
                CallbackBridge.sendKeyPress(lwjglKeycode, CallbackBridge.getCurrentMods(), isDown)
                CallbackBridge.setModifiers(lwjglKeycode, isDown)
            }
        }

        private fun replacementCustomControls() {
            val dialog = SelectControlsDialog(activity) { file ->
                try {
                    ControlManager.selectLayoutByFileName(file.name)
                    if (ControlManager.isZl1LayoutFile(file)) {
                        rootBinding.mainControlLayout.loadLayout(file.absolutePath)
                        gameMenuWrapper.setVisibility(!rootBinding.mainControlLayout.hasMenuButton())
                    } else {
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.cubiq_secondary_layout_next_launch, file.nameWithoutExtension),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: IOException) {
                    Tools.showError(activity, e)
                }
            }
            dialog.setTitleText(R.string.replacement_customcontrol)
            dialog.show()
        }

        private fun openCustomControls() {
            rootBinding.mainControlLayout.setModifiable(true)
            rootBinding.mainNavigationView.removeAllViews()
            rootBinding.mainNavigationView.addView(controlMenuBinding.root)
            gameMenuWrapper.setVisibility(true)
            isInEditor = true
        }

        override fun onClick(v: View?) {
            when (v) {
                binding.forceClose -> showForceCloseDialog()
                binding.logOutput -> {
                    rootBinding.mainDrawerOptions.closeDrawer(rootBinding.mainNavigationView)
                    onToggleLog()
                }
                binding.sendCustomKey -> dialogSendCustomKey()
                binding.openMemoryInfoLayout -> MenuUtils.toggleSwitchState(binding.openMemoryInfo)
                binding.openFpsInfoLayout -> MenuUtils.toggleSwitchState(binding.openFpsInfo)
                binding.resolutionScalerRemove -> MenuUtils.adjustSeekbar(binding.resolutionScaler, -1)
                binding.resolutionScalerAdd -> MenuUtils.adjustSeekbar(binding.resolutionScaler, 1)
                binding.disableGesturesLayout -> MenuUtils.toggleSwitchState(binding.disableGestures)
                binding.disableDoubleTapLayout -> MenuUtils.toggleSwitchState(binding.disableDoubleTap)
                binding.timeLongPressTriggerRemove -> MenuUtils.adjustSeekbar(binding.timeLongPressTrigger, -1)
                binding.timeLongPressTriggerAdd -> MenuUtils.adjustSeekbar(binding.timeLongPressTrigger, 1)
                binding.mouseSpeedRemove -> MenuUtils.adjustSeekbar(binding.mouseSpeed, -1)
                binding.mouseSpeedAdd -> MenuUtils.adjustSeekbar(binding.mouseSpeed, 1)
                binding.customMouse -> SelectMouseDialog(activity) {
                    rootBinding.mainTouchpad.updateMouseDrawable()
                }.show()
                binding.replacementCustomcontrol -> replacementCustomControls()
                binding.editControl -> openCustomControls()
                binding.enableGyroLayout -> MenuUtils.toggleSwitchState(binding.enableGyro)
                binding.gyroSensitivityRemove -> MenuUtils.adjustSeekbar(binding.gyroSensitivity, -1)
                binding.gyroSensitivityAdd -> MenuUtils.adjustSeekbar(binding.gyroSensitivity, 1)
                binding.gyroInvertXLayout -> MenuUtils.toggleSwitchState(binding.gyroInvertX)
                binding.gyroInvertYLayout -> MenuUtils.toggleSwitchState(binding.gyroInvertY)
                binding.hotbarWidthRemove -> MenuUtils.adjustSeekbar(binding.hotbarWidth, -1)
                binding.hotbarWidthAdd -> MenuUtils.adjustSeekbar(binding.hotbarWidth, 1)
                binding.hotbarHeightRemove -> MenuUtils.adjustSeekbar(binding.hotbarHeight, -1)
                binding.hotbarHeightAdd -> MenuUtils.adjustSeekbar(binding.hotbarHeight, 1)
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateSeekbarValue(seekBar, !fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            updateSeekbarValue(seekBar, true)
        }

        private fun updateSeekbarValue(seekBar: SeekBar?, saveValue: Boolean) {
            val progress = seekBar?.progress ?: 0
            when (seekBar) {
                binding.resolutionScaler -> {
                    val clamped = progress.coerceAtLeast(25)
                    if (saveValue) AllSettings.resolutionRatio.save(clamped)
                    MenuUtils.updateSeekbarValue(clamped, binding.resolutionScalerValue, "%")
                    binding.resolutionScalerPreview.text = getResolutionRatioPreview(clamped)
                    AllStaticSettings.scaleFactor = clamped / 100f
                    rootBinding.mainGameRenderView.refreshSize()
                }
                binding.timeLongPressTrigger -> {
                    val clamped = progress.coerceAtLeast(100)
                    if (saveValue) AllSettings.timeLongPressTrigger.save(clamped)
                    MenuUtils.updateSeekbarValue(clamped, binding.timeLongPressTriggerValue, "ms")
                    AllStaticSettings.timeLongPressTrigger = clamped
                }
                binding.mouseSpeed -> {
                    val clamped = progress.coerceAtLeast(25)
                    if (saveValue) AllSettings.mouseSpeed.save(clamped)
                    MenuUtils.updateSeekbarValue(clamped, binding.mouseSpeedValue, "%")
                }
                binding.gyroSensitivity -> {
                    val clamped = progress.coerceAtLeast(25)
                    if (saveValue) AllSettings.gyroscopeSensitivity.save(clamped)
                    MenuUtils.updateSeekbarValue(clamped, binding.gyroSensitivityValue, "%")
                    AllStaticSettings.gyroSensitivity = clamped.toFloat()
                }
                binding.hotbarWidth -> {
                    if (saveValue) AllSettings.zl1HotbarWidth.save(progress)
                    MenuUtils.updateSeekbarValue(progress, binding.hotbarWidthValue, "px")
                    EventBus.getDefault().post(HotbarChangeEvent(progress, binding.hotbarHeight.progress))
                }
                binding.hotbarHeight -> {
                    if (saveValue) AllSettings.zl1HotbarHeight.save(progress)
                    MenuUtils.updateSeekbarValue(progress, binding.hotbarHeightValue, "px")
                    EventBus.getDefault().post(HotbarChangeEvent(binding.hotbarWidth.progress, progress))
                }
            }
        }

        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            when (buttonView) {
                binding.openMemoryInfo -> {
                    AllSettings.gameMenuShowMemory.save(isChecked)
                    gameMenuWrapper.refreshSettingsState()
                }
                binding.openFpsInfo -> {
                    AllSettings.gameMenuShowFPS.save(isChecked)
                    gameMenuWrapper.refreshSettingsState()
                }
                binding.disableGestures -> {
                    refreshLayoutVisible(binding.timeLongPressTriggerLayout, !isChecked)
                    AllSettings.disableGestures.save(isChecked)
                }
                binding.disableDoubleTap -> {
                    AllSettings.disableDoubleTap.save(isChecked)
                    AllStaticSettings.disableDoubleTap = isChecked
                }
                binding.enableGyro -> {
                    refreshLayoutVisible(binding.gyroLayout, isChecked)
                    AllSettings.gyroscopeControl.save(isChecked)
                    AllStaticSettings.enableGyro = isChecked
                    gyroControl.updateOrientation()
                    if (isChecked) gyroControl.enable() else gyroControl.disable()
                }
                binding.gyroInvertX -> {
                    AllSettings.gyroscopeInvertX.save(isChecked)
                    AllStaticSettings.gyroInvertX = isChecked
                    gyroControl.updateOrientation()
                }
                binding.gyroInvertY -> {
                    AllSettings.gyroscopeInvertY.save(isChecked)
                    AllStaticSettings.gyroInvertY = isChecked
                    gyroControl.updateOrientation()
                }
            }
        }

        private fun refreshLayoutVisible(view: View, visible: Boolean) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }

        override fun onItemSelected(
            oldIndex: Int,
            oldItem: HotbarType?,
            newIndex: Int,
            newItem: HotbarType
        ) {
            if (newItem == HotbarType.AUTO) {
                binding.hotbarWidthLayout.visibility = View.GONE
                binding.hotbarHeightLayout.visibility = View.GONE
            } else if (newItem == HotbarType.MANUALLY) {
                binding.hotbarWidthLayout.visibility = View.VISIBLE
                binding.hotbarHeightLayout.visibility = View.VISIBLE
                binding.hotbarWidth.progress = AllSettings.zl1HotbarWidth.getValue().coerceIn(0, binding.hotbarWidth.max)
                binding.hotbarHeight.progress = AllSettings.zl1HotbarHeight.getValue().coerceIn(0, binding.hotbarHeight.max)
            }
            AllSettings.hotbarType.save(newItem.valueName)
            EventBus.getDefault().post(RefreshHotbarEvent())
        }

        override fun onDrawerSlide(@NonNull drawerView: View, slideOffset: Float) {}
        override fun onDrawerOpened(@NonNull drawerView: View) {}
        override fun onDrawerClosed(@NonNull drawerView: View) {}

        override fun onDrawerStateChanged(newState: Int) {
            binding.hotbarType.dismiss()
        }
    }
}

