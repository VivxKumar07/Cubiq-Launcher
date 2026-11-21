package dev.horizon.android

import android.app.NativeActivity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import com.movtery.zalithlauncher.ui.subassembly.view.GameMenuViewWrapper
import com.movtery.zalithlauncher.databinding.ViewGameMenuBinding
import net.kdt.pojavlaunch.customcontrols.mouse.HotbarView
import net.kdt.pojavlaunch.Tools
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.savedstate.*
import com.movtery.inputmap.keycodes.ControlEventKeycode
import com.movtery.layer_controller.ControlBoxLayout
import com.movtery.layer_controller.data.HideLayerWhen
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.event.EventHandler
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.layout.loadLayoutFromFile
import com.movtery.layer_controller.layout.loadLayoutFromString
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.CURSOR_ENABLED
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.input.EfficientAndroidLWJGLKeycode
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.ui.components.MinecraftPixelDonutSpinner
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.control.Keyboard
import com.movtery.zalithlauncher.ui.control.MinecraftHotbar
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SCROLL_DOWN
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SCROLL_DOWN_SINGLE
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SCROLL_UP
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SCROLL_UP_SINGLE
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SWITCH_IME
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SWITCH_MENU
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SWITCH_MOUSE
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadKeyListener
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadOnActionListener
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadStickMovementListener
import com.movtery.zalithlauncher.ui.control.gamepad.SimpleGamepadCapture
import com.movtery.zalithlauncher.context.copyAssetFile
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.setting.enums.toAction
import com.movtery.zalithlauncher.ui.activities.startEditorActivity
import com.movtery.zalithlauncher.ui.control.gyroscope.Zl1GyroControl
import com.movtery.zalithlauncher.ui.control.gyroscope.isGyroscopeAvailable
import com.movtery.zalithlauncher.ui.control.hotbarPercentage
import com.movtery.zalithlauncher.ui.control.mouse.SwitchableMouseLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.*
import com.movtery.zalithlauncher.ui.screens.main.control_editor.ControlEditor
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.toAndroidString
import com.movtery.zalithlauncher.ui.components.rememberBoxSize
import com.movtery.zalithlauncher.utils.computeGameRenderSize
import com.movtery.zalithlauncher.utils.currentGameDisplayLayout
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.EditorViewModel
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * Native host activity for Horizon (Minecraft Rust Edition).
 * Hosts the launcher controls UI (ZL1, ZL2, Mojo, Pocket) and forwards all game controls,
 * mouse/cursor, scroll, gamepad, gyro, and unconsumed multi-touch gestures directly into
 * NativeActivity's AInputQueue for libhorizon_android.so (winit).
 */
