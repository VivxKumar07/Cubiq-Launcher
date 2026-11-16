package com.movtery.zalithlauncher.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.databinding.DialogControlInfoBinding
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.dialog.DraggableDialog.DialogInitializationListener
import com.movtery.zalithlauncher.ui.subassembly.customcontrols.ControlInfoData
import com.movtery.zalithlauncher.ui.subassembly.customcontrols.EditControlData
import java.io.File

class ControlInfoDialog(
    context: Context,
    private val controlInfoData: ControlInfoData,
    private val onUpdated: Runnable
) : FullScreenDialog(context), DialogInitializationListener {
    private val binding = DialogControlInfoBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setContentView(binding.root)

        init(context)
        DraggableDialog.initDialog(this)
    }

    private fun TextView.setTextOrDefault(value: String?) {
        this.text = value?.takeIf { it.isNotEmpty() && it != "null" } ?: context.getString(R.string.generic_unknown)
    }

    private fun init(context: Context) {
        binding.apply {
            closeButton.setOnClickListener { dismiss() }
            editButton.setOnClickListener {
                val editControlInfoDialog = EditControlInfoDialog(
                    context, false, controlInfoData.fileName,
                    controlInfoData
                )
                editControlInfoDialog.setTitle(context.getString(R.string.generic_edit))
                editControlInfoDialog.setOnConfirmClickListener { fileName: String, updatedInfo: ControlInfoData ->
                    val controlFile = File(PathManager.DIR_CONTROL_LAYOUTS, fileName)
                    EditControlData.loadCustomControlsFromFile(context, controlFile)?.let { customControls ->
                        customControls.mControlInfoDataList.name = updatedInfo.name
                        customControls.mControlInfoDataList.author = updatedInfo.author
                        customControls.mControlInfoDataList.version = updatedInfo.version
                        customControls.mControlInfoDataList.desc = updatedInfo.desc
                        EditControlData.saveToFile(context, customControls, controlFile)
                    }
                    onUpdated.run()
                    editControlInfoDialog.dismiss()
                }
                editControlInfoDialog.show()
                dismiss()
            }

            nameText.setTextOrDefault(controlInfoData.name)
            fileNameText.setTextOrDefault(controlInfoData.fileName)
            authorText.setTextOrDefault(controlInfoData.author)
            versionText.setTextOrDefault(controlInfoData.version)
            descText.setTextOrDefault(controlInfoData.desc)

            checkHeight(root, contentView, scrollView)
        }
    }

    override fun onInit(): Window? = window
}

