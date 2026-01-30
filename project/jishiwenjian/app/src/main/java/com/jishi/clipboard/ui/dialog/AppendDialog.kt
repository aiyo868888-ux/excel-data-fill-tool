package com.jishi.clipboard.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.jishi.clipboard.R
import com.jishi.clipboard.databinding.DialogAppendBinding

/**
 * 追加笔记对话框
 */
class AppendDialog(
    context: Context,
    private val originalContent: String,
    private val onConfirm: (String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAppendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAppendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置原文预览（显示前20字）
        binding.originalContentText.text = if (originalContent.length > 20) {
            "${originalContent.take(20)}..."
        } else {
            originalContent
        }

        // 取消按钮
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        // 确认按钮
        binding.confirmButton.setOnClickListener {
            val appendContent = binding.appendContentInput.text?.toString()?.trim() ?: ""
            if (appendContent.isNotEmpty()) {
                onConfirm(appendContent)
            }
            dismiss()
        }
    }

    override fun show() {
        super.show()
        // 自动聚焦到输入框并弹出键盘
        binding.appendContentInput.requestFocus()
    }
}
