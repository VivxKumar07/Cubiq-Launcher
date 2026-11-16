package com.movtery.zalithlauncher.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.EditText
import androidx.annotation.CheckResult
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.databinding.DialogEditTextBinding
import com.movtery.zalithlauncher.ui.dialog.DraggableDialog.DialogInitializationListener
import com.movtery.zalithlauncher.utils.stringutils.StringUtils

class EditTextDialog private constructor(
    private val context: Context,
    private val title: String?,
    private val message: String?,
    private val editText: String?,
    private val hintText: String?,
    private val checkBox: String?,
    private val confirm: String?,
    private val emptyError: String?,
    private val showCheckBox: Boolean,
    private val inputType: Int,
    private val cancelListener: View.OnClickListener?,
    private val confirmListener: ConfirmListener?,
    private val required: Boolean
) : FullScreenDialog(context), DialogInitializationListener {
    private val binding = DialogEditTextBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setCancelable(false)
        this.setContentView(binding.root)
        init()
        DraggableDialog.initDialog(this)
    }

    private fun init() {
        binding.apply {
            title?.let { titleView.text = it }
            message?.let {
                messageView.text = it
                messageView.visibility = View.VISIBLE
            }
            editText?.let { textEdit.setText(it) }
            hintText?.let { textEdit.hint = it } ?: run {
                if (required) textEdit.setHint(R.string.generic_required)
            }
            checkHeight(root, contentView, scrollView)

            confirm?.let { confirmButton.text = it }
            if (showCheckBox) {
                checkBox.visibility = View.VISIBLE
                checkBox.text = this@EditTextDialog.checkBox
            }
            if (inputType != -1) textEdit.inputType = inputType

            confirmListener?.let {
                confirmButton.setOnClickListener { _ ->
                    if (required) {
                        val text = textEdit.text.toString()
                        if (StringUtils.isEmptyOrBlank(text)) {
                            textEdit.error = emptyError ?: context.getString(R.string.generic_error_field_empty)
                            return@setOnClickListener
                        }
                    }
                    val dismissDialog = it.onConfirm(textEdit, checkBox.isChecked)
                    if (dismissDialog) dismiss()
                }
            }

            cancelButton.setOnClickListener(cancelListener ?: View.OnClickListener { dismiss() })
        }
    }

    override fun onInit(): Window? = window

    fun interface ConfirmListener {
        fun onConfirm(editText: EditText, checked: Boolean): Boolean
    }

    class Builder(private val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var editText: String? = null
        private var hintText: String? = null
        private var checkBox: String? = null
        private var confirm: String? = null
        private var emptyError: String? = null
        private var showCheckBox = false
        private var inputType = -1
        private var cancelListener: View.OnClickListener? = null
        private var confirmListener: ConfirmListener? = null
        private var required = false

        @CheckResult fun setTitle(title: Int): Builder = setTitle(context.getString(title))
        @CheckResult fun setTitle(title: String?): Builder = apply { this.title = title }
        @CheckResult fun setMessage(message: Int): Builder = setMessage(context.getString(message))
        @CheckResult fun setMessage(message: String?): Builder = apply { this.message = message }
        @CheckResult fun setEditText(editText: String?): Builder = apply { this.editText = editText }
        @CheckResult fun setHintText(hintText: Int): Builder = setHintText(context.getString(hintText))
        @CheckResult fun setHintText(hintText: String?): Builder = apply { this.hintText = hintText }
        @CheckResult fun setConfirmText(text: Int): Builder = setConfirmText(context.getString(text))
        @CheckResult fun setConfirmText(text: String?): Builder = apply { this.confirm = text }
        @CheckResult fun setEmptyError(emptyError: String?): Builder = apply { this.emptyError = emptyError }
        @CheckResult fun setShowCheckBox(show: Boolean): Builder = apply { this.showCheckBox = show }
        @CheckResult fun setCheckBoxText(text: Int): Builder = setCheckBoxText(context.getString(text))
        @CheckResult fun setCheckBoxText(text: String?): Builder = apply { this.checkBox = text }
        @CheckResult fun setInputType(inputType: Int): Builder = apply { this.inputType = inputType }
        @CheckResult fun setCancelListener(cancel: View.OnClickListener?): Builder = apply { this.cancelListener = cancel }
        @CheckResult fun setConfirmListener(listener: ConfirmListener?): Builder = apply { this.confirmListener = listener }
        @CheckResult fun setAsRequired(): Builder = apply { this.required = true }

        fun buildDialog(): EditTextDialog {
            return EditTextDialog(
                context, title, message, editText, hintText, checkBox, confirm, emptyError,
                showCheckBox, inputType, cancelListener, confirmListener, required
            ).apply { create() }
        }

        fun showDialog() {
            buildDialog().show()
        }
    }
}

