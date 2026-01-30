package com.jishi.clipboard.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jishi.clipboard.R

/**
 * 导出对话框
 */
class ExportDialog : BottomSheetDialogFragment() {
    private var onExportListener: ((ExportType) -> Unit)? = null

    private lateinit var exportTypeRadioGroup: RadioGroup
    private lateinit var btnCancel: Button
    private lateinit var btnExport: Button

    enum class ExportType {
        ALL,           // 全部导出
        INSPIRATION,   // 仅灵感
        INSIGHT,       // 仅启发
        TODO           // 仅待办
    }

    fun setOnExportListener(listener: (ExportType) -> Unit) {
        onExportListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_export, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exportTypeRadioGroup = view.findViewById(R.id.exportTypeRadioGroup)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnExport = view.findViewById(R.id.btnExport)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnExport.setOnClickListener {
            val exportType = when (exportTypeRadioGroup.checkedRadioButtonId) {
                R.id.rbExportAll -> ExportType.ALL
                R.id.rbExportInspiration -> ExportType.INSPIRATION
                R.id.rbExportInsight -> ExportType.INSIGHT
                R.id.rbExportTodo -> ExportType.TODO
                else -> ExportType.ALL
            }
            onExportListener?.invoke(exportType)
            dismiss()
        }
    }
}
