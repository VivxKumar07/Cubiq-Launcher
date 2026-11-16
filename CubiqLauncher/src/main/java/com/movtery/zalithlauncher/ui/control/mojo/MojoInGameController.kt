package com.movtery.zalithlauncher.ui.control.mojo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.dialog.KeyboardDialog
import com.movtery.zalithlauncher.ui.dialog.TipDialog
import com.movtery.zalithlauncher.ui.subassembly.view.GameMenuViewWrapper
import git.artdeell.mojo.input.ControlButtonMenuListener
import git.artdeell.mojo.input.ControlData
import git.artdeell.mojo.input.ControlDrawerData
import git.artdeell.mojo.input.ControlJoystickData
import git.artdeell.mojo.input.ControlLayout
import git.artdeell.mojo.input.EditorExitable
import git.artdeell.mojo.prefs.QuickSettingSideDialog
import kotlinx.coroutines.CoroutineScope
import net.kdt.pojavlaunch.MinecraftGLSurface
import net.kdt.pojavlaunch.Tools
import java.io.File
import java.lang.ref.WeakReference

/**
 * Controller host for authentic Mojo Launcher Controls:
 * Hosts git.artdeell.mojo.input.ControlLayout, authentic sliding drawer menu,
 * Quick Settings side sheet, in-game custom controls editor, and gear menu.
 */
