package dev.horizon.android

import android.app.NativeActivity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
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
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.control.event.LAUNCHER_EVENT_SWITCH_MENU
import com.movtery.zalithlauncher.ui.screens.game.elements.DraggableGameBall
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import git.artdeell.mojo.input.LayoutConverter
import kotlinx.coroutines.delay
import org.lwjgl.glfw.CallbackBridge
import java.io.File

/**
 * Native host activity for Horizon (Minecraft Rust Edition).
 * Directly hosts Compose UI (ZL2 ControlBoxLayout, ZL2 DraggableGameBall menu, and Splash Screen)
 * over the NativeActivity surface via WindowManager TYPE_APPLICATION_PANEL to guarantee
 * hardware-accelerated rendering on top of the native Vulkan/EGL surface.
 */
open class HorizonActivity : NativeActivity(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner, OnBackPressedDispatcherOwner {

    companion object {
        private const val TAG = "HorizonActivity"

        init {
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

    inner class PassthroughOverlayLayout(context: Context) : FrameLayout(context) {
        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            // First let Compose UI (buttons, joystick, floating menu ball) handle the event
            val handled = super.dispatchTouchEvent(ev)
            if (!handled) {
                // If touch fell on empty / transparent space, pass it directly to the underlying
                // NativeActivity window so the Rust engine can perform camera look / drag / interaction
                return this@HorizonActivity.dispatchToNative(ev)
            }
            return true
        }
    }

    fun dispatchToNative(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
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
            val ballPosSetting = if (isPocket) AllSettings.menuBallPosPocket else AllSettings.menuBallPos
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

            // 3. In-Game Menu Dialog (when 3-line menu ball is clicked)
            if (isMenuOpen) {
                Dialog(onDismissRequest = { isMenuOpen = false }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E202E),
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Game Menu",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                TextButton(onClick = { isMenuOpen = false }) {
                                    Text("✕", fontSize = 18.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Switch layout options
                            Text(
                                text = "Control Layout:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { activeLayoutName = "zalith_2_layout.json" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeLayoutName == "zalith_2_layout.json") MaterialTheme.colorScheme.primary else Color(0xFF2C2F40)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("ZL2 Layout", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { activeLayoutName = "pocket_edition_layout.json" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeLayoutName == "pocket_edition_layout.json") MaterialTheme.colorScheme.primary else Color(0xFF2C2F40)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Pocket Edition", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Opacity slider
                            Text(
                                text = "Button Opacity: ${(controlsOpacity * 100).toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.LightGray
                            )
                            Slider(
                                value = controlsOpacity,
                                onValueChange = { controlsOpacity = it },
                                valueRange = 0.1f..1.0f
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick function buttons
                            Text(
                                text = "Quick Keys:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "F3" to KeyEvent.KEYCODE_F3,
                                    "F5" to KeyEvent.KEYCODE_F5,
                                    "Chat" to KeyEvent.KEYCODE_T,
                                    "Esc" to KeyEvent.KEYCODE_ESCAPE,
                                    "Inv" to KeyEvent.KEYCODE_E
                                ).forEach { (label, keycode) ->
                                    Button(
                                        onClick = {
                                            val now = SystemClock.uptimeMillis()
                                            dispatchKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keycode, 0))
                                            dispatchKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keycode, 0))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2F40)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(label, fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Bottom action buttons: Return to Game & Quit Game
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { isMenuOpen = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Resume", color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        isMenuOpen = false
                                        finish()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Quit Game", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

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