open class HorizonActivity : NativeActivity(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner, OnBackPressedDispatcherOwner, ActivityResultRegistryOwner {

    companion object {
        private const val TAG = "HorizonActivity"

        init {
            unsealHiddenApis()
            try {
                var filesDir = File("/data/user/0/com.cubiq.app/files")
                if (!filesDir.exists()) {
                    filesDir = File("/data/data/com.cubiq.app/files")
                }
                val nativeLibDir = File(filesDir, "app_data/lib")
                if (nativeLibDir.exists() && nativeLibDir.isDirectory) {
                    val horizonLib = File(nativeLibDir, "libhorizon_android.so")
                    if (horizonLib.exists()) {
                        System.load(horizonLib.absolutePath)
                    } else {
                        System.loadLibrary("horizon_android")
                    }
                } else {
                    System.loadLibrary("horizon_android")
                }
            } catch (t: Throwable) {
                try {
                    System.loadLibrary("horizon_android")
                } catch (_: Throwable) {}
            }
        }

        private fun unsealHiddenApis() {
            try {
                val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
                val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
                val getRuntimeMethod = vmRuntimeClass.getDeclaredMethod("getRuntime")
                val setHiddenApiExemptionsMethod = vmRuntimeClass.getDeclaredMethod("setHiddenApiExemptions", Array<String>::class.java)
                val vmRuntime = getRuntimeMethod.invoke(null)
                setHiddenApiExemptionsMethod.invoke(vmRuntime, arrayOf("L"))
                Log.i(TAG, "Successfully exempted hidden APIs")
            } catch (t: Throwable) {
                Log.w(TAG, "Hidden API unseal failed: ${t.message}")
            }
        }
    }

    private external fun nativeDocumentResult(request: Int, result: Int, uri: String?)

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val backDispatcher = OnBackPressedDispatcher()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val activityResultRegistryImpl = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            val intent = contract.createIntent(this@HorizonActivity, input)
            this@HorizonActivity.startActivityForResult(intent, requestCode, options?.toBundle())
        }
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val onBackPressedDispatcher: OnBackPressedDispatcher get() = backDispatcher
    override val activityResultRegistry: ActivityResultRegistry get() = activityResultRegistryImpl

    private var isOverlayAttached = false
    private var overlayContainer: PassthroughOverlayLayout? = null
    private var overlayLayoutParams: WindowManager.LayoutParams? = null
    private var windowManagerRef: WindowManager? = null

    @Volatile private var nativeInputQueue: InputQueue? = null
    @Volatile private var isEditingControlsActive: Boolean = false
    @Volatile private var isVirtualMouseActive: Boolean = false
    private val hudHitRects = ConcurrentHashMap<String, Rect>()

    private var inputReflectInitialized = false
    private var iqSendEventMethod: Method? = null
    private var iqFinishedCallbackProxy: Any? = null
    private var iqPtrField: Field? = null
    private var iqNativeSendKeyMethod: Method? = null
    private var iqNativeSendMotionMethod: Method? = null
    private var vriGetterMethod: Method? = null
    private var vriDispatchInputMethod: Method? = null
    private var vriEnqueueInputMethod: Method? = null
    private var loggedInputRoute: String? = null

    private fun ensureInputReflectionInitialized() {
        if (inputReflectInitialized) return
        inputReflectInitialized = true
        try {
            val iqClass = InputQueue::class.java
            val cbClass = runCatching { Class.forName("android.view.InputQueue\$FinishedInputEventCallback") }.getOrNull()
            if (cbClass != null) {
                iqSendEventMethod = runCatching {
                    iqClass.getDeclaredMethod(
                        "sendInputEvent",
                        InputEvent::class.java,
                        Any::class.java,
                        Boolean::class.javaPrimitiveType,
                        cbClass
                    ).apply { isAccessible = true }
                }.getOrNull()
                if (iqSendEventMethod != null) {
                    iqFinishedCallbackProxy = Proxy.newProxyInstance(
                        cbClass.classLoader,
                        arrayOf(cbClass)
                    ) { _, _, _ -> null }
                }
            }
            iqPtrField = runCatching { iqClass.getDeclaredField("mPtr").apply { isAccessible = true } }.getOrNull()
            iqNativeSendKeyMethod = runCatching {
                iqClass.getDeclaredMethod(
                    "nativeSendKeyEvent",
                    Long::class.javaPrimitiveType,
                    KeyEvent::class.java,
                    Boolean::class.javaPrimitiveType
                ).apply { isAccessible = true }
            }.getOrNull()
            iqNativeSendMotionMethod = runCatching {
                iqClass.getDeclaredMethod(
                    "nativeSendMotionEvent",
                    Long::class.javaPrimitiveType,
                    MotionEvent::class.java
                ).apply { isAccessible = true }
            }.getOrNull()
        } catch (t: Throwable) {
            Log.w(TAG, "InputQueue reflection setup issue: ${t.message}")
        }

        runCatching {
            vriGetterMethod = View::class.java.getDeclaredMethod("getViewRootImpl").apply { isAccessible = true }
            val vriClass = Class.forName("android.view.ViewRootImpl")
            vriDispatchInputMethod = runCatching {
                vriClass.getDeclaredMethod("dispatchInputEvent", InputEvent::class.java).apply { isAccessible = true }
            }.getOrNull()
            vriEnqueueInputMethod = runCatching {
                vriClass.getDeclaredMethod("enqueueInputEvent", InputEvent::class.java).apply { isAccessible = true }
            }.getOrNull()
        }
    }

    private fun cloneInputEvent(event: InputEvent): InputEvent = when (event) {
        is MotionEvent -> MotionEvent.obtain(event)
        is KeyEvent -> KeyEvent(event)
        else -> event
    }

    override fun onInputQueueCreated(queue: InputQueue) {
        super.onInputQueueCreated(queue)
        nativeInputQueue = queue
        Log.i(TAG, "Captured NativeActivity InputQueue")
    }

    override fun onInputQueueDestroyed(queue: InputQueue) {
        if (nativeInputQueue === queue) {
            nativeInputQueue = null
        }
        super.onInputQueueDestroyed(queue)
    }

    private var nativeSurfaceHolder: SurfaceHolder? = null

    override fun surfaceCreated(holder: SurfaceHolder) {
        super.surfaceCreated(holder)
        nativeSurfaceHolder = holder
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        super.surfaceChanged(holder, format, width, height)
        nativeSurfaceHolder = holder
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (nativeSurfaceHolder === holder) {
            nativeSurfaceHolder = null
        }
        super.surfaceDestroyed(holder)
    }

    fun applyGameResolution(width: Int, height: Int) {
        runOnUiThread {
            runCatching {
                runCatching { window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) }
                val holder = nativeSurfaceHolder
                if (holder != null) {
                    if (width > 0 && height > 0) {
                        runCatching { holder.setFixedSize(width, height) }
                    } else {
                        runCatching { holder.setSizeFromLayout() }
                    }
                }
            }
        }
    }

    fun refreshGameResolution() {
        val size = com.movtery.zalithlauncher.utils.getRealScreenSize(this)
        if (size.width > 0 && size.height > 0) {
            val renderSize = com.movtery.zalithlauncher.utils.computeGameRenderSize(size)
            if (renderSize.width > 0 && renderSize.height > 0 &&
                (renderSize.width != size.width || renderSize.height != size.height)) {
                applyGameResolution(renderSize.width, renderSize.height)
            } else {
                applyGameResolution(-1, -1)
            }
        }
    }

    /**
     * Dispatches any InputEvent (KeyEvent or MotionEvent) directly into NativeActivity's NDK AInputQueue
     * (with safe fallbacks to nativeSendKey/MotionEvent and ViewRootImpl), always marshalled to Main Looper.
     */
    fun dispatchInputEventToNative(event: InputEvent): Boolean {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            val copy = cloneInputEvent(event)
            mainHandler.post { dispatchInputEventToNative(copy) }
            return true
        }

        ensureInputReflectionInitialized()
        val iq = nativeInputQueue

        if (iq != null) {
            val sendMethod = iqSendEventMethod
            val cbProxy = iqFinishedCallbackProxy
            if (sendMethod != null && cbProxy != null) {
                try {
                    val copy = cloneInputEvent(event)
                    sendMethod.invoke(iq, copy, null, false, cbProxy)
                    if (loggedInputRoute != "InputQueue.sendInputEvent") {
                        loggedInputRoute = "InputQueue.sendInputEvent"
                        Log.i(TAG, "Native input route active: InputQueue.sendInputEvent")
                    }
                    return true
                } catch (t: Throwable) {
                    Log.w(TAG, "InputQueue.sendInputEvent failed, falling back: ${t.message}")
                }
            }

            val ptr = runCatching { (iqPtrField?.get(iq) as? Long) ?: 0L }.getOrDefault(0L)
            if (ptr != 0L) {
                try {
                    when (event) {
                        is KeyEvent -> iqNativeSendKeyMethod?.invoke(null, ptr, KeyEvent(event), false)
                        is MotionEvent -> iqNativeSendMotionMethod?.invoke(null, ptr, MotionEvent.obtain(event))
                    }
                    if (loggedInputRoute != "InputQueue.nativeSend") {
                        loggedInputRoute = "InputQueue.nativeSend"
                        Log.i(TAG, "Native input route active: InputQueue.nativeSend")
                    }
                    return true
                } catch (t: Throwable) {
                    Log.w(TAG, "InputQueue.nativeSend failed, falling back: ${t.message}")
                }
            }
        }

        try {
            val decor = window?.decorView
            val vri = if (decor != null) vriGetterMethod?.invoke(decor) else null
            if (vri != null) {
                val method = vriDispatchInputMethod ?: vriEnqueueInputMethod
                if (method != null) {
                    method.invoke(vri, cloneInputEvent(event))
                    if (loggedInputRoute != "ViewRootImpl") {
                        loggedInputRoute = "ViewRootImpl"
                        Log.i(TAG, "Native input route active: ViewRootImpl")
                    }
                    return true
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "ViewRootImpl dispatch failed: ${t.message}")
        }

        return try {
            when (event) {
                is KeyEvent -> window.superDispatchKeyEvent(event)
                is MotionEvent -> {
                    if (event.isFromSource(InputDevice.SOURCE_CLASS_POINTER) &&
                        event.actionMasked != MotionEvent.ACTION_HOVER_MOVE &&
                        event.actionMasked != MotionEvent.ACTION_SCROLL
                    ) {
                        window.superDispatchTouchEvent(event)
                    } else {
                        window.superDispatchGenericMotionEvent(event)
                    }
                }
                else -> false
            }
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Overlay container hosting the Compose UI.
     */
    inner class PassthroughOverlayLayout(context: Context) : FrameLayout(context) {
        private var isModalState = false

        fun setModalState(isModal: Boolean) {
            isModalState = isModal
        }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            return super.dispatchTouchEvent(ev)
        }
    }

    fun updateOverlayFocusable(focusable: Boolean) {
        val container = overlayContainer ?: return
        val params = overlayLayoutParams ?: return
        val wm = windowManagerRef ?: return
        val currentlyFocusable = (params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) == 0
        if (currentlyFocusable == focusable) return
        params.flags = if (focusable) {
            params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        } else {
            params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        runCatching { wm.updateViewLayout(container, params) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        savedStateRegistryController.performRestore(savedInstanceState)
        super.onCreate(savedInstanceState)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        window.decorView.setViewTreeLifecycleOwner(this)
        window.decorView.setViewTreeViewModelStoreOwner(this)
        window.decorView.setViewTreeSavedStateRegistryOwner(this)
        window.decorView.setViewTreeOnBackPressedDispatcherOwner(this)

        title = "Minecraft Rust Edition"

        net.kdt.pojavlaunch.Tools.updateWindowSize(this)
        ControlManager.checkDefaultAndRefresh(this)

        setupInputBridge()

        window.decorView.post {
            attachOverlayIfNeeded()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        net.kdt.pojavlaunch.Tools.updateWindowSize(this)
        attachOverlayIfNeeded()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            net.kdt.pojavlaunch.Tools.updateWindowSize(this)
            attachOverlayIfNeeded()
        }
    }

    private fun attachOverlayIfNeeded() {
        if (isOverlayAttached || isFinishing || isDestroyed) return
        val decor = window?.decorView ?: return
        val token = decor.windowToken ?: return

        try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManagerRef = wm

            val container = PassthroughOverlayLayout(this).apply {
                setViewTreeLifecycleOwner(this@HorizonActivity)
                setViewTreeViewModelStoreOwner(this@HorizonActivity)
                setViewTreeSavedStateRegistryOwner(this@HorizonActivity)
                setViewTreeOnBackPressedDispatcherOwner(this@HorizonActivity)
            }

            val composeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@HorizonActivity)
                setViewTreeViewModelStoreOwner(this@HorizonActivity)
                setViewTreeSavedStateRegistryOwner(this@HorizonActivity)
                setViewTreeOnBackPressedDispatcherOwner(this@HorizonActivity)
                setContent {
                    CompositionLocalProvider(
                        LocalActivityResultRegistryOwner provides this@HorizonActivity
                    ) {
                        ZalithLauncherTheme {
                            HorizonInGameScreen()
                        }
                    }
                }
            }

            container.addView(
                composeView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                this.token = token
                gravity = Gravity.FILL
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = window?.attributes?.layoutInDisplayCutoutMode
                        ?: WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            wm.addView(container, params)
            overlayContainer = container
            overlayLayoutParams = params
            isOverlayAttached = true
            Log.i(TAG, "Successfully attached Compose overlay via WindowManager TYPE_APPLICATION_PANEL")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to attach overlay: ${e.message}", e)
        }
    }

    private var lastCursorX = 500f
    private var lastCursorY = 500f
    private var lastRawCursorX = 500f
    private var lastRawCursorY = 500f
    private var activeMetaState = 0
    private var currentMouseButtonState = 0

    private fun updateMetaState(androidKeycode: Int, pressed: Boolean) {
        val bit = when (androidKeycode) {
            KeyEvent.KEYCODE_SHIFT_LEFT -> KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
            KeyEvent.KEYCODE_SHIFT_RIGHT -> KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_RIGHT_ON
            KeyEvent.KEYCODE_CTRL_LEFT -> KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
            KeyEvent.KEYCODE_CTRL_RIGHT -> KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_RIGHT_ON
            KeyEvent.KEYCODE_ALT_LEFT -> KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
            KeyEvent.KEYCODE_ALT_RIGHT -> KeyEvent.META_ALT_ON or KeyEvent.META_ALT_RIGHT_ON
            else -> 0
        }
        if (bit == 0) return
        activeMetaState = if (pressed) {
            activeMetaState or bit
        } else {
            val remaining = activeMetaState and bit.inv()
            when {
                androidKeycode == KeyEvent.KEYCODE_SHIFT_LEFT || androidKeycode == KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                    val hasOther = (remaining and (KeyEvent.META_SHIFT_LEFT_ON or KeyEvent.META_SHIFT_RIGHT_ON)) != 0
                    if (hasOther) remaining or KeyEvent.META_SHIFT_ON else remaining and KeyEvent.META_SHIFT_ON.inv()
                }
                androidKeycode == KeyEvent.KEYCODE_CTRL_LEFT || androidKeycode == KeyEvent.KEYCODE_CTRL_RIGHT -> {
                    val hasOther = (remaining and (KeyEvent.META_CTRL_LEFT_ON or KeyEvent.META_CTRL_RIGHT_ON)) != 0
                    if (hasOther) remaining or KeyEvent.META_CTRL_ON else remaining and KeyEvent.META_CTRL_ON.inv()
                }
                else -> {
                    val hasOther = (remaining and (KeyEvent.META_ALT_LEFT_ON or KeyEvent.META_ALT_RIGHT_ON)) != 0
                    if (hasOther) remaining or KeyEvent.META_ALT_ON else remaining and KeyEvent.META_ALT_ON.inv()
                }
            }
        }
    }

    private fun glfwToAndroidKeycode(glfwCode: Int): Int {
        val converted = EfficientAndroidLWJGLKeycode.getSdlAndroidKeycode(glfwCode)
        if (converted != KeyEvent.KEYCODE_UNKNOWN) {
            return converted
        }
        val fallback = EfficientAndroidLWJGLKeycode.getAndroidKeycode(glfwCode)
        if (fallback != KeyEvent.KEYCODE_UNKNOWN) {
            return fallback
        }
        return KeyEvent.KEYCODE_UNKNOWN
    }

    fun dispatchAndroidKey(androidKeycode: Int, pressed: Boolean) {
        if (androidKeycode <= KeyEvent.KEYCODE_UNKNOWN || androidKeycode > KeyEvent.getMaxKeyCode()) return
        updateMetaState(androidKeycode, pressed)
        val now = SystemClock.uptimeMillis()
        val keyEvent = KeyEvent(
            now,
            now,
            if (pressed) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP,
            androidKeycode,
            0,
            activeMetaState
        )
        dispatchInputEventToNative(keyEvent)
    }

    fun dispatchGlfwKey(glfwKey: Int, pressed: Boolean) {
        val androidKeycode = glfwToAndroidKeycode(glfwKey)
        if (androidKeycode != KeyEvent.KEYCODE_UNKNOWN) {
            dispatchAndroidKey(androidKeycode, pressed)
        }
    }

    fun dispatchGlfwKeyTap(glfwKey: Int) {
        dispatchGlfwKey(glfwKey, true)
        dispatchGlfwKey(glfwKey, false)
    }

    fun dispatchTextString(text: String) {
        if (text.isEmpty()) return
        val kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
        val events = kcm.getEvents(text.toCharArray())
        if (events != null) {
            for (ev in events) {
                dispatchInputEventToNative(ev)
            }
        }
    }

    fun dispatchCursorMove(x: Float, y: Float) {
        val dx = x - lastRawCursorX
        val dy = y - lastRawCursorY
        lastRawCursorX = x
        lastRawCursorY = y

        val maxW = (CallbackBridge.windowWidth - 1).coerceAtLeast(1).toFloat()
        val maxH = (CallbackBridge.windowHeight - 1).coerceAtLeast(1).toFloat()
        val cx = x.coerceIn(0f, maxW)
        val cy = y.coerceIn(0f, maxH)
        lastCursorX = cx
        lastCursorY = cy

        val now = SystemClock.uptimeMillis()
        val pp = arrayOf(MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_MOUSE
        })
        val pc = arrayOf(MotionEvent.PointerCoords().apply {
            this.x = cx
            this.y = cy
            setAxisValue(MotionEvent.AXIS_RELATIVE_X, dx)
            setAxisValue(MotionEvent.AXIS_RELATIVE_Y, dy)
        })
        val action = if (currentMouseButtonState != 0) MotionEvent.ACTION_MOVE else MotionEvent.ACTION_HOVER_MOVE
        val ev = MotionEvent.obtain(
            now, now, action, 1, pp, pc, activeMetaState, currentMouseButtonState,
            1.0f, 1.0f, 0, 0, InputDevice.SOURCE_MOUSE, 0
        )
        dispatchInputEventToNative(ev)
        ev.recycle()
    }

    fun dispatchRelativeMotion(dx: Float, dy: Float) {
        if (dx == 0f && dy == 0f) return
        val now = SystemClock.uptimeMillis()
        val pp = arrayOf(MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_MOUSE
        })
        val pc = arrayOf(MotionEvent.PointerCoords().apply {
            x = lastCursorX
            y = lastCursorY
            setAxisValue(MotionEvent.AXIS_RELATIVE_X, dx)
            setAxisValue(MotionEvent.AXIS_RELATIVE_Y, dy)
        })
        val ev = MotionEvent.obtain(
            now, now, MotionEvent.ACTION_MOVE, 1, pp, pc, activeMetaState, currentMouseButtonState,
            1.0f, 1.0f, 0, 0, InputDevice.SOURCE_MOUSE, 0
        )
        dispatchInputEventToNative(ev)
        ev.recycle()
    }

    fun dispatchAndroidMouseButton(androidButton: Int, pressed: Boolean) {
        val buttonBit = when (androidButton) {
            MotionEvent.BUTTON_PRIMARY,
            MotionEvent.BUTTON_SECONDARY,
            MotionEvent.BUTTON_TERTIARY,
            MotionEvent.BUTTON_BACK,
            MotionEvent.BUTTON_FORWARD -> androidButton
            else -> MotionEvent.BUTTON_PRIMARY
        }
        val prevState = currentMouseButtonState
        currentMouseButtonState = if (pressed) prevState or buttonBit else prevState and buttonBit.inv()

        val now = SystemClock.uptimeMillis()
        val pp = arrayOf(MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_MOUSE
        })
        val pc = arrayOf(MotionEvent.PointerCoords().apply {
            this.x = lastCursorX
            this.y = lastCursorY
        })
        val action = when {
            pressed && prevState == 0 -> MotionEvent.ACTION_DOWN
            !pressed && currentMouseButtonState == 0 -> MotionEvent.ACTION_UP
            else -> MotionEvent.ACTION_MOVE
        }
        val ev = MotionEvent.obtain(
            now, now, action, 1, pp, pc, activeMetaState, currentMouseButtonState,
            1.0f, 1.0f, 0, 0, InputDevice.SOURCE_MOUSE, 0
        )
        dispatchInputEventToNative(ev)
        ev.recycle()
    }

    fun dispatchGlfwMouseButton(glfwButton: Int, pressed: Boolean) {
        val androidButton = when (glfwButton) {
            0 -> MotionEvent.BUTTON_PRIMARY
            1 -> MotionEvent.BUTTON_SECONDARY
            2 -> MotionEvent.BUTTON_TERTIARY
            3 -> MotionEvent.BUTTON_BACK
            4 -> MotionEvent.BUTTON_FORWARD
            else -> MotionEvent.BUTTON_PRIMARY
        }
        dispatchAndroidMouseButton(androidButton, pressed)
    }

    fun dispatchScroll(xoffset: Double, yoffset: Double) {
        val now = SystemClock.uptimeMillis()
        val pp = arrayOf(MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_MOUSE
        })
        val pc = arrayOf(MotionEvent.PointerCoords().apply {
            this.x = lastCursorX
            this.y = lastCursorY
            setAxisValue(MotionEvent.AXIS_HSCROLL, xoffset.toFloat())
            setAxisValue(MotionEvent.AXIS_VSCROLL, yoffset.toFloat())
        })
        val ev = MotionEvent.obtain(
            now, now, MotionEvent.ACTION_SCROLL, 1, pp, pc, activeMetaState, currentMouseButtonState,
            1.0f, 1.0f, 0, 0, InputDevice.SOURCE_MOUSE, 0
        )
        dispatchInputEventToNative(ev)
        ev.recycle()
    }

    private fun setupInputBridge() {
        CallbackBridge.setNativeInputListener(object : CallbackBridge.NativeInputListener {
            override fun onKey(key: Int, keyChar: Char, scancode: Int, modifiers: Int, status: Boolean) {
                dispatchGlfwKey(key, status)
            }

            override fun onCursor(x: Float, y: Float) {
                dispatchCursorMove(x, y)
            }

            override fun onCursorDelta(dx: Float, dy: Float) {
                dispatchRelativeMotion(dx, dy)
            }

            override fun onMouseButton(button: Int, status: Boolean) {
                dispatchGlfwMouseButton(button, status)
            }

            override fun onScroll(xoffset: Double, yoffset: Double) {
                dispatchScroll(xoffset, yoffset)
            }
        })
    }

    @Composable
    private fun HorizonInGameScreen() {
        var showSplash by remember { mutableStateOf(true) }
        var isMenuOpen by remember { mutableStateOf(false) }
        var isEditingControls by remember { mutableStateOf(false) }
        var showLogs by remember { mutableStateOf(false) }
        var controlMenuTabIndex by remember { mutableIntStateOf(0) }
        var forceCloseState by remember { mutableStateOf<ForceCloseOperation>(ForceCloseOperation.None) }
        var replacementControlState by remember { mutableStateOf<ReplacementControlState>(ReplacementControlState.None) }
        var sendKeycodeState by remember { mutableStateOf<SendKeycodeState>(SendKeycodeState.None) }
        var layoutReloadVersion by remember { mutableIntStateOf(0) }
        var virtualMouseOverride by remember { mutableStateOf<Boolean?>(null) }

        val coroutineScope = rememberCoroutineScope()
        val gamepadViewModel = remember {
            ViewModelProvider(this@HorizonActivity)[GamepadViewModel::class.java]
        }
        val editorViewModel = remember {
            ViewModelProvider(this@HorizonActivity)[EditorViewModel::class.java]
        }

        // Start splash fade timer
        LaunchedEffect(Unit) {
            delay(3200)
            showSplash = false
        }

        var activeLayoutName by remember {
            val initial = intent.getStringExtra("EXTRA_CONTROL_LAYOUT_NAME")
                ?: runCatching { AllSettings.controlLayout.getValue() }.getOrNull()
                ?: "zalith_1_layout.json"
            mutableStateOf(if (initial.isNotBlank()) initial else "zalith_1_layout.json")
        }

        var controlsOpacity by remember {
            mutableFloatStateOf((runCatching { AllSettings.controlsOpacity.state.toFloat() / 100f }.getOrDefault(0.8f)).coerceIn(0.1f, 1f))
        }

        val layoutFile = remember(activeLayoutName) {
            File(PathManager.DIR_CONTROL_LAYOUTS, activeLayoutName)
        }

        val isPocket = activeLayoutName.contains("pocket", ignoreCase = true)

        val bridgeCursorMode by ZLBridgeStates.cursorMode.collectAsStateWithLifecycle()
        val hideMouseSetting = AllSettings.hideMouse.state
        val isVirtualMouseMode = if (hideMouseSetting) {
            false
        } else {
            virtualMouseOverride ?: (bridgeCursorMode == CURSOR_ENABLED)
        }

        LaunchedEffect(isEditingControls, isVirtualMouseMode) {
            isEditingControlsActive = isEditingControls
            isVirtualMouseActive = isVirtualMouseMode
        }

        // Real-time Choreographer frame rate meter (replaces hardcoded 60)
        val showFpsSetting = AllSettings.showFPS.state
        var liveFps by remember { mutableStateOf<Int?>(null) }
        DisposableEffect(showFpsSetting, showSplash) {
            if (!showFpsSetting || showSplash) {
                liveFps = null
                onDispose {}
            } else {
                var frameCount = 0
                var lastSampleNs = 0L
                val choreographer = Choreographer.getInstance()
                val callback = object : Choreographer.FrameCallback {
                    override fun doFrame(frameTimeNanos: Long) {
                        if (lastSampleNs == 0L) {
                            lastSampleNs = frameTimeNanos
                        } else {
                            frameCount++
                            val elapsedNs = frameTimeNanos - lastSampleNs
                            if (elapsedNs >= 500_000_000L) {
                                val fps = ((frameCount * 1_000_000_000.0) / elapsedNs).roundToInt().coerceAtLeast(1)
                                liveFps = fps
                                frameCount = 0
                                lastSampleNs = frameTimeNanos
                            }
                        }
                        choreographer.postFrameCallback(this)
                    }
                }
                choreographer.postFrameCallback(callback)
                onDispose {
                    choreographer.removeFrameCallback(callback)
                }
            }
        }
        val gameFps: Int? = if (showFpsSetting) liveFps else null

        val moveOnlyPointers = remember { mutableSetOf<PointerId>() }
        val occupiedPointers = remember { mutableSetOf<PointerId>() }
        val ballPosSetting = if (isPocket) AllSettings.menuBallPosPocket else AllSettings.menuBallPos

        var drawerLayoutRef by remember { mutableStateOf<DrawerLayout?>(null) }
        var isDrawerOpen by remember { mutableStateOf(false) }

        val isModalOpen = isMenuOpen ||
                isDrawerOpen ||
                forceCloseState == ForceCloseOperation.Show ||
                replacementControlState == ReplacementControlState.Show ||
                sendKeycodeState == SendKeycodeState.ShowDialog ||
                showLogs
        val isUiGameplayGrabbing = !showSplash && !isModalOpen && !isEditingControls

        androidx.activity.compose.BackHandler(enabled = isDrawerOpen) {
            drawerLayoutRef?.let { drawer ->
                val nav = drawer.getChildAt(1)
                if (nav != null && drawer.isDrawerOpen(nav)) {
                    drawer.closeDrawer(nav)
                }
            }
        }

        val setupDrawerMenuBinding: (ViewGameMenuBinding, DrawerLayout, View, File) -> Unit = remember {
            { binding, drawerLayout, navContainer, file ->
                binding.forceClose.setOnClickListener {
                    drawerLayout.closeDrawer(navContainer)
                    forceCloseState = ForceCloseOperation.Show
                }

                binding.logOutput.setOnClickListener {
                    drawerLayout.closeDrawer(navContainer)
                    showLogs = !showLogs
                }

                binding.sendCustomKey.setOnClickListener {
                    drawerLayout.closeDrawer(navContainer)
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    @Suppress("DEPRECATION")
                    imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                }

                binding.customMouse.setOnClickListener {
                    val newHide = !AllSettings.hideMouse.getValue()
                    AllSettings.hideMouse.save(newHide)
                    virtualMouseOverride = !newHide
                    ZLBridgeStates.changeCursorMode(if (!newHide) CURSOR_ENABLED else CURSOR_DISABLED)
                    Toast.makeText(this@HorizonActivity, if (newHide) "Mouse Disabled" else "Mouse Enabled", Toast.LENGTH_SHORT).show()
                }

                binding.replacementCustomcontrol.setOnClickListener {
                    drawerLayout.closeDrawer(navContainer)
                    replacementControlState = ReplacementControlState.Show
                }

                binding.editControl.setOnClickListener {
                    drawerLayout.closeDrawer(navContainer)
                    startEditorActivity(this@HorizonActivity, file)
                }

                binding.openMemoryInfo.isChecked = AllSettings.gameMenuShowMemory.getValue()
                binding.openMemoryInfo.setOnCheckedChangeListener { _, isChecked ->
                    AllSettings.gameMenuShowMemory.save(isChecked)
                }
                binding.openMemoryInfoLayout.setOnClickListener {
                    binding.openMemoryInfo.toggle()
                }

                binding.openFpsInfo.isChecked = AllSettings.gameMenuShowFPS.getValue()
                binding.openFpsInfo.setOnCheckedChangeListener { _, isChecked ->
                    AllSettings.gameMenuShowFPS.save(isChecked)
                }
                binding.openFpsInfoLayout.setOnClickListener {
                    binding.openFpsInfo.toggle()
                }
            }
        }

        // Keep overlay focusability & modal touch blocking in sync (enables EditText typing in editors/dialogs)
        LaunchedEffect(isModalOpen, isEditingControls) {
            overlayContainer?.setModalState(isModalOpen)
            updateOverlayFocusable(isModalOpen || isEditingControls)
        }

        // Continuous scroll jobs for hold-to-scroll control buttons
        var scrollUpJob by remember { mutableStateOf<Job?>(null) }
        var scrollDownJob by remember { mutableStateOf<Job?>(null) }
        DisposableEffect(Unit) {
            onDispose {
                scrollUpJob?.cancel()
                scrollDownJob?.cancel()
            }
        }

        val handleClickEvent: (ClickEvent, Boolean) -> Unit = remember {
            { event: ClickEvent, pressed: Boolean ->
                when (event.type) {
                    ClickEvent.Type.Key -> {
                        val keycode = ControlEventKeycode.getKeycodeFromEvent(event.key)?.toInt()
                        if (keycode != null) {
                            if (event.key.startsWith("GLFW_MOUSE_", false)) {
                                dispatchGlfwMouseButton(keycode, pressed)
                            } else {
                                CallbackBridge.setModifiers(keycode, pressed)
                                dispatchGlfwKey(keycode, pressed)
                            }
                        }
                    }
                    ClickEvent.Type.LauncherEvent -> {
                        if (event.key.startsWith("GLFW_MOUSE_", false)) {
                            val keycode = ControlEventKeycode.getKeycodeFromEvent(event.key)?.toInt()
                            if (keycode != null) {
                                dispatchGlfwMouseButton(keycode, pressed)
                            }
                        } else {
                            when (event.key) {
                                LAUNCHER_EVENT_SWITCH_IME -> if (pressed) {
                                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                    @Suppress("DEPRECATION")
                                    imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                                }
                                LAUNCHER_EVENT_SWITCH_MENU -> if (pressed) {
                                    isMenuOpen = !isMenuOpen
                                }
                                LAUNCHER_EVENT_SWITCH_MOUSE -> if (pressed) {
                                    val nextMode = if (isVirtualMouseMode) CURSOR_DISABLED else CURSOR_ENABLED
                                    ZLBridgeStates.changeCursorMode(nextMode)
                                    virtualMouseOverride = (nextMode == CURSOR_ENABLED)
                                }
                                LAUNCHER_EVENT_SCROLL_UP_SINGLE -> if (pressed) {
                                    dispatchScroll(0.0, 1.0)
                                }
                                LAUNCHER_EVENT_SCROLL_DOWN_SINGLE -> if (pressed) {
                                    dispatchScroll(0.0, -1.0)
                                }
                                LAUNCHER_EVENT_SCROLL_UP -> {
                                    if (pressed) {
                                        if (scrollUpJob?.isActive != true) {
                                            scrollUpJob = coroutineScope.launch {
                                                while (isActive) {
                                                    dispatchScroll(0.0, 1.0)
                                                    delay(120)
                                                }
                                            }
                                        }
                                    } else {
                                        scrollUpJob?.cancel()
                                        scrollUpJob = null
                                    }
                                }
                                LAUNCHER_EVENT_SCROLL_DOWN -> {
                                    if (pressed) {
                                        if (scrollDownJob?.isActive != true) {
                                            scrollDownJob = coroutineScope.launch {
                                                while (isActive) {
                                                    dispatchScroll(0.0, -1.0)
                                                    delay(120)
                                                }
                                            }
                                        }
                                    } else {
                                        scrollDownJob?.cancel()
                                        scrollDownJob = null
                                    }
                                }
                            }
                        }
                    }
                    ClickEvent.Type.SendText -> {
                        if (pressed) {
                            dispatchTextString(event.key)
                        }
                    }
                    else -> {}
                }
            }
        }
        val eventHandler = remember {
            EventHandler { event, pressed -> handleClickEvent(event, pressed) }
        }

        // Gamepad listeners (identical to Java Edition)
        GamepadOnActionListener(gamepadViewModel = gamepadViewModel) { }
        GamepadKeyListener(
            gamepadViewModel = gamepadViewModel,
            isGrabbing = isUiGameplayGrabbing,
            onKeyEvent = { targets, pressed ->
                targets.forEach { target -> handleClickEvent(target, pressed) }
            }
        )
        GamepadStickMovementListener(
            gamepadViewModel = gamepadViewModel,
            isGrabbing = isUiGameplayGrabbing,
            onKeyEvent = { event, pressed ->
                handleClickEvent(event, pressed)
            }
        )

        // ZL1 Gyroscope controller for Horizon Rust Edition
        val gyroAvailable = remember { isGyroscopeAvailable(this@HorizonActivity) }
        val gyroActive = AllSettings.gyroscopeControl.state
        DisposableEffect(gyroAvailable, gyroActive, showSplash) {
            val gyro = if (gyroAvailable && gyroActive && !showSplash) {
                Zl1GyroControl(this@HorizonActivity).apply {
                    onDeltaListener = { dx, dy ->
                        // Match Java edition: Only apply gyro when grabbing during active gameplay, never in menus or virtual mouse
                        if (!isModalOpen && !isVirtualMouseMode && isUiGameplayGrabbing && CallbackBridge.isGrabbing()) {
                            dispatchRelativeMotion(dx, dy)
                        }
                    }
                    enable(alwaysHandle = true)
                }
            } else null
            onDispose {
                gyro?.disable()
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenSize = rememberBoxSize()

            // Dynamically apply game resolution scaling to NativeActivity window
            LaunchedEffect(
                AllSettings.resolutionRatio.state,
                AllSettings.resolutionRule.state,
                AllSettings.customResolutionWidth.state,
                AllSettings.customResolutionHeight.state,
                screenSize
            ) {
                if (screenSize.width > 0 && screenSize.height > 0) {
                    val renderSize = computeGameRenderSize(screenSize)
                    if (renderSize.width > 0 && renderSize.height > 0 &&
                        (renderSize.width != screenSize.width || renderSize.height != screenSize.height)
                    ) {
                        applyGameResolution(renderSize.width, renderSize.height)
                    } else {
                        applyGameResolution(-1, -1)
                    }
                }
            }

            // Sync CallbackBridge dimensions and center initial cursor once measured
            var cursorInitialized by remember { mutableStateOf(false) }
            LaunchedEffect(screenSize) {
                if (screenSize.width > 0 && screenSize.height > 0) {
                    CallbackBridge.windowWidth = screenSize.width
                    CallbackBridge.windowHeight = screenSize.height
                    CallbackBridge.physicalWidth = screenSize.width
                    CallbackBridge.physicalHeight = screenSize.height
                    if (!cursorInitialized) {
                        val cx = screenSize.width / 2f
                        val cy = screenSize.height / 2f
                        lastCursorX = cx
                        lastCursorY = cy
                        lastRawCursorX = cx
                        lastRawCursorY = cy
                        CallbackBridge.mouseX = cx
                        CallbackBridge.mouseY = cy
                        cursorInitialized = true
                    }
                }
            }

            // Mouse / Direct Touch Input Surface (shared underneath Legacy or inside ControlBoxLayout)
            val mouseOrTouchLayer: @Composable () -> Unit = {
                if (isVirtualMouseMode) {
                    val mouseSpeed = (AllSettings.mouseSpeed.state / 100f).coerceIn(0.1f, 3.0f)
                    SwitchableMouseLayout(
                        modifier = Modifier.fillMaxSize(),
                        screenSize = screenSize,
                        cursorMode = if (isVirtualMouseMode) CURSOR_ENABLED else CURSOR_DISABLED,
                        controlMode = MouseControlMode.SLIDE,
                        hideMouseInClickMode = false,
                        gamepadViewModel = if (AllSettings.gamepadControl.state) gamepadViewModel else null,
                        onTap = { pos ->
                            dispatchCursorMove(pos.x, pos.y)
                            if (AllSettings.gestureControl.state) {
                                val action = AllSettings.gestureTapMouseAction.state.toAction()
                                dispatchGlfwMouseButton(action, true)
                                dispatchGlfwMouseButton(action, false)
                            } else if (AllSettings.enableMouseClick.state) {
                                dispatchGlfwMouseButton(0, true)
                                dispatchGlfwMouseButton(0, false)
                            }
                        },
                        onCapturedTap = {
                            if (AllSettings.gestureControl.state) {
                                val action = AllSettings.gestureTapMouseAction.state.toAction()
                                dispatchGlfwMouseButton(action, true)
                                dispatchGlfwMouseButton(action, false)
                            } else if (AllSettings.enableMouseClick.state) {
                                dispatchGlfwMouseButton(0, true)
                                dispatchGlfwMouseButton(0, false)
                            }
                        },
                        onLongPress = {
                            if (AllSettings.gestureControl.state) {
                                val action = AllSettings.gestureLongPressMouseAction.state.toAction()
                                dispatchGlfwMouseButton(action, true)
                            } else {
                                dispatchGlfwMouseButton(0, true)
                            }
                        },
                        onLongPressEnd = {
                            if (AllSettings.gestureControl.state) {
                                val action = AllSettings.gestureLongPressMouseAction.state.toAction()
                                dispatchGlfwMouseButton(action, false)
                            } else {
                                dispatchGlfwMouseButton(0, false)
                            }
                        },
                        onCapturedLongPress = {
                            if (AllSettings.gestureControl.state) {
                                val action = AllSettings.gestureLongPressMouseAction.state.toAction()
                                dispatchGlfwMouseButton(action, true)
                            } else {
                                dispatchGlfwMouseButton(0, true)
                            }
                        },
                        onCapturedLongPressEnd = {
                            if (AllSettings.gestureControl.state) {
                                val action = AllSettings.gestureLongPressMouseAction.state.toAction()
                                dispatchGlfwMouseButton(action, false)
                            } else {
                                dispatchGlfwMouseButton(0, false)
                            }
                        },
                        onPointerMove = { pos ->
                            dispatchCursorMove(pos.x, pos.y)
                        },
                        onCapturedMove = { delta ->
                            val dx = delta.x * mouseSpeed
                            val dy = delta.y * mouseSpeed
                            dispatchRelativeMotion(dx, dy)
                            CallbackBridge.sendCursorDelta(dx, dy)
                        },
                        onMouseScroll = { scroll ->
                            dispatchScroll(scroll.x.toDouble(), scroll.y.toDouble())
                        },
                        onMouseButton = { button, pressed ->
                            dispatchGlfwMouseButton(button, pressed)
                        },
                        isMoveOnlyPointer = { moveOnlyPointers.contains(it) },
                        onOccupiedPointer = { occupiedPointers.add(it) },
                        onReleasePointer = {
                            occupiedPointers.remove(it)
                            moveOnlyPointers.remove(it)
                        },
                        enableScrollGesture = AllSettings.gestureControl.state,
                        onScrollGesture = { scroll ->
                            dispatchScroll(scroll.x.toDouble(), scroll.y.toDouble())
                        }
                    )
                } else {
                    // Forward unconsumed Compose pointers as genuine native multi-touch into AInputQueue
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                val activeSlots = LinkedHashMap<PointerId, Pair<Int, Offset>>()
                                val startDownTimes = longArrayOf(0L)
                                fun allocateSlot(): Int {
                                    val used = activeSlots.values.map { it.first }.toSet()
                                    var slot = 0
                                    while (used.contains(slot)) slot++
                                    return slot
                                }
                                fun emitTouchEvent(action: Int, downTime: Long) {
                                    val count = activeSlots.size
                                    if (count == 0) return
                                    val props = Array(count) { MotionEvent.PointerProperties() }
                                    val coords = Array(count) { MotionEvent.PointerCoords() }
                                    var i = 0
                                    for ((_, slotPos) in activeSlots) {
                                        props[i].id = slotPos.first
                                        props[i].toolType = MotionEvent.TOOL_TYPE_FINGER
                                        coords[i].x = slotPos.second.x
                                        coords[i].y = slotPos.second.y
                                        i++
                                    }
                                    val now = SystemClock.uptimeMillis()
                                    val ev = MotionEvent.obtain(
                                        downTime, now, action, count, props, coords,
                                        0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
                                    )
                                    dispatchInputEventToNative(ev)
                                    ev.recycle()
                                }

                                awaitEachGesture {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        val changes = event.changes
                                        var moved = false
                                        for (change in changes) {
                                            val pid = change.id
                                            val isOwnedByUi = change.isConsumed || occupiedPointers.contains(pid)
                                            if (change.pressed) {
                                                if (!isOwnedByUi && !activeSlots.containsKey(pid)) {
                                                    val isFirst = activeSlots.isEmpty()
                                                    if (isFirst) startDownTimes[0] = SystemClock.uptimeMillis()
                                                    val slot = allocateSlot()
                                                    activeSlots[pid] = slot to change.position
                                                    val idx = activeSlots.keys.indexOf(pid)
                                                    val action = if (isFirst) {
                                                        MotionEvent.ACTION_DOWN
                                                    } else {
                                                        MotionEvent.ACTION_POINTER_DOWN or (idx shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                                                    }
                                                    emitTouchEvent(action, startDownTimes[0])
                                                    change.consume()
                                                } else if (activeSlots.containsKey(pid)) {
                                                    val existing = activeSlots[pid]!!
                                                    if (existing.second != change.position) {
                                                        activeSlots[pid] = existing.first to change.position
                                                        moved = true
                                                    }
                                                    change.consume()
                                                }
                                            } else if (activeSlots.containsKey(pid)) {
                                                val existing = activeSlots[pid]!!
                                                activeSlots[pid] = existing.first to change.position
                                                val idx = activeSlots.keys.indexOf(pid)
                                                val action = if (activeSlots.size == 1) {
                                                    MotionEvent.ACTION_UP
                                                } else {
                                                    MotionEvent.ACTION_POINTER_UP or (idx shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                                                }
                                                emitTouchEvent(action, startDownTimes[0])
                                                activeSlots.remove(pid)
                                                moved = false
                                                change.consume()
                                            }
                                        }
                                        if (moved && activeSlots.isNotEmpty()) {
                                            emitTouchEvent(MotionEvent.ACTION_MOVE, startDownTimes[0])
                                        }
                                        if (changes.all { !it.pressed } && activeSlots.isEmpty()) break
                                    }
                                }
                            }
                    )
                }
            }

            val isZl1Layout = remember(activeLayoutName, layoutReloadVersion) {
                activeLayoutName.contains("zalith_1", ignoreCase = true) ||
                activeLayoutName.contains("zl1", ignoreCase = true) ||
                activeLayoutName.equals("default.json", ignoreCase = true) ||
                ControlManager.isZl1LayoutFile(layoutFile)
            }

            // 1. Controls layer: ZL1 ControlLayout OR Pocket Edition / Zalith 2 ControlBoxLayout / ControlEditor layer
            if (isZl1Layout) {
                // Attach the authentic ZL1 floating draggable gear icon with remembered position
                DisposableEffect(Unit) {
                    val wrapper = GameMenuViewWrapper(
                        this@HorizonActivity,
                        {
                            drawerLayoutRef?.let { drawer ->
                                val nav = drawer.getChildAt(1)
                                if (nav != null) {
                                    if (drawer.isDrawerOpen(nav)) drawer.closeDrawer(nav)
                                    else drawer.openDrawer(nav)
                                }
                            }
                        },
                        showInfo = true
                    )
                    wrapper.setVisibility(true)
                    onDispose {
                        wrapper.release()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    mouseOrTouchLayer()
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val drawerLayout = DrawerLayout(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                                addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                                    override fun onDrawerOpened(drawerView: View) {
                                        isDrawerOpen = true
                                    }
                                    override fun onDrawerClosed(drawerView: View) {
                                        isDrawerOpen = false
                                    }
                                })
                            }

                            val contentFrame = FrameLayout(ctx).apply {
                                layoutParams = DrawerLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }

                            val zl1Layout = net.kdt.pojavlaunch.customcontrols.ControlLayout(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                runCatching {
                                    if (layoutFile.exists()) {
                                        loadLayout(layoutFile.absolutePath)
                                    } else {
                                        val defFile = File(PathManager.DIR_CONTROL_LAYOUTS, "zalith_1_layout.json")
                                        if (!defFile.exists()) {
                                            ctx.copyAssetFile("zalith_1_layout.json", defFile, false)
                                        }
                                        if (defFile.exists()) {
                                            loadLayout(defFile.absolutePath)
                                        }
                                    }
                                }
                                setMenuListener {
                                    val nav = drawerLayout.getChildAt(1)
                                    if (nav != null) {
                                        if (drawerLayout.isDrawerOpen(nav)) drawerLayout.closeDrawer(nav)
                                        else drawerLayout.openDrawer(nav)
                                    }
                                }
                            }
                            contentFrame.addView(zl1Layout)

                            val hotbarView = HotbarView(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                                )
                            }
                            contentFrame.addView(hotbarView)
                            drawerLayout.addView(contentFrame)

                            val navContainer = FrameLayout(ctx).apply {
                                layoutParams = DrawerLayout.LayoutParams(
                                    Tools.dpToPx(320f).toInt(),
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                ).apply {
                                    gravity = GravityCompat.END
                                }
                            }

                            val menuBinding = ViewGameMenuBinding.inflate(layoutInflater, navContainer, true)
                            setupDrawerMenuBinding(menuBinding, drawerLayout, navContainer, layoutFile)

                            drawerLayout.addView(navContainer)
                            drawerLayoutRef = drawerLayout
                            drawerLayout
                        },
                        update = { drawer ->
                            val contentFrame = drawer.getChildAt(0) as? FrameLayout
                            val zl1Layout = contentFrame?.getChildAt(0) as? net.kdt.pojavlaunch.customcontrols.ControlLayout
                            zl1Layout?.alpha = controlsOpacity
                        }
                    )
                }
            } else {
                val parsedLayout: ControlLayout? = remember(activeLayoutName, layoutReloadVersion) {
                    runCatching {
                        if (layoutFile.exists()) loadLayoutFromFile(layoutFile)
                        else null
                    }.getOrNull() ?: runCatching {
                        val assetName = if (isPocket) "pocket_edition_layout.json" else "zalith_2_layout.json"
                        assets.open(assetName).use { input ->
                            loadLayoutFromString(input.bufferedReader().use { it.readText() })
                        }
                    }.getOrNull() ?: runCatching {
                        assets.open("zalith_2_layout.json").use { input ->
                            loadLayoutFromString(input.bufferedReader().use { it.readText() })
                        }
                    }.getOrNull()
                }

                if (isEditingControls && parsedLayout != null) {
                    LaunchedEffect(parsedLayout) {
                        editorViewModel.initLayout(parsedLayout)
                    }
                    ControlEditor(
                        viewModel = editorViewModel,
                        targetFile = layoutFile,
                        exit = {
                            isEditingControls = false
                            layoutReloadVersion++
                        },
                        menuExit = {
                            editorViewModel.showExitEditorDialog(
                                context = this@HorizonActivity,
                                targetFile = layoutFile,
                                onExit = {
                                    isEditingControls = false
                                    layoutReloadVersion++
                                }
                            )
                        }
                    )
                } else {
                    if (parsedLayout != null) {
                        val observable = remember(parsedLayout) { ObservableControlLayout(parsedLayout) }
                        ControlBoxLayout(
                            modifier = Modifier.fillMaxSize(),
                            observedLayout = observable,
                            eventHandler = eventHandler,
                            checkOccupiedPointers = { occupiedPointers.contains(it) },
                            opacity = controlsOpacity,
                            buttonScale = (runCatching { AllSettings.controlsButtonScale.state.toFloat() / 100f }.getOrDefault(1f)).coerceIn(0.5f, 2.0f),
                            markPointerAsMoveOnly = { moveOnlyPointers.add(it) },
                            onOccupiedPointer = { occupiedPointers.add(it) },
                            onReleasePointer = { occupiedPointers.remove(it) },
                            isCursorGrabbing = isUiGameplayGrabbing,
                            hideLayerWhen = HideLayerWhen.None,
                            isDark = true
                        ) {
                            mouseOrTouchLayer()
                        }
                    } else {
                        mouseOrTouchLayer()
                    }

                    // ZL2 / Pocket Floating Menu Ball (FPS & Memory displayed inside DraggableGameBall)
                    DraggableGameBall(
                        position = ballPosSetting.state,
                        onPositionChanged = { ballPosSetting.updateState(it) },
                        onSavePos = { ballPosSetting.save() },
                        gameFps = gameFps,
                        showMemory = AllSettings.showMemory.state,
                        opened = isMenuOpen,
                        isPocketEdition = isPocket,
                        alpha = (AllSettings.menuBallOpacity.state / 100f).coerceIn(0.1f, 1f),
                        onClick = { isMenuOpen = !isMenuOpen }
                    )
                }
            }

            // Minecraft Hotbar trigger box (enabled in Virtual Mouse for ZL2 / Pocket Edition)
            if (!isZl1Layout && isVirtualMouseMode && !isEditingControls) {
                val gameDisplayLayout = currentGameDisplayLayout(screenSize)
                MinecraftHotbar(
                    screenSize = screenSize,
                    rule = AllSettings.hotbarRule.state,
                    widthPercentage = AllSettings.hotbarWidth.state.hotbarPercentage(),
                    heightPercentage = AllSettings.hotbarHeight.state.hotbarPercentage(),
                    sendKeycode = { keycode -> dispatchGlfwKeyTap(keycode) },
                    isGrabbing = isUiGameplayGrabbing,
                    displayOffset = gameDisplayLayout.offset,
                    onOccupiedPointer = { occupiedPointers.add(it) },
                    onReleasePointer = { occupiedPointers.remove(it) }
                )
            }

            // Authentic Zalith Launcher 2 In-Game Menu (GameMenuSubscreen - used for ZL2 & Pocket Edition)
            if (!isZl1Layout) {
                GameMenuSubscreen(
                    state = if (isMenuOpen) MenuState.SHOW else MenuState.HIDE,
                    controlMenuTabIndex = controlMenuTabIndex,
                    onControlMenuTabChange = { controlMenuTabIndex = it },
                    gamepadViewModel = gamepadViewModel,
                    closeScreen = { isMenuOpen = false },
                    onForceClose = {
                        forceCloseState = ForceCloseOperation.Show
                    },
                    onSwitchLog = {
                        showLogs = !showLogs
                    },
                    enableTerracotta = false,
                    onOpenTerracottaMenu = {},
                    onRefreshWindowSize = { refreshGameResolution() },
                    onInputMethod = {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        @Suppress("DEPRECATION")
                        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    },
                    onSendKeycode = {
                        sendKeycodeState = SendKeycodeState.ShowDialog
                    },
                    onReplacementControl = {
                        replacementControlState = ReplacementControlState.Show
                    },
                    onEditLayout = {
                        isEditingControls = true
                    },
                    onShowToast = { text, duration ->
                        Toast.makeText(this@HorizonActivity, text.toAndroidString(this@HorizonActivity), duration).show()
                    }
                )
            }

            // Gamepad hardware capture + mode selection prompt
            if (AllSettings.gamepadControl.state) {
                SimpleGamepadCapture(gamepadViewModel = gamepadViewModel)
            }
            GamepadModePromptDialog(
                visible = gamepadViewModel.modePromptVisible,
                onConfirm = { mode -> gamepadViewModel.confirmModePrompt(mode) }
            )

            // Force close confirmation dialog
            ForceCloseOperation(
                operation = forceCloseState,
                onChange = { forceCloseState = it },
                onForceClose = {
                    isMenuOpen = false
                    finishAffinity()
                    android.os.Process.killProcess(android.os.Process.myPid())
                },
                text = stringResource(R.string.game_dialog_force_close_message)
            )

            // Layout switcher dialog
            ReplacementControlOperation(
                operation = replacementControlState,
                onChange = { replacementControlState = it },
                currentLayout = layoutFile,
                replacementControl = { file ->
                    activeLayoutName = file.name
                    runCatching { AllSettings.controlLayout.save(file.name) }
                    layoutReloadVersion++
                    replacementControlState = ReplacementControlState.None
                }
            )

            // Send Keycode Operation Dialog routed straight to native GLFW -> Android key pipeline
            if (sendKeycodeState == SendKeycodeState.ShowDialog) {
                val pressedKeys = remember { mutableStateListOf<String>() }
                Keyboard(
                    onDismissRequest = { sendKeycodeState = SendKeycodeState.None },
                    onSwitch = { keyString, pressed ->
                        if (pressed) pressedKeys.add(keyString) else pressedKeys.remove(keyString)
                        ControlEventKeycode.getKeycodeFromEvent(keyString)?.toInt()?.let { glfw ->
                            if (keyString.startsWith("GLFW_MOUSE_", false)) {
                                dispatchGlfwMouseButton(glfw, pressed)
                            } else {
                                CallbackBridge.setModifiers(glfw, pressed)
                                dispatchGlfwKey(glfw, pressed)
                            }
                        }
                    }
                )
                DisposableEffect(Unit) {
                    onDispose {
                        pressedKeys.forEach { keyString ->
                            ControlEventKeycode.getKeycodeFromEvent(keyString)?.toInt()?.let { glfw ->
                                if (keyString.startsWith("GLFW_MOUSE_", false)) {
                                    dispatchGlfwMouseButton(glfw, false)
                                } else {
                                    CallbackBridge.setModifiers(glfw, false)
                                    dispatchGlfwKey(glfw, false)
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Log Viewer
            if (showLogs) {
                LogBox(
                    enableLog = true,
                    onClose = { showLogs = false },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Floating Screen Capture / Recording Tools HUD
            if (AllSettings.hudScreenCaptureTools.state && !showSplash && !isModalOpen && !isEditingControls) {
                com.movtery.zalithlauncher.ui.screens.game.elements.InGameCaptureHud(
                    modifier = Modifier.align(Alignment.TopStart),
                    alpha = controlsOpacity
                )
            }

            // Splash Screen Overlay
            AnimatedVisibility(
                visible = showSplash,
                exit = fadeOut(animationSpec = tween(durationMillis = 600)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Cubiq Launcher",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Minecraft Java rewritten into Rust",
                            color = Color(0xFF4CAF50),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        MinecraftPixelDonutSpinner(
                            size = 36.dp,
                            baseColor = Color(0xFF55FF55)
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        super.onResume()
        net.kdt.pojavlaunch.Tools.updateWindowSize(this)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        attachOverlayIfNeeded()
    }

    override fun onPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        super.onPause()
    }

    override fun onStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onStop()
    }

    override fun onDestroy() {
        if (isOverlayAttached && overlayContainer != null) {
            try {
                windowManagerRef?.removeViewImmediate(overlayContainer)
            } catch (_: Throwable) {}
            overlayContainer = null
            isOverlayAttached = false
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        appViewModelStore.clear()
        CallbackBridge.setNativeInputListener(null)
        super.onDestroy()
        Process.killProcess(Process.myPid())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedStateRegistryController.performSave(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (backDispatcher.hasEnabledCallbacks()) {
            backDispatcher.onBackPressed()
            return
        }
        val now = SystemClock.uptimeMillis()
        val down = KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE, 0)
        val up = KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE, 0)
        dispatchInputEventToNative(down)
        dispatchInputEventToNative(up)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        if (!activityResultRegistryImpl.dispatchResult(request, result, data)) {
            super.onActivityResult(request, result, data)
        }
        if (request == 4101) {
            nativeDocumentResult(request, result, data?.data?.toString())
        }
    }
}
