package com.fleetingnotes.presentation.ui.edit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fleetingnotes.BuildConfig
import com.fleetingnotes.R
import com.fleetingnotes.data.model.NoteType
import timber.log.Timber

/**
 * 类型选择 Activity - 透明对话框
 *
 * 点击悬浮窗后弹出，显示三个类型选项
 */
class TypeSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 Timber（如果还没有初始化）
        if (BuildConfig.DEBUG) {
            timber.log.Timber.plant(timber.log.Timber.DebugTree())
        }

        Timber.d("========== TypeSelectionActivity onCreate ==========")
        try {
            setContentView(R.layout.activity_transparent) // 空布局
            Timber.d("setContentView succeeded")

            // 显示类型选择对话框
            showTypeSelectionDialog()
        } catch (e: Exception) {
            Timber.e(e, "Error in TypeSelectionActivity onCreate: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showTypeSelectionDialog() {
        Timber.d("showTypeSelectionDialog() called")
        try {
            val dialog = TypeSelectionDialogFragment()

            dialog.setOnTypeSelectedListener { noteType ->
                Timber.d("Type selected: $noteType")
                // 启动编辑对话框
                val intent = Intent(this, NoteEditActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                    putExtra(NoteEditActivity.EXTRA_NOTE_TYPE, noteType.name)
                }
                startActivity(intent)

                // 关闭类型选择对话框
                finish()
            }

            dialog.setOnDismissListener {
                Timber.d("TypeSelectionDialog dismissed")
                // 对话框关闭时也关闭 Activity
                finish()
            }

            // 显示对话框
            Timber.d("About to show TypeSelectionDialog")
            dialog.show(supportFragmentManager, "TypeSelection")
            Timber.d("TypeSelectionDialog shown successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error in showTypeSelectionDialog: ${e.message}")
            e.printStackTrace()
            finish()
        }
    }

    override fun onBackPressed() {
        // 关闭对话框
        val dialog = supportFragmentManager.findFragmentByTag("TypeSelection") as? TypeSelectionDialogFragment
        dialog?.dismiss()

        super.onBackPressed()
    }
}
