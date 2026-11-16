package com.movtery.zalithlauncher.ui.subassembly.filelist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.subassembly.filelist.FileRecyclerAdapter.OnMultiSelectListener
import com.movtery.zalithlauncher.utils.stringutils.StringFilter.Companion.containsSubstring
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class FileRecyclerViewCreator(
    context: Context?,
    recyclerView: RecyclerView,
    onItemClickListener: FileRecyclerAdapter.OnItemClickListener?,
    onItemLongClickListener: FileRecyclerAdapter.OnItemLongClickListener?,
    data: MutableList<FileItemBean> = ArrayList()
) {
    @JvmField
    val fileRecyclerAdapter: FileRecyclerAdapter = FileRecyclerAdapter()
    private val mainRecyclerView: RecyclerView = recyclerView
    private val itemBeans: MutableList<FileItemBean> = mutableListOf()
    private var filterString: String? = null

    init {
        fileRecyclerAdapter.setOnItemClickListener(onItemClickListener)
        fileRecyclerAdapter.setOnItemLongClickListener(onItemLongClickListener)

        mainRecyclerView.layoutAnimation = LayoutAnimationController(
            AnimationUtils.loadAnimation(context, R.anim.fade_downwards)
        )
        mainRecyclerView.layoutManager = LinearLayoutManager(context)
        mainRecyclerView.adapter = this.fileRecyclerAdapter

        if (data.isNotEmpty()) loadData(data)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadData(itemBeans: List<FileItemBean>) {
        this.itemBeans.clear()
        this.itemBeans.addAll(itemBeans)
        updateWithFilter()
    }

    private fun updateWithFilter() {
        fileRecyclerAdapter.updateItems(
            filterString?.takeIf { it.isNotEmpty() }?.let { string ->
                itemBeans.filter { it.name.contains(string, true) }
            } ?: itemBeans
        )
        mainRecyclerView.scheduleLayoutAnimation()
    }

    fun setOnMultiSelectListener(listener: OnMultiSelectListener?) {
        fileRecyclerAdapter.setOnMultiSelectListener(listener)
    }

    companion object {
        fun loadItemBeansFromPath(
            context: Context,
            path: File,
            fileIcon: FileIcon,
            showFile: Boolean,
            showFolder: Boolean
        ): MutableList<FileItemBean> {
            return loadItemBeansFromPath(
                context, null, false, false, null, path, fileIcon, showFile, showFolder
            )
        }

        @JvmStatic
        @SuppressLint("UseCompatLoadingForDrawables")
        fun loadItemBeansFromPath(
            context: Context,
            filterString: String?,
            showSearchResultsOnly: Boolean,
            caseSensitive: Boolean,
            searchCount: AtomicInteger?,
            path: File,
            fileIcon: FileIcon,
            showFile: Boolean,
            showFolder: Boolean
        ): MutableList<FileItemBean> {
            val itemBeans: MutableList<FileItemBean> = ArrayList()
            val files = path.listFiles()
            if (files != null) {
                val folderDrawable = ContextCompat.getDrawable(context, R.drawable.ic_folder)
                val fileDrawable = ContextCompat.getDrawable(context, R.drawable.ic_file)
                for (file in files) {
                    if (file.isDirectory && !showFolder) continue
                    if (file.isFile && !showFile) continue

                    val itemBean = FileItemBean(file)
                    itemBean.image = if (file.isDirectory) folderDrawable else getIcon(fileIcon, fileDrawable)

                    val isHighlighted = filterString != null && filterString.isNotEmpty() &&
                        containsSubstring(file.name, filterString, caseSensitive)
                    if (isHighlighted) {
                        searchCount?.addAndGet(1)
                        itemBean.isHighlighted = true
                    } else if (showSearchResultsOnly) {
                        continue
                    }

                    itemBeans.add(itemBean)
                }
            }
            itemBeans.sort()
            return itemBeans
        }

        private fun getIcon(fileIcon: FileIcon, fileDrawable: Drawable?): Drawable? {
            return when (fileIcon) {
                FileIcon.FILE, FileIcon.MOD -> fileDrawable
            }
        }
    }
}