class MojoInGameController(
    private val activity: Activity,
    private val lifecycleScope: CoroutineScope,
    private val useTouchControllerProxy: Boolean,
    useSurfaceView: Boolean,
    surfaceCallback: SurfaceHolder.Callback,
    textureListener: TextureView.SurfaceTextureListener,
    insetsListener: View.OnApplyWindowInsetsListener,
    renderViewCreatedListener: MinecraftGLSurface.RenderViewCreatedListener,
    private val onToggleLog: () -> Unit,
    private val onOpenCubiqFriends: () -> Unit = {}
) : ControlButtonMenuListener, EditorExitable {

    companion object {
        @Volatile
        private var activeController: WeakReference<MojoInGameController>? = null

        @JvmStatic
        fun toggleKeyboard(): Boolean {
            val controller = activeController?.get() ?: return false
            controller.switchKeyboardState()
            return true
        }

        @JvmStatic
        fun toggleMouse(): Boolean {
            val controller = activeController?.get() ?: return false
            controller.toggleMouse()
            return true
        }
    }

    val rootView: FrameLayout = FrameLayout(activity)
    val drawerLayout: DrawerLayout = DrawerLayout(activity)
    val controlLayout: ControlLayout = ControlLayout(activity)
    private val keyboardDialog: KeyboardDialog = KeyboardDialog(activity).setShowSpecialButtons(false)

    private val navDrawer: ListView = ListView(activity)
    private lateinit var gameActionArrayAdapter: ArrayAdapter<String>
    private lateinit var editorActionArrayAdapter: ArrayAdapter<String>
    private var quickSettingSideDialog: QuickSettingSideDialog? = null

    private val gameMenuWrapper: GameMenuViewWrapper

    var isInEditor: Boolean = false
        private set

    init {
        activeController = WeakReference(this)

        drawerLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Game container: Render Surface + ControlLayout
        val gameContainer = FrameLayout(activity).apply {
            layoutParams = DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val renderView: View = if (useSurfaceView) {
            android.view.SurfaceView(activity).apply {
                holder.addCallback(surfaceCallback)
            }
        } else {
            TextureView(activity).apply {
                surfaceTextureListener = textureListener
            }
        }
        renderView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        gameContainer.addView(renderView)
        renderViewCreatedListener.onRenderViewCreated(renderView, useSurfaceView)

        // Control Layout
        controlLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        controlLayout.setMenuListener(this)
        gameContainer.addView(controlLayout)

        drawerLayout.addView(gameContainer)

        // Setup authentic right-side drawer ListView matching Mojo
        val drawerWidthPx = Tools.dpToPx(240f).toInt()
        navDrawer.apply {
            layoutParams = DrawerLayout.LayoutParams(drawerWidthPx, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                gravity = GravityCompat.END
            }
            setBackgroundColor(Color.parseColor("#EE16161C"))
            divider = ColorDrawable(Color.parseColor("#33FFFFFF"))
            dividerHeight = Tools.dpToPx(1f).toInt()
            setPadding(0, Tools.dpToPx(24f).toInt(), 0, Tools.dpToPx(24f).toInt())
            clipToPadding = false
        }
        drawerLayout.addView(navDrawer)

        rootView.addView(drawerLayout)
        rootView.setOnApplyWindowInsetsListener(insetsListener)

        // Floating gear button wrapper (remembered position)
        gameMenuWrapper = GameMenuViewWrapper(activity, {
            drawerLayout.openDrawer(navDrawer)
        }, showInfo = true)
        gameMenuWrapper.setVisibility(true)

        setupSideDialogs()
        setupDrawerMenus()
        loadActiveMojoLayout()
    }

    private fun setupSideDialogs() {
        quickSettingSideDialog = object : QuickSettingSideDialog(activity, controlLayout) {
            override fun onResolutionChanged() {}
            override fun onGyroStateChanged() {}
            override fun onButtonTransparencyChanged() {
                controlLayout.invalidate()
            }
        }
    }

    private fun setupDrawerMenus() {
        val inGameItems = activity.resources.getStringArray(R.array.menu_ingame)
        val editorItems = activity.resources.getStringArray(R.array.menu_customcontrol)

        gameActionArrayAdapter = object : ArrayAdapter<String>(
            activity,
            android.R.layout.simple_list_item_1,
            inGameItems
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                view.textSize = 15f
                view.setPadding(
                    Tools.dpToPx(20f).toInt(),
                    Tools.dpToPx(16f).toInt(),
                    Tools.dpToPx(20f).toInt(),
                    Tools.dpToPx(16f).toInt()
                )
                return view
            }
        }

        editorActionArrayAdapter = object : ArrayAdapter<String>(
            activity,
            android.R.layout.simple_list_item_1,
            editorItems
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#E0E0FF"))
                view.textSize = 15f
                view.setPadding(
                    Tools.dpToPx(20f).toInt(),
                    Tools.dpToPx(16f).toInt(),
                    Tools.dpToPx(20f).toInt(),
                    Tools.dpToPx(16f).toInt()
                )
                return view
            }
        }

        navDrawer.adapter = gameActionArrayAdapter
        navDrawer.setOnItemClickListener { _, _, position, _ ->
            if (isInEditor) {
                handleEditorAction(position)
            } else {
                handleInGameAction(position)
            }
        }
    }

    private fun handleInGameAction(position: Int) {
        drawerLayout.closeDrawer(navDrawer)
        when (position) {
            0 -> {
                // Force close
                TipDialog.Builder(activity)
                    .setTitle(R.string.control_forceclose)
                    .setMessage(R.string.game_dialog_force_close_message)
                    .setConfirmClickListener {
                        activity.finishAffinity()
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                    .showDialog()
            }
            1 -> {
                // Log output
                onToggleLog()
            }
            2 -> {
                // Send custom keycode
                keyboardDialog.show()
            }
            3 -> {
                // Quick settings
                quickSettingSideDialog?.appear(true)
            }
            4 -> {
                // Custom controls
                enterEditorMode()
            }
        }
    }

    private fun enterEditorMode() {
        isInEditor = true
        controlLayout.setModifiable(true)
        navDrawer.adapter = editorActionArrayAdapter
        gameMenuWrapper.setVisibility(true)
        Toast.makeText(activity, R.string.hint_control_mapping, Toast.LENGTH_LONG).show()
    }

    private fun handleEditorAction(position: Int) {
        when (position) {
            0 -> {
                // Add button
                controlLayout.addControlButton(ControlData("New"))
                drawerLayout.closeDrawer(navDrawer)
            }
            1 -> {
                // Add button drawer
                controlLayout.addDrawer(ControlDrawerData())
                drawerLayout.closeDrawer(navDrawer)
            }
            2 -> {
                // Add joystick
                controlLayout.addJoystickButton(ControlJoystickData())
                drawerLayout.closeDrawer(navDrawer)
            }
            3 -> {
                // Load
                drawerLayout.closeDrawer(navDrawer)
                controlLayout.openLoadDialog()
            }
            4 -> {
                // Save
                drawerLayout.closeDrawer(navDrawer)
                controlLayout.openSaveDialog(this)
            }
            5 -> {
                // Select default controlmap
                drawerLayout.closeDrawer(navDrawer)
                controlLayout.openSetDefaultDialog()
            }
            6 -> {
                // Leave editor
                drawerLayout.closeDrawer(navDrawer)
                controlLayout.askToExit(this)
            }
        }
    }

    override fun exitEditor() {
        activity.runOnUiThread {
            isInEditor = false
            controlLayout.setModifiable(false)
            loadActiveMojoLayout()
            navDrawer.adapter = gameActionArrayAdapter
            gameMenuWrapper.setVisibility(!controlLayout.hasMenuButton())
            drawerLayout.closeDrawer(navDrawer)
        }
    }

    private fun loadActiveMojoLayout() {
        try {
            val selectedFile = ControlManager.getSelectedLayoutFile()
            val layoutPath = if (selectedFile != null && selectedFile.exists()) {
                selectedFile.absolutePath
            } else {
                val assetFallback = File(PathManager.DIR_CONTROL_LAYOUTS, "mojo_layout.json")
                if (!assetFallback.exists()) {
                    runCatching {
                        activity.assets.open("mojo_layout.json").use { input ->
                            assetFallback.parentFile?.mkdirs()
                            assetFallback.outputStream().use { out -> input.copyTo(out) }
                        }
                    }
                }
                assetFallback.absolutePath
            }
            controlLayout.loadLayout(layoutPath)
            gameMenuWrapper.setVisibility(!controlLayout.hasMenuButton())
        } catch (e: Exception) {
            Tools.showError(activity, e)
        }
    }

    override fun onClickedMenu() {
        if (drawerLayout.isDrawerOpen(navDrawer)) {
            drawerLayout.closeDrawer(navDrawer)
        } else {
            drawerLayout.openDrawer(navDrawer)
        }
    }

    fun handleBackPressed(): Boolean {
        if (drawerLayout.isDrawerOpen(navDrawer)) {
            drawerLayout.closeDrawer(navDrawer)
            return true
        }
        if (isInEditor) {
            controlLayout.askToExit(this)
            return true
        }
        return false
    }

    fun switchKeyboardState() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        imm?.toggleSoftInput(android.view.inputmethod.InputMethodManager.SHOW_FORCED, 0)
    }

    fun toggleMouse() {
        val curMode = AllSettings.hideMouse.getValue()
        AllSettings.hideMouse.save(!curMode)
        Toast.makeText(activity, if (curMode) "Mouse Enabled" else "Mouse Disabled", Toast.LENGTH_SHORT).show()
    }

    fun onDestroy() {
        gameMenuWrapper.release()
        if (activeController?.get() === this) {
            activeController = null
        }
    }
}
