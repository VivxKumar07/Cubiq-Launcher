package com.movtery.zalithlauncher.ui.dialog

import android.content.Context
import com.movtery.zalithlauncher.R
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.Executors

class DeleteDialog(context: Context, onFinished: Runnable, files: List<File>) {
    private val mDialog: TipDialog = TipDialog.Builder(context).apply {
        setCancelable(false)

        val singleFile = files.size == 1
        val file = files[0]
        val isFolder = file.isDirectory

        setTitle(
            if (singleFile) (
                if (isFolder) R.string.file_delete_dir else R.string.file_tips
            ) else R.string.file_delete_multiple_items_title
        )

        setMessage(
            if (singleFile) (
                if (isFolder) R.string.file_delete_dir_message else R.string.file_delete
            ) else R.string.file_delete_multiple_items_message
        )

        setConfirm(R.string.generic_delete)
        setWarning()

        setConfirmClickListener {
            Executors.newSingleThreadExecutor().execute {
                for (target in files) {
                    FileUtils.deleteQuietly(target)
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post(onFinished)
            }
        }
    }.buildDialog()

    fun show() {
        mDialog.show()
    }
}

