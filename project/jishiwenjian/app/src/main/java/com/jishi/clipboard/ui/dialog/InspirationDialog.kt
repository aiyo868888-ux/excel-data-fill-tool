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
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jishi.clipboard.R
import com.jishi.clipboard.data.json.InspirationItem

/**
 * 灵感对话框
 */
class InspirationDialog : BottomSheetDialogFragment() {
    private var onSaveListener: ((InspirationItem) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null
    private var initialContent: String = ""

    private lateinit var etContent: EditText
    private lateinit var btnSave: Button

    fun setOnSaveListener(listener: (InspirationItem) -> Unit) {
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
        // 如果对话框已经创建，直接设置
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
        return inflater.inflate(R.layout.dialog_inspiration, container, false)
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
            saveInspiration()
        }
    }

    private fun saveInspiration() {
        android.util.Log.d("InspirationDialog", "保存按钮被点击")
        val content = etContent.text?.toString()?.trim() ?: ""
        android.util.Log.d("InspirationDialog", "输入内容: '$content'")

        if (content.isEmpty()) {
            etContent.error = "内容不能为空"
            android.util.Log.d("InspirationDialog", "内容为空，返回")
            return
        }

        val item = InspirationItem(content = content)
        android.util.Log.d("InspirationDialog", "创建 InspirationItem: ${item.id}")
        android.util.Log.d("InspirationDialog", "onSaveListener 是否为空: ${onSaveListener == null}")

        onSaveListener?.invoke(item)
        android.util.Log.d("InspirationDialog", "回调已调用")
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismissListener?.invoke()
        super.onDismiss(dialog)
    }

    companion object {
        fun newInstance() = InspirationDialog()
    }
}
