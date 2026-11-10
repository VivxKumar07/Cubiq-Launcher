package dev.horizon.android

import android.app.NativeActivity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Region
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.*
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
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SWITCH_MENU
import com.movtery.zalithlauncher.ui.screens.game.elements.DraggableGameBall
import com.movtery.zalithlauncher.ui.screens.game.elements.ForceCloseOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.GameMenuSubscreen
import com.movtery.zalithlauncher.ui.screens.game.elements.ReplacementControlOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.ReplacementControlState
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.toAndroidString
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import git.artdeell.mojo.input.LayoutConverter
import kotlinx.coroutines.delay
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.lang.reflect.Proxy

/**
 * Native host activity for Horizon (Minecraft Rust Edition).
 * Directly hosts Compose UI (ZL2 ControlBoxLayout, authentic ZL2 GameMenuSubscreen, and DraggableGameBall)
 * over the NativeActivity surface via WindowManager TYPE_APPLICATION_PANEL with OnComputeInternalInsetsListener
 * so touch events outside the buttons seamlessly pass straight through to the underlying Rust game engine.
 */
open class HorizonActivity : NativeActivity(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner, OnBackPressedDispatcherOwner {

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

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val onBackPressedDispatcher: OnBackPressedDispatcher get() = backDispatcher

    private var isOverlayAttached = false
    private var overlayContainer: PassthroughOverlayLayout? = null
    private var windowManagerRef: WindowManager? = null

    /**
     * Overlay container that dynamically restricts its touchable window region
     * to only the on-screen buttons and menu ball.
     * All touches outside this region pass through to the underlying NativeActivity window.
     */
    inner class PassthroughOverlayLayout(context: Context) : FrameLayout(context) {
        private val touchableRegion = Region()

        fun updateTouchableRegions(rects: List<Rect>, isModal: Boolean) {
            val w = width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
            val h = height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels

            if (isModal) {
                touchableRegion.set(0, 0, w, h)
            } else {
                touchableRegion.setEmpty()
                for (r in rects) {
                    touchableRegion.op(r, Region.Op.UNION)
                }
            }
            viewTreeObserver.dispatchOnGlobalLayout()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            setupInsetsListener()
        }

        private fun setupInsetsListener() {
            try {
                val listenerClass = Class.forName("android.view.ViewTreeObserver\$OnComputeInternalInsetsListener")
                val addMethod = ViewTreeObserver::class.java.getMethod("addOnComputeInternalInsetsListener", listenerClass)
                val infoClass = Class.forName("android.view.ViewTreeObserver\$InternalInsetsInfo")
                val setTouchableInsetsMethod = infoClass.getMethod("setTouchableInsets", Int::class.javaPrimitiveType)
                val touchableRegionField = infoClass.getField("touchableRegion")
                val touchableInsetsRegionConstant = infoClass.getField("TOUCHABLE_INSETS_REGION").getInt(null)

                val listener = Proxy.newProxyInstance(
                    listenerClass.classLoader,
                    arrayOf(listenerClass)
                ) { _, method, args ->
                    if (method?.name == "onComputeInternalInsets" && args != null && args.isNotEmpty()) {
                        val inoutInfo = args[0]
                        setTouchableInsetsMethod.invoke(inoutInfo, touchableInsetsRegionConstant)
                        val region = touchableRegionField.get(inoutInfo) as Region
                        region.set(touchableRegion)
                    }
                    null
                }
                addMethod.invoke(viewTreeObserver, listener)
                Log.i(TAG, "OnComputeInternalInsetsListener attached successfully")
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to register OnComputeInternalInsetsListener: ${e.message}", e)
            }
        }
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

        setupInputBridge()

        window.decorView.post {
            attachOverlayIfNeeded()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachOverlayIfNeeded()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
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
                    ZalithLauncherTheme {
                        HorizonInGameScreen()
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
            }

            wm.addView(container, params)
            overlayContainer = container
            isOverlayAttached = true
            Log.i(TAG, "Successfully attached Compose overlay via WindowManager TYPE_APPLICATION_PANEL")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to attach overlay: ${e.message}", e)
        }
    }

    private fun setupInputBridge() {
        CallbackBridge.setNativeInputListener(object : CallbackBridge.NativeInputListener {
            override fun onKey(glfwKey: Int, keyChar: Char, scancode: Int, modifiers: Int, status: Boolean) {
                var androidKeycode = LayoutConverter.keyCodeFromGLFW(glfwKey)
                if (androidKeycode == KeyEvent.KEYCODE_UNKNOWN) {
                    androidKeycode = glfwKey
                }
                if (androidKeycode > 0) {
                    val now = SystemClock.uptimeMillis()
                    val keyEvent = KeyEvent(now, now, if (status) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP, androidKeycode, 0)
                    this@HorizonActivity.dispatchKeyEvent(keyEvent)
                }
            }

            override fun onMouseButton(glfwButton: Int, status: Boolean) {
                val now = SystemClock.uptimeMillis()
                val buttonState = if (status) {
                    when (glfwButton) {
                        0, MotionEvent.BUTTON_PRIMARY -> MotionEvent.BUTTON_PRIMARY
                        1, MotionEvent.BUTTON_SECONDARY -> MotionEvent.BUTTON_SECONDARY
                        else -> MotionEvent.BUTTON_TERTIARY
                    }
                } else 0

                val w = window.decorView.width
                val h = window.decorView.height
                val x = if (w > 0) w / 2f else 500f
                val y = if (h > 0) h / 2f else 500f

                val pp = arrayOf(MotionEvent.PointerProperties().apply {
                    id = 0
                    toolType = MotionEvent.TOOL_TYPE_MOUSE
                })
                val pc = arrayOf(MotionEvent.PointerCoords().apply {
                    this.x = x
                    this.y = y
                })
                val action = if (status) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_UP
                val ev = MotionEvent.obtain(
                    now, now, action, 1, pp, pc, 0, buttonState, 1.0f, 1.0f, 0, 0,
                    InputDevice.SOURCE_MOUSE, 0
                )
                try {
                    this@HorizonActivity.dispatchTouchEvent(ev)
                    this@HorizonActivity.dispatchGenericMotionEvent(ev)
                } catch (_: Throwable) {}
                ev.recycle()
            }

            override fun onScroll(xoffset: Double, yoffset: Double) {}
            override fun onCursor(x: Float, y: Float) {}
        })
    }

    @Composable
    private fun HorizonInGameScreen() {
        var showSplash by remember { mutableStateOf(true) }
        var isMenuOpen by remember { mutableStateOf(false) }
        var controlMenuTabIndex by remember { mutableIntStateOf(0) }
        var forceCloseState by remember { mutableStateOf<ForceCloseOperation>(ForceCloseOperation.None) }
        var replacementControlState by remember { mutableStateOf<ReplacementControlState>(ReplacementControlState.None) }

        val gamepadViewModel = remember {
            ViewModelProvider(this@HorizonActivity)[GamepadViewModel::class.java]
        }

        // Start splash fade timer
        LaunchedEffect(Unit) {
            delay(3200)
            showSplash = false
        }

        // Active layout setting - defaults to authentic ZL2 layout
        var activeLayoutName by remember {
            val initial = intent.getStringExtra("EXTRA_CONTROL_LAYOUT_NAME")
                ?: runCatching { AllSettings.controlLayout.getValue() }.getOrNull()
                ?: "zalith_2_layout.json"
            mutableStateOf(if (initial.isNotBlank() && (initial.contains("zalith_2") || initial.contains("pocket"))) initial else "zalith_2_layout.json")
        }

        var controlsOpacity by remember {
            mutableFloatStateOf((runCatching { AllSettings.controlsOpacity.state.toFloat() / 100f }.getOrDefault(0.8f)).coerceIn(0.1f, 1f))
        }

        val isPocket = activeLayoutName.contains("pocket", ignoreCase = true)
        val isMojo = activeLayoutName.contains("mojo", ignoreCase = true)
        val isZl1 = activeLayoutName.contains("zalith_1", ignoreCase = true) || activeLayoutName.contains("zl1", ignoreCase = true)
        val isLegacy = isMojo || isZl1

        val moveOnlyPointers = remember { mutableSetOf<PointerId>() }
        val occupiedPointers = remember { mutableSetOf<PointerId>() }

        val ballPosSetting = if (isPocket) AllSettings.menuBallPosPocket else AllSettings.menuBallPos

        // Dynamically update the touchable region of the overlay window so touches
        // on empty areas (title screen buttons, crosshair camera drag) pass through to Rust
        val density = LocalDensity.current.density
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        LaunchedEffect(isMenuOpen, activeLayoutName, ballPosSetting.state, forceCloseState, replacementControlState) {
            val rects = mutableListOf<Rect>()
            val isModal = isMenuOpen || forceCloseState == ForceCloseOperation.Show || replacementControlState == ReplacementControlState.Show
            if (isModal) {
                overlayContainer?.updateTouchableRegions(emptyList(), isModal = true)
            } else {
                // 1. Draggable Game Ball
                val ballX = (ballPosSetting.state.x * density).toInt()
                val ballY = (ballPosSetting.state.y * density).toInt()
                val ballSize = (56 * density).toInt()
                rects.add(Rect(ballX - 10, ballY - 10, ballX + ballSize + 10, ballY + ballSize + 10))

                // 2. Control clusters:
                // Left cluster (D-pad / Joystick):
                rects.add(Rect(0, (screenHeight * 0.40f).toInt(), (screenWidth * 0.36f).toInt(), screenHeight))

                // Right cluster (Action buttons, Jump, Sneak, Attack):
                rects.add(Rect((screenWidth * 0.64f).toInt(), (screenHeight * 0.40f).toInt(), screenWidth, screenHeight))

                // Top cluster (Keyboard, Pause, etc.):
                rects.add(Rect((screenWidth * 0.30f).toInt(), 0, (screenWidth * 0.70f).toInt(), (screenHeight * 0.16f).toInt()))

                overlayContainer?.updateTouchableRegions(rects, isModal = false)
            }
        }

        val eventHandler = remember {
            EventHandler { event: ClickEvent, pressed: Boolean ->
                when (event.type) {
                    ClickEvent.Type.Key -> {
                        val keycode = ControlEventKeycode.getKeycodeFromEvent(event.key)?.toInt() ?: return@EventHandler
                        if (event.key.startsWith("GLFW_MOUSE_", false)) {
                            CallbackBridge.sendMouseButton(keycode, pressed)
                        } else {
                            CallbackBridge.sendKeyPress(keycode, CallbackBridge.getCurrentMods(), pressed)
                            CallbackBridge.setModifiers(keycode, pressed)
                        }
                    }
                    ClickEvent.Type.LauncherEvent -> {
                        when (event.key) {
                            LAUNCHER_EVENT_SWITCH_MENU -> {
                                if (pressed) isMenuOpen = !isMenuOpen
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Controls layer
            if (isLegacy) {
                // Legacy Mojo / ZL1 layout view
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(controlsOpacity),
                    factory = { ctx ->
                        git.artdeell.mojo.Tools.init(ctx)
                        git.artdeell.mojo.input.ControlLayout(ctx).apply {
                            setModifiable(false)
                            background = null
                            post {
                                runCatching {
                                    val f = File(PathManager.DIR_CONTROL_LAYOUTS, activeLayoutName)
                                    if (f.exists()) {
                                        loadLayout(f.absolutePath)
                                    } else {
                                        val fallbackFile = File(filesDir, "horizon_$activeLayoutName")
                                        assets.open(activeLayoutName).use { input ->
                                            fallbackFile.outputStream().use { output -> input.copyTo(output) }
                                        }
                                        loadLayout(fallbackFile.absolutePath)
                                    }
                                }
                            }
                        }
                    }
                )
            } else {
                // Pocket Edition & Zalith 2 ControlBoxLayout layer
                val currentFile = File(PathManager.DIR_CONTROL_LAYOUTS, activeLayoutName)
                val parsedLayout: ControlLayout? = remember(activeLayoutName) {
                    runCatching {
                        if (currentFile.exists()) loadLayoutFromFile(currentFile)
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
                        isCursorGrabbing = false,
                        hideLayerWhen = HideLayerWhen.None,
                        isDark = true
                    ) {}
                }
            }

            // 2. The Iconic ZL2 3-Horizontal-Line Floating Menu Ball!
            DraggableGameBall(
                position = ballPosSetting.state,
                onPositionChanged = { ballPosSetting.updateState(it) },
                onSavePos = { ballPosSetting.save() },
                gameFps = null,
                showMemory = false,
                opened = isMenuOpen,
                isPocketEdition = isPocket,
                alpha = (AllSettings.menuBallOpacity.state / 100f).coerceIn(0.1f, 1f),
                onClick = { isMenuOpen = !isMenuOpen }
            )

            // 3. Authentic Zalith Launcher 2 In-Game Menu (GameMenuSubscreen)
            GameMenuSubscreen(
                state = if (isMenuOpen) MenuState.SHOW else MenuState.HIDE,
                controlMenuTabIndex = controlMenuTabIndex,
                onControlMenuTabChange = { controlMenuTabIndex = it },
                gamepadViewModel = gamepadViewModel,
                closeScreen = { isMenuOpen = false },
                onForceClose = {
                    forceCloseState = ForceCloseOperation.Show
                },
                onSwitchLog = {},
                enableTerracotta = false,
                onOpenTerracottaMenu = {},
                onRefreshWindowSize = {},
                onInputMethod = {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                },
                onSendKeycode = {},
                onReplacementControl = {
                    replacementControlState = ReplacementControlState.Show
                },
                onEditLayout = {},
                onShowToast = { text, duration ->
                    Toast.makeText(this@HorizonActivity, text.toAndroidString(this@HorizonActivity), duration).show()
                }
            )

            // Force close confirmation dialog
            ForceCloseOperation(
                operation = forceCloseState,
                onChange = { forceCloseState = it },
                onForceClose = {
                    isMenuOpen = false
                    finish()
                },
                text = stringResource(R.string.game_dialog_force_close_message)
            )

            // Layout switcher dialog
            ReplacementControlOperation(
                operation = replacementControlState,
                onChange = { replacementControlState = it },
                currentLayout = File(PathManager.DIR_CONTROL_LAYOUTS, activeLayoutName),
                replacementControl = { file ->
                    activeLayoutName = file.name
                    runCatching { AllSettings.controlLayout.save(file.name) }
                    replacementControlState = ReplacementControlState.None
                }
            )

            // 4. Clean Branded Splash Screen Overlay
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
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Minecraft Java rewritten into Rust",
                            color = Color(0xFF4CAF50),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(
                            color = Color(0xFF4CAF50)
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
        val now = SystemClock.uptimeMillis()
        dispatchKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE, 0))
        dispatchKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE, 0))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        if (request != 4101) return
        nativeDocumentResult(request, result, data?.data?.toString())
    }
}
