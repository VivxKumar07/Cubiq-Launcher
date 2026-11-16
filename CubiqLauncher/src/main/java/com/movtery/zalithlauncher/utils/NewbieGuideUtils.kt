package com.movtery.zalithlauncher.utils

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

class NewbieGuideUtils {
    companion object {
        private const val TEXT_COLOR = R.color.black_or_white
        private const val TARGET_CIRCLE_COLOR = R.color.background_menu_element
        private val NEWBIE_TAGS: MutableList<String> = ArrayList()
        private val guideFile: File
            get() = File(PathManager.DIR_FILES_PRIVATE, "newbie_guide.json")

        init {
            runCatching {
                val file = guideFile
                if (!file.exists()) {
                    file.parentFile?.mkdirs()
                    file.createNewFile()
                }
                val read = file.readText()
                if (read.isNotBlank()) {
                    val jsonArray = JsonParser.parseString(read).asJsonArray
                    for (element in jsonArray) {
                        NEWBIE_TAGS.add(element.asString)
                    }
                }
            }.onFailure { e ->
                Logger.warning("NewbieGuideUtils", "Failed to load newbie guide tags", e)
            }
        }

        @JvmStatic
        fun showOnlyOne(tag: String): Boolean {
            if (!NEWBIE_TAGS.contains(tag)) {
                NEWBIE_TAGS.add(tag)
                saveTags()
                return false
            }
            return true
        }

        private fun saveTags() {
            val jsonArray = JsonArray()
            for (tag in NEWBIE_TAGS) {
                jsonArray.add(tag)
            }
            runCatching {
                guideFile.writeText(jsonArray.toString())
            }.onFailure { e ->
                Logger.warning("NewbieGuideUtils", "Failed to save newbie guide tags", e)
            }
        }

        @JvmStatic
        fun getSimpleTarget(context: Context, view: View?, title: String, desc: String): TapTarget {
            return TapTarget.forView(view, title, desc)
                .titleTextColor(TEXT_COLOR)
                .descriptionTextColor(TEXT_COLOR)
                .targetCircleColorInt(ContextCompat.getColor(context, TARGET_CIRCLE_COLOR))
        }

        @JvmStatic
        fun getSimpleTarget(context: Context, view: View?, title: String): TapTarget {
            return TapTarget.forView(view, title)
                .titleTextColor(TEXT_COLOR)
                .targetCircleColorInt(ContextCompat.getColor(context, TARGET_CIRCLE_COLOR))
        }
    }
}

