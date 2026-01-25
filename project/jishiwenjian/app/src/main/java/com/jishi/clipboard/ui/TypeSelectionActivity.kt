package com.jishi.clipboard.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jishi.clipboard.R
import com.jishi.clipboard.ui.dialog.ContentTypeSelectionDialog
import com.jishi.clipboard.utils.DialogManager

/**
 * 类型选择 Activity - 透明背景
 * 用于显示内容类型选择对话框（灵感/启发/待办）
 */
class TypeSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_selection)

        // 如果 savedInstanceState 为 null，显示对话框
        if (savedInstanceState == null) {
            showTypeSelectionDialog()
        }
    }

    private fun showTypeSelectionDialog() {
        val dialog = ContentTypeSelectionDialog.newInstance()

        // 设置类型选择回调
        dialog.onContentTypeSelected = { contentType ->
            DialogManager.currentContentType = contentType
            // 跳转到编辑 Activity 并传递内容类型
            openEditActivity(contentType)
        }

        // 显示对话框
        dialog.show(supportFragmentManager, ContentTypeSelectionDialog.TAG)
    }

    private fun openEditActivity(contentType: String) {
        val intent = Intent(this, EditActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra(EXTRA_CONTENT_TYPE, contentType)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogManager.clearCurrentTypeDialog()
    }

    companion object {
        const val EXTRA_CONTENT_TYPE = "content_type"
    }
}
