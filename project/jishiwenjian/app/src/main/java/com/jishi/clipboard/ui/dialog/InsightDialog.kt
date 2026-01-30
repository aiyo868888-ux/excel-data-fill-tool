package com.jishi.clipboard.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.text.method.ScrollingMovementMethod
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jishi.clipboard.R
import com.jishi.clipboard.data.json.InsightItem

/**
 * 启发对话框
 */
class InsightDialog : BottomSheetDialogFragment() {
    private var onSaveListener: ((InsightItem) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null
    private var initialContent: String = ""

    private lateinit var etContent: EditText
    private lateinit var btnSave: Button

    fun setOnSaveListener(listener: (InsightItem) -> Unit) {
        onSaveListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    /**
     * 设置初始内容
     */
    fun setInitialContent(content: String) {
        initialContent = content
        if (::etContent.isInitialized) {
            etContent.setText(content)
            etContent.setSelection(content.length)
        }
    }

    /**
     * 聚焦到输入框并显示键盘
     */
    fun requestFocusAndShowKeyboard() {
        if (::etContent.isInitialized) {
            etContent.requestFocus()
            etContent.post {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etContent, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_insight, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etContent = view.findViewById(R.id.etContent)
        btnSave = view.findViewById(R.id.btnSave)

        // 启用 EditText 滚动
        etContent.movementMethod = ScrollingMovementMethod.getInstance()
        etContent.setVerticalScrollBarEnabled(true)

        // 设置初始内容
        if (initialContent.isNotEmpty()) {
            etContent.setText(initialContent)
            etContent.setSelection(initialContent.length)
        }

        btnSave.setOnClickListener {
            saveInsight()
        }
    }

    private fun saveInsight() {
        val content = etContent.text?.toString()?.trim() ?: ""
        if (content.isEmpty()) {
            etContent.error = "内容不能为空"
            return
        }

        val item = InsightItem(content = content)
        onSaveListener?.invoke(item)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismissListener?.invoke()
        super.onDismiss(dialog)
    }

    companion object {
        fun newInstance() = InsightDialog()
    }
}
