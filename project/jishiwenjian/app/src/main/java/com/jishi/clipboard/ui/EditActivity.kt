package com.jishi.clipboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.jishi.clipboard.R
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import com.jishi.clipboard.utils.DialogManager
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference

/**
 * 编辑 Activity - 透明对话框
 * 用于悬浮窗点击时显示编辑界面
 */
@AndroidEntryPoint
class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent) // 空布局

        // 注册到静态引用
        EditActivity.instance = WeakReference(this)

        // 显示对话框
        showEditDialog()
    }

    override fun onResume() {
        super.onResume()
        // Activity 恢复时，重新注册对话框引用
        val dialog = supportFragmentManager.findFragmentByTag("EditDialog") as? ClipboardEditDialogFragment
        if (dialog != null) {
            DialogManager.setCurrentEditDialog(dialog)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Activity 被带到前台时，确保对话框引用正确注册
        val dialog = supportFragmentManager.findFragmentByTag("EditDialog") as? ClipboardEditDialogFragment
        if (dialog != null) {
            DialogManager.setCurrentEditDialog(dialog)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清除静态引用
        if (EditActivity.instance?.get() == this) {
            EditActivity.instance = null
        }
    }

    private fun showEditDialog() {
        // 获取传入的内容类型
        val contentType = intent.getStringExtra(TypeSelectionActivity.EXTRA_CONTENT_TYPE) ?: "灵感"

        val dialog = ClipboardEditDialogFragment.newInstance(
            initialContent = ""
        )

        var isDismissedBySave = false

        dialog.setOnSaveListener { _, _ ->
            // 标记为保存引起的关闭
            isDismissedBySave = true
            // 保存成功后关闭 Activity
            finish()
        }

        dialog.setOnDismissListener {
            // 只有在非保存情况下才关闭 Activity
            DialogManager.clearCurrentEditDialog()
            if (!isDismissedBySave) {
                finish()
            }
        }

        // 显示对话框
        dialog.show(supportFragmentManager, "EditDialog")

        // 延迟设置默认标签和光标（等待对话框完全显示）
        // 增加延迟到 500ms，确保对话框完全展开
        Handler(Looper.getMainLooper()).postDelayed({
            setDefaultTagAndFocus(dialog, contentType)
        }, 500)
    }

    private fun setDefaultTagAndFocus(dialog: ClipboardEditDialogFragment, contentType: String) {
        try {
            // 设置默认标签
            dialog.setDefaultTag(contentType)
            DialogManager.currentContentType = contentType

            // 再次延迟，确保标签创建完成后再聚焦
            Handler(Looper.getMainLooper()).postDelayed({
                // 自动聚焦到输入框并弹出键盘，光标定位到末尾
                dialog.requestFocusAndShowKeyboard()
            }, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        // 静态弱引用，用于从外部访问 Activity 实例
        var instance: WeakReference<EditActivity>? = null
    }
}
