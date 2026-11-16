package com.movtery.zalithlauncher.ui.subassembly.hotbar

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings

enum class HotbarType(val nameId: Int, val valueName: String) {
    AUTO(R.string.option_hotbar_type_auto, "auto"),
    MANUALLY(R.string.option_hotbar_type_manually, "manually")
}

class HotbarUtils {
    companion object {
        @JvmStatic
        fun getCurrentType(): HotbarType {
            return when (AllSettings.hotbarType.getValue()) {
                "manually" -> HotbarType.MANUALLY
                "auto" -> HotbarType.AUTO
                else -> HotbarType.AUTO
            }
        }

        @JvmStatic
        fun getCurrentTypeIndex(): Int {
            return when (AllSettings.hotbarType.getValue()) {
                "manually" -> 1
                "auto" -> 0
                else -> 0
            }
        }
    }
}

