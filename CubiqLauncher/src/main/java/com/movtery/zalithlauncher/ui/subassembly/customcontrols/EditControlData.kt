package com.movtery.zalithlauncher.ui.subassembly.customcontrols

import android.content.Context
import com.movtery.zalithlauncher.utils.logging.Logger
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.customcontrols.CustomControls
import net.kdt.pojavlaunch.customcontrols.LayoutConverter
import org.json.JSONObject
import java.io.File

class EditControlData {
    companion object {
        @JvmStatic
        fun loadFormFile(context: Context?, file: File): ControlInfoData? {
            val customControls = loadCustomControlsFromFile(context, file)
            customControls?.let {
                val info = customControls.mControlInfoDataList
                info.fileName = file.name
                return info
            }
            return null
        }

        @JvmStatic
        fun loadCustomControlsFromFile(context: Context?, file: File): CustomControls? {
            return runCatching {
                val jsonLayoutData = Tools.read(file)
                val layoutJsonObject = JSONObject(jsonLayoutData)
                LayoutConverter.loadFromJsonObject(context, layoutJsonObject, jsonLayoutData, file.absolutePath, false)
            }.getOrElse { e ->
                Logger.warning("Load Controls", "Failed to parse ZL1 controls: ${file.name}", e)
                null
            }
        }

        @JvmStatic
        fun saveToFile(context: Context?, customControls: CustomControls?, file: File) {
            runCatching {
                Tools.write(file.absolutePath, Tools.GLOBAL_GSON.toJson(customControls))
            }.onFailure { e ->
                Tools.showError(context, e)
            }
        }

        @JvmStatic
        fun createNewControlFile(context: Context?, jsonFile: File, controlInfoData: ControlInfoData?) {
            val customControls = CustomControls(ArrayList(), ArrayList(), ArrayList(), controlInfoData)
            saveToFile(context, customControls, jsonFile)
        }
    }
}

