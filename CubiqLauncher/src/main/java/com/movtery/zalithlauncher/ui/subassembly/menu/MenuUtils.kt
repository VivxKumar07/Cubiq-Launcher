package com.movtery.zalithlauncher.ui.subassembly.menu

import android.annotation.SuppressLint
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class MenuUtils {
    companion object {
        @JvmStatic
        fun adjustSeekbar(seekBar: SeekBar, v: Int) {
            seekBar.progress += v
        }

        @JvmStatic
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        fun toggleSwitchState(switchView: Switch) {
            switchView.isChecked = !switchView.isChecked
        }

        @JvmStatic
        fun initSeekBarValue(seek: SeekBar, value: Int, valueView: TextView, suffix: String) {
            seek.progress = value
            updateSeekbarValue(value, valueView, suffix)
        }

        @JvmStatic
        fun updateSeekbarValue(value: Int, valueView: TextView, suffix: String) {
            val valueText = "$value $suffix"
            valueView.text = valueText.trim { it <= ' ' }
        }
    }
}

