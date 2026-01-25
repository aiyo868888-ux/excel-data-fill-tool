package com.jishi.clipboard.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.jishi.clipboard.R
import com.jishi.clipboard.databinding.DialogContentTypeSelectionBinding

/**
 * 内容类型选择对话框
 * 提供三种类型：灵感、启发、待办
 */
class ContentTypeSelectionDialog : DialogFragment() {

    private var _binding: DialogContentTypeSelectionBinding? = null
    private val binding get() = _binding!!

    var onContentTypeSelected: ((contentType: String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // 设置对话框样式
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogContentTypeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置点击监听
        binding.typeInspiration.setOnClickListener {
            onContentTypeSelected?.invoke("灵感")
            dismiss()
        }

        binding.typeInsight.setOnClickListener {
            onContentTypeSelected?.invoke("启发")
            dismiss()
        }

        binding.typeTodo.setOnClickListener {
            onContentTypeSelected?.invoke("待办")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        // 设置对话框位置和宽度
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }
    }

    companion object {
        const val TAG = "ContentTypeSelectionDialog"

        fun newInstance(): ContentTypeSelectionDialog {
            return ContentTypeSelectionDialog()
        }
    }
}
