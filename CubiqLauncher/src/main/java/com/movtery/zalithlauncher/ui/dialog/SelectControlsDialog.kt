package com.movtery.zalithlauncher.ui.dialog

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.movtery.zalithlauncher.ui.subassembly.customcontrols.ControlSelectedListener
import com.movtery.zalithlauncher.ui.subassembly.customcontrols.ControlsListViewCreator
import java.io.File

class SelectControlsDialog(
    context: Context,
    private val selectedListener: SelectedListener
) : AbstractSelectDialog(context) {

    override fun initDialog(recyclerView: RecyclerView) {
        ControlsListViewCreator(context, recyclerView).apply {
            listAtPath()
            setSelectedListener(object : ControlSelectedListener() {
                override fun onItemSelected(file: File) {
                    selectedListener.onSelected(file)
                    dismiss()
                }

                override fun onItemLongClick(file: File) {
                }
            })
        }
    }

    fun interface SelectedListener {
        fun onSelected(file: File)
    }
}

