package com.movtery.zalithlauncher.ui.subassembly.filelist

import android.graphics.drawable.Drawable
import com.movtery.zalithlauncher.utils.stringutils.SortStrings.Companion.compareChar
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Date

class FileItemBean(
    @JvmField val name: String,
    @JvmField val date: Date?,
    @JvmField val size: Long?
) : Comparable<FileItemBean?> {
    @JvmField var image: Drawable? = null
    @JvmField var file: File? = null
    @JvmField var isHighlighted: Boolean = false
    @JvmField var isCanCheck: Boolean = true

    constructor(file: File) : this(
        file.name,
        Date(file.lastModified()),
        if (file.isFile) FileUtils.sizeOf(file) else null
    ) {
        this.file = file
    }

    constructor(name: String, image: Drawable?) : this(name, null as Date?, null) {
        this.image = image
    }

    override fun compareTo(other: FileItemBean?): Int {
        other ?: return 1
        val thisName = file?.name ?: name
        val otherName = other.file?.name ?: other.name
        if (this.file != null && file!!.isDirectory && (other.file == null || !other.file!!.isDirectory)) {
            return -1
        } else if (other.file != null && other.file!!.isDirectory && (this.file == null || !this.file!!.isDirectory)) {
            return 1
        }
        return compareChar(thisName, otherName)
    }
}

