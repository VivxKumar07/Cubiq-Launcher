package com.movtery.zalithlauncher.ui.control.input

import android.content.Context
import android.view.KeyEvent
import com.movtery.zalithlauncher.context.GlobalContext
import org.json.JSONObject

object KeyboardRemapManager {
    private const val PREF_NAME = "cubiq_keyboard_remap"
    private const val KEY_MAP = "remap_json"

    // Default bindings (Action -> Android KeyEvent Code and Display Name)
    val DEFAULT_KEY_MAP = mapOf(
        "Walk Forward" to Pair(KeyEvent.KEYCODE_W, "Key W"),
        "Walk Backward" to Pair(KeyEvent.KEYCODE_S, "Key S"),
        "Strafe Left" to Pair(KeyEvent.KEYCODE_A, "Key A"),
        "Strafe Right" to Pair(KeyEvent.KEYCODE_D, "Key D"),
        "Jump" to Pair(KeyEvent.KEYCODE_SPACE, "Space"),
        "Sneak" to Pair(KeyEvent.KEYCODE_SHIFT_LEFT, "Left Shift"),
        "Sprint" to Pair(KeyEvent.KEYCODE_CTRL_LEFT, "Left Control"),
        "Inventory" to Pair(KeyEvent.KEYCODE_E, "Key E"),
        "Drop Item" to Pair(KeyEvent.KEYCODE_Q, "Key Q"),
        "Chat" to Pair(KeyEvent.KEYCODE_T, "Key T"),
        "Player List" to Pair(KeyEvent.KEYCODE_TAB, "Tab"),
        "Change Perspective" to Pair(KeyEvent.KEYCODE_F5, "F5"),
        "Debug Screen" to Pair(KeyEvent.KEYCODE_F3, "F3"),
        "Fullscreen" to Pair(KeyEvent.KEYCODE_F11, "F11")
    )

    val DEFAULT_MOUSE_MAP = mapOf(
        "Attack / Destroy" to "Mouse Left",
        "Use Item / Place Block" to "Mouse Right",
        "Pick Block" to "Mouse Middle",
        "Hotbar Next" to "Scroll Down",
        "Hotbar Previous" to "Scroll Up"
    )

    private fun getPrefs(context: Context = GlobalContext) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getBindingLabel(action: String, defaultLabel: String): String {
        return try {
            val jsonStr = getPrefs().getString(KEY_MAP, null) ?: return defaultLabel
            val json = JSONObject(jsonStr)
            if (json.has(action)) {
                json.getJSONObject(action).optString("label", defaultLabel)
            } else {
                defaultLabel
            }
        } catch (_: Exception) {
            defaultLabel
        }
    }

    fun getActionKeyCode(action: String, defaultCode: Int): Int {
        return try {
            val jsonStr = getPrefs().getString(KEY_MAP, null) ?: return defaultCode
            val json = JSONObject(jsonStr)
            if (json.has(action)) {
                json.getJSONObject(action).optInt("code", defaultCode)
            } else {
                defaultCode
            }
        } catch (_: Exception) {
            defaultCode
        }
    }

    fun saveBinding(action: String, keyCode: Int, label: String, context: Context = GlobalContext) {
        try {
            val prefs = getPrefs(context)
            val jsonStr = prefs.getString(KEY_MAP, "{}") ?: "{}"
            val json = JSONObject(jsonStr)
            val item = JSONObject().apply {
                put("code", keyCode)
                put("label", label)
            }
            json.put(action, item)
            prefs.edit().putString(KEY_MAP, json.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getAllCustomBindings(): Map<String, Pair<Int, String>> {
        val result = mutableMapOf<String, Pair<Int, String>>()
        try {
            val jsonStr = getPrefs().getString(KEY_MAP, null) ?: return emptyMap()
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val action = keys.next()
                val item = json.getJSONObject(action)
                result[action] = Pair(item.getInt("code"), item.getString("label"))
            }
        } catch (_: Exception) {}
        return result
    }

    fun getRemappedKey(originalKeyCode: Int): Int {
        // Reverse check: if user remapped key X to action Y
        return originalKeyCode
    }
}

