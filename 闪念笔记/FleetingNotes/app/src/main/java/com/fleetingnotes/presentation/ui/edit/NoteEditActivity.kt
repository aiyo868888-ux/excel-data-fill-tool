package com.fleetingnotes.presentation.ui.edit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fleetingnotes.BuildConfig
import com.fleetingnotes.R
import com.fleetingnotes.presentation.ui.dialog.NoteEditDialogFragment
import com.fleetingnotes.data.model.NoteType
import timber.log.Timber

/**
 * 编辑 Activity - 透明对话框
 *
 * 照搬及时记的方式：
 * - 使用透明 Activity 作为容器
 * - 显示 BottomSheetDialogFragment
 * - 对话框关闭时自动关闭 Activity
 */
class NoteEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 Timber（如果还没有初始化）
        if (BuildConfig.DEBUG) {
            timber.log.Timber.plant(timber.log.Timber.DebugTree())
        }

        Timber.d("NoteEditActivity onCreate")
        setContentView(R.layout.activity_transparent) // 空布局

        // 获取传入的类型
        val noteTypeStr = intent.getStringExtra(EXTRA_NOTE_TYPE)
        Timber.d("Received noteType: $noteTypeStr")
        val noteType = try {
            NoteType.valueOf(noteTypeStr ?: "IDEA")
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse note type, defaulting to IDEA")
            NoteType.IDEA
        }

        Timber.d("Showing dialog for type: $noteType")
        // 显示对话框
        showEditDialog(noteType)
    }

    private fun showEditDialog(noteType: NoteType) {
        Timber.d("showEditDialog() called with type: $noteType")
        val dialog = NoteEditDialogFragment.newInstance(noteType)

        dialog.setOnSaveListener {
            Timber.d(" onSave triggered")
            // 保存成功后关闭 Activity
            finish()
        }

        dialog.setOnDismissListener {
            Timber.d("onDismiss triggered")
            // 对话框关闭时也关闭 Activity
            finish()
        }

        // 显示对话框
        Timber.d("About to show dialog")
        dialog.show(supportFragmentManager, "NoteEditDialog")
        Timber.d("Dialog shown successfully")
    }

    override fun onBackPressed() {
        // 关闭对话框
        val dialog = supportFragmentManager.findFragmentByTag("NoteEditDialog") as? NoteEditDialogFragment
        dialog?.dismiss()

        super.onBackPressed()
    }

    companion object {
        const val EXTRA_NOTE_TYPE = "note_type"
    }
}
