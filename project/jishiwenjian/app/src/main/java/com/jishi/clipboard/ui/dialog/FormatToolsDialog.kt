package com.jishi.clipboard.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jishi.clipboard.R

/**
 * 格式工具弹窗对话框
 *
 * 提供各种格式化工具选项，用于在编辑文本时快速插入格式
 */
class FormatToolsDialog : BottomSheetDialogFragment() {

    private var onToolSelected: ((String) -> Unit)? = null

    /**
     * 设置工具选择监听器
     * @param listener 当工具被选择时回调，参数为工具类型（quote, list, code, divider, clear, voice）
     */
    fun setOnToolSelected(listener: (String) -> Unit) {
        onToolSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_format_tools, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 引用块
        view.findViewById<View>(R.id.btnQuote).setOnClickListener {
            onToolSelected?.invoke("quote")
            dismiss()
        }

        // 列表
        view.findViewById<View>(R.id.btnList).setOnClickListener {
            onToolSelected?.invoke("list")
            dismiss()
        }

        // 代码块
        view.findViewById<View>(R.id.btnCode).setOnClickListener {
            onToolSelected?.invoke("code")
            dismiss()
        }

        // 分割线
        view.findViewById<View>(R.id.btnDivider).setOnClickListener {
            onToolSelected?.invoke("divider")
            dismiss()
        }

        // 清空格式
        view.findViewById<View>(R.id.btnClearFormat).setOnClickListener {
            onToolSelected?.invoke("clear")
            dismiss()
        }

        // 语音输入
        view.findViewById<View>(R.id.btnVoiceInput).setOnClickListener {
            onToolSelected?.invoke("voice")
            dismiss()
        }
    }

    companion object {
        const val TAG = "FormatToolsDialog"
    }
}
