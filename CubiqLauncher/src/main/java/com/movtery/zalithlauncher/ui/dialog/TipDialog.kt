package com.movtery.zalithlauncher.ui.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.annotation.CheckResult
import com.movtery.zalithlauncher.databinding.DialogTipBinding
import com.movtery.zalithlauncher.ui.dialog.DraggableDialog.DialogInitializationListener

class TipDialog private constructor(
    context: Context,
    private val title: String?,
    private val message: String?,
    private val confirm: String?,
    private val cancel: String?,
    private val checkBoxText: String?,
    private val showCheckBox: Boolean,
    private val showCancel: Boolean,
    private val showConfirm: Boolean,
    private val centerMessage: Boolean,
    private var selectable: Boolean,
    private val confirmButtonCountdown: Long,
    private val warning: Boolean,
    private val textBeautifier: TextBeautifier?,
    private val cancelListener: OnCancelClickListener?,
    private val confirmListener: OnConfirmClickListener?,
    private val dismissListener: OnDialogDismissListener?
) : FullScreenDialog(context), DialogInitializationListener {
    private val binding = DialogTipBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun TextView.addText(textString: String?) {
            this.text = textString
            if (textString == null) this.visibility = View.GONE
        }

        binding.apply {
            setContentView(root)
            DraggableDialog.initDialog(this@TipDialog)

            titleView.addText(title)
            messageView.addText(message)
            messageView.setTextIsSelectable(selectable)
            textBeautifier?.beautify(titleView, messageView)
            checkHeight(root, contentView, scrollView)

            cancel?.let { cancelButton.text = it }
            confirm?.let { confirmButton.text = it }
            if (centerMessage) messageView.gravity = Gravity.CENTER_HORIZONTAL
            if (showCheckBox) {
                checkBox.visibility = View.VISIBLE
                checkBoxText?.let { checkBox.text = it }
            }

            cancelButton.setOnClickListener {
                cancelListener?.onCancelClick()
                this@TipDialog.dismiss()
            }
            confirmButton.setOnClickListener {
                confirmListener?.onConfirmClick(checkBox.isChecked)
                this@TipDialog.dismiss()
            }

            cancelButton.visibility = if (showCancel) View.VISIBLE else View.GONE
            confirmButton.visibility = if (showConfirm) View.VISIBLE else View.GONE

            if (warning) {
                warningIcon.visibility = View.VISIBLE
                warningIcon.drawable?.setTint(Color.RED)
            }
        }
    }

    override fun show() {
        super.show()
        window?.findViewById<View>(android.R.id.content)?.measure(0, 0)

        if (confirmButtonCountdown > 0) {
            binding.confirmButton.isEnabled = false
            val handler = Handler(Looper.getMainLooper())
            val startTime = System.currentTimeMillis()
            val runnable = object : Runnable {
                override fun run() {
                    val elapsed = System.currentTimeMillis() - startTime
                    val remaining = ((confirmButtonCountdown - elapsed) / 1000L).toInt()
                    if (remaining > 0) {
                        val baseText = confirm ?: context.getString(com.movtery.zalithlauncher.R.string.generic_confirm)
                        binding.confirmButton.text = "$baseText ($remaining)"
                        handler.postDelayed(this, 250L)
                    } else {
                        binding.confirmButton.isEnabled = true
                        binding.confirmButton.text = confirm ?: context.getString(com.movtery.zalithlauncher.R.string.generic_confirm)
                    }
                }
            }
            handler.post(runnable)
        }
    }

    override fun dismiss() {
        super.dismiss()
        dismissListener?.onDismiss()
    }

    override fun onInit(): Window? = window

    fun interface TextBeautifier {
        fun beautify(title: TextView, message: TextView)
    }

    fun interface OnCancelClickListener {
        fun onCancelClick()
    }

    fun interface OnConfirmClickListener {
        fun onConfirmClick(checked: Boolean)
    }

    fun interface OnDialogDismissListener {
        fun onDismiss()
    }

    class Builder(private val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var cancel: String? = null
        private var confirm: String? = null
        private var checkBox: String? = null
        private var textBeautifier: TextBeautifier? = null
        private var cancelClickListener: OnCancelClickListener? = null
        private var confirmClickListener: OnConfirmClickListener? = null
        private var dialogDismissListener: OnDialogDismissListener? = null
        private var cancelable = true
        private var showCheckBox = false
        private var showCancel = true
        private var showConfirm = true
        private var centerMessage = true
        private var selectable = false
        private var confirmButtonCountdown = -1L
        private var warning = false

        fun buildDialog(): TipDialog {
            val tipDialog = TipDialog(
                this.context,
                title, message, confirm, cancel, checkBox,
                showCheckBox, showCancel, showConfirm, centerMessage, selectable,
                confirmButtonCountdown, warning,
                textBeautifier, cancelClickListener, confirmClickListener, dialogDismissListener
            )
            tipDialog.setCancelable(cancelable)
            return tipDialog
        }

        fun showDialog() {
            buildDialog().show()
        }

        @CheckResult fun setTitle(title: String?): Builder = apply { this.title = title }
        @CheckResult fun setTitle(title: Int): Builder = setTitle(context.getString(title))
        @CheckResult fun setMessage(message: String?): Builder = apply { this.message = message }
        @CheckResult fun setMessage(message: Int): Builder = setMessage(context.getString(message))
        @CheckResult fun setCancel(cancel: String?): Builder = apply { this.cancel = cancel }
        @CheckResult fun setCancel(cancel: Int): Builder = setCancel(context.getString(cancel))
        @CheckResult fun setCheckBox(checkBoxText: String?): Builder = apply { this.checkBox = checkBoxText }
        @CheckResult fun setCheckBox(checkBoxText: Int): Builder = setCheckBox(context.getString(checkBoxText))
        @CheckResult fun setConfirm(confirm: String?): Builder = apply { this.confirm = confirm }
        @CheckResult fun setConfirm(confirm: Int): Builder = setConfirm(context.getString(confirm))
        @CheckResult fun setShowCheckBox(show: Boolean): Builder = apply { showCheckBox = show }
        @CheckResult fun setTextBeautifier(beautifier: TextBeautifier): Builder = apply { this.textBeautifier = beautifier }
        @CheckResult fun setCancelClickListener(listener: OnCancelClickListener?): Builder = apply { this.cancelClickListener = listener }
        @CheckResult fun setConfirmClickListener(listener: OnConfirmClickListener?): Builder = apply { this.confirmClickListener = listener }
        @CheckResult fun setDialogDismissListener(listener: OnDialogDismissListener?): Builder = apply { this.dialogDismissListener = listener }
        @CheckResult fun setConfirmButtonCountdown(countdownMillis: Long): Builder = apply { this.confirmButtonCountdown = countdownMillis }
        @CheckResult fun setCancelable(cancelable: Boolean): Builder = apply { this.cancelable = cancelable }
        @CheckResult fun setShowCancel(showCancel: Boolean): Builder = apply { this.showCancel = showCancel }
        @CheckResult fun setShowConfirm(showConfirm: Boolean): Builder = apply { this.showConfirm = showConfirm }
        @CheckResult fun setCenterMessage(center: Boolean): Builder = apply { this.centerMessage = center }
        @CheckResult fun setSelectable(selectable: Boolean): Builder = apply { this.selectable = selectable }
        @CheckResult fun setWarning(): Builder = apply { this.warning = true }
    }
}

