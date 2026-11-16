package com.movtery.zalithlauncher.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.subassembly.filelist.FileIcon
import com.movtery.zalithlauncher.ui.subassembly.filelist.FileItemBean
import com.movtery.zalithlauncher.ui.subassembly.filelist.FileRecyclerAdapter
import com.movtery.zalithlauncher.ui.subassembly.filelist.FileRecyclerViewCreator
import com.movtery.zalithlauncher.utils.image.isImageFile
import java.io.File

class SelectMouseDialog(
    context: Context,
    private val listener: MouseSelectedListener
) : AbstractSelectDialog(context) {

    override fun initDialog(recyclerView: RecyclerView) {
        initView(recyclerView)
        setTitleText(R.string.custom_mouse_title)
        setMessageText(
            context.getString(
                R.string.custom_mouse_dialog,
                context.getString(R.string.setting_category_control),
                context.getString(R.string.custom_mouse_title)
            )
        )
    }

    private fun initView(mMouseListView: RecyclerView) {
        FileRecyclerViewCreator(
            context,
            mMouseListView,
            object : FileRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, itemBean: FileItemBean) {
                    val file = itemBean.file
                    if (file != null && file.exists() && file.isImageFile()) {
                        AllSettings.customMouse.save(file.name)
                        listener.onSelectedListener()
                        dismiss()
                        return
                    }
                    if (position == 0) {
                        AllSettings.customMouse.save("")
                        listener.onSelectedListener()
                        dismiss()
                    }
                }
            },
            null,
            getItems()
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getItems(): MutableList<FileItemBean> {
        val fileItemBeans = FileRecyclerViewCreator.loadItemBeansFromPath(
            context,
            mousePath(),
            FileIcon.FILE,
            showFile = true,
            showFolder = false
        )
        fileItemBeans.add(
            0,
            FileItemBean(
                context.getString(R.string.custom_mouse_default),
                ContextCompat.getDrawable(context, R.drawable.ic_mouse_pointer)
            )
        )
        return fileItemBeans
    }

    private fun mousePath(): File {
        val path = PathManager.DIR_MOUSE_POINTER
        if (!path.exists()) path.mkdirs()
        return path
    }

    fun interface MouseSelectedListener {
        fun onSelectedListener()
    }
}

