package com.jishi.clipboard.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jishi.clipboard.R
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import com.jishi.clipboard.ui.dialog.ContentTypeSelectionDialog
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference

/**
 * 编辑 Activity - 透明对话框
 * 用于悬浮窗点击时显示编辑界面
 */
@AndroidEntryPoint
class EditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONTENT = "initial_content"
        const val EXTRA_CLIPBOARD_ID = "clipboard_id"

        // 静态弱引用，用于从外部访问 Activity 实例
        var instance: WeakReference<EditActivity>? = null
    }

    // 使用成员变量而不是局部变量，确保闭包能正确访问
    private var isDismissedBySave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent) // 空布局

        // 注册到静态引用
        EditActivity.instance = WeakReference(this)

        // 检查是否是分享过来的
        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            // 分享过来的内容，显示类型选择对话框
            showTypeSelectionDialog(sharedText)
        } else {
            // 正常流程：显示编辑对话框
            showEditDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Activity 恢复时，重新注册对话框引用（如果需要）
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Activity 被带到前台时，重新处理新的 Intent
        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            showTypeSelectionDialog(sharedText)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清除静态引用
        if (EditActivity.instance?.get() == this) {
            EditActivity.instance = null
        }
    }

    /**
     * 显示类型选择对话框（用于接收分享）
     */
    private fun showTypeSelectionDialog(content: String) {
        val dialog = ContentTypeSelectionDialog.newInstance()

        dialog.onContentTypeSelected = { contentType ->
            // 根据选择的类型打开对应的对话框
            when (contentType) {
                "灵感" -> showClipboardEditDialog(content, "灵感")
                "启发" -> showClipboardEditDialog(content, "启发")
                "待办" -> showClipboardEditDialog(content, "待办")
            }
        }

        dialog.show(supportFragmentManager, ContentTypeSelectionDialog.TAG)
    }

    /**
     * 显示编辑对话框（悬浮窗流程）
     */
    private fun showEditDialog() {
        // 获取传入的内容类型
        val contentType = intent.getStringExtra(TypeSelectionActivity.EXTRA_CONTENT_TYPE) ?: "灵感"

        // 获取剪贴板内容
        val clipboardContent = getClipboardContent()

        showClipboardEditDialog(clipboardContent, contentType)
    }

    /**
     * 获取剪贴板内容
     */
    private fun getClipboardContent(): String {
        return try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 显示剪贴板编辑对话框
     * 使用原来的 ClipboardEditDialogFragment(支持+号添加图片等功能)
     */
    private fun showClipboardEditDialog(initialContent: String, defaultTag: String) {
        isDismissedBySave = false

        val dialog = ClipboardEditDialogFragment.newInstance(
            initialContent = initialContent
        )

        dialog.setDefaultTag(defaultTag)

        dialog.setOnSaveListener { content, tags ->
            android.util.Log.d("EditActivity", ">>> onSaveListener 被调用, content长度=${content.length}, tags=$tags")
            // 标记为保存引起的关闭
            isDismissedBySave = true

            // 保存成功，关闭对话框
            try {
                android.util.Log.d("EditActivity", ">>> 准备 dismiss 对话框")
                dialog.dismissAllowingStateLoss()
                android.util.Log.d("EditActivity", ">>> 对话框已 dismiss")
            } catch (e: Exception) {
                android.util.Log.e("EditActivity", ">>> dismiss 失败", e)
                e.printStackTrace()
            }

            // 延迟关闭 Activity
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                android.util.Log.d("EditActivity", ">>> 准备 finish Activity")
                finish()
            }, 50)
        }

        dialog.setOnDismissListener {
            // 只有在非保存情况下才关闭 Activity
            if (!isDismissedBySave) {
                finish()
            }
        }

        dialog.show(supportFragmentManager, "ClipboardEditDialog")

        // 延迟聚焦到输入框
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            dialog.requestFocusAndShowKeyboard()
        }, 300)
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
