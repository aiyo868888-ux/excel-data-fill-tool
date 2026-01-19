package com.jishi.clipboard.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jishi.clipboard.R
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * 编辑 Activity - 透明对话框
 * 用于悬浮窗点击时显示编辑界面
 */
@AndroidEntryPoint
class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent) // 空布局

        // 获取参数
        val autoFillClipboard = intent.getBooleanExtra("auto_fill_clipboard", false)

        // 显示对话框
        showEditDialog(autoFillClipboard)
    }

    private fun showEditDialog(autoFillClipboard: Boolean) {
        val dialog = ClipboardEditDialogFragment.newInstance(
            initialContent = "",
            autoFillClipboard = autoFillClipboard
        )

        dialog.setOnSaveListener { _, _ ->
            // 保存成功后关闭 Activity
            finish()
        }

        dialog.setOnDismissListener {
            // 对话框关闭时也关闭 Activity
            finish()
        }

        // 显示对话框
        dialog.show(supportFragmentManager, "EditDialog")
    }

    override fun onBackPressed() {
        // 关闭对话框
        val dialog = supportFragmentManager.findFragmentByTag("EditDialog") as? ClipboardEditDialogFragment
        dialog?.dismiss()

        super.onBackPressed()
    }

    companion object {
        const val EXTRA_CONTENT = "initial_content"
        const val EXTRA_CLIPBOARD_ID = "clipboard_id"
    }
}
