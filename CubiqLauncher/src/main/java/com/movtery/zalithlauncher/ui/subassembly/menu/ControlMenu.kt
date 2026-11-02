package com.movtery.zalithlauncher.ui.subassembly.menu

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.core.content.FileProvider
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.databinding.ViewZl1ControlMenuBinding
import com.movtery.zalithlauncher.setting.AllSettings
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.customcontrols.ControlData
import net.kdt.pojavlaunch.customcontrols.ControlDrawerData
import net.kdt.pojavlaunch.customcontrols.ControlJoystickData
import net.kdt.pojavlaunch.customcontrols.ControlLayout
import net.kdt.pojavlaunch.customcontrols.EditorExitable
import java.io.File

class ControlMenu(
    private val activity: Activity,
    private val exitListener: EditorExitable,
    private val binding: ViewZl1ControlMenuBinding,
    private val controlLayout: ControlLayout,
    private val export: Boolean
) : View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    init {
        val listener = this
        binding.apply {
            snapping.isChecked = AllSettings.buttonSnapping.getValue()

            MenuUtils.initSeekBarValue(snappingDistance, AllSettings.buttonSnappingDistance.getValue(), snappingDistanceValue, "dp")

            addButton.setOnClickListener(listener)
            addDrawer.setOnClickListener(listener)
            addJoystick.setOnClickListener(listener)

            load.setOnClickListener(listener)
            save.setOnClickListener(listener)
            saveAndExit.setOnClickListener(listener)
            saveAndExport.setOnClickListener(listener)

            snappingLayout.setOnClickListener(listener)
            snapping.setOnCheckedChangeListener(listener)
            snappingDistance.setOnSeekBarChangeListener(listener)
            snappingDistanceAdd.setOnClickListener(listener)
            snappingDistanceRemove.setOnClickListener(listener)
            selectDefault.setOnClickListener(listener)
            exit.setOnClickListener(listener)
            if (export) saveAndExport.visibility = View.VISIBLE
            else saveAndExport.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v) {
                addButton -> controlLayout.addControlButton(ControlData(activity.getString(R.string.controls_add_control_button)))
                addDrawer -> controlLayout.addDrawer(ControlDrawerData())
                addJoystick -> controlLayout.addJoystickButton(ControlJoystickData())

                load -> controlLayout.openLoadDialog()
                save -> controlLayout.openSaveDialog()
                saveAndExit -> controlLayout.openSaveAndExitDialog(exitListener)
                saveAndExport -> {
                    try {
                        val path = controlLayout.saveToDirectory(controlLayout.mLayoutFileName)
                        val file = File(path)
                        val contentUri = FileProvider.getUriForFile(activity, activity.packageName + ".provider", file)

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, contentUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        activity.startActivity(Intent.createChooser(shareIntent, controlLayout.mLayoutFileName))
                    } catch (e: Exception) {
                        Tools.showError(activity, e)
                    }
                }

                snappingLayout -> MenuUtils.toggleSwitchState(snapping)
                snappingDistanceAdd -> MenuUtils.adjustSeekbar(snappingDistance, 1)
                snappingDistanceRemove -> MenuUtils.adjustSeekbar(snappingDistance, -1)
                selectDefault -> controlLayout.openSetDefaultDialog()

                exit -> controlLayout.openExitDialog(exitListener)
                else -> {}
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        updateSeekbarValue(seekBar, !fromUser)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        updateSeekbarValue(seekBar, true)
    }

    private fun updateSeekbarValue(seekBar: SeekBar?, saveValue: Boolean) {
        val progress = seekBar?.progress ?: 0

        binding.apply {
            when (seekBar) {
                snappingDistance -> {
                    if (saveValue) AllSettings.buttonSnappingDistance.save(progress)
                    MenuUtils.updateSeekbarValue(progress, snappingDistanceValue, "dp")
                }
                else -> {}
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        binding.apply {
            when (buttonView) {
                snapping -> AllSettings.buttonSnapping.save(isChecked)
                else -> {}
            }
        }
    }
}
