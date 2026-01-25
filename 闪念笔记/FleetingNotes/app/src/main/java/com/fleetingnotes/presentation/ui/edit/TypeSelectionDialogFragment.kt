package com.fleetingnotes.presentation.ui.edit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.fleetingnotes.R
import com.fleetingnotes.data.model.NoteType
import com.google.android.material.bottomsheet.BottomSheetDialog
import timber.log.Timber

/**
 * 类型选择对话框 - 照搬及时记的 BottomSheetDialog 方式
 * 使用 XML 布局而非 Compose，避免兼容性问题
 */
class TypeSelectionDialogFragment : DialogFragment() {

    private var onTypeSelectedListener: ((NoteType) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Timber.d("onCreateDialog: creating BottomSheetDialog")
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        // 设置圆角背景（照搬及时记）
        bottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 设置软键盘调整模式
        bottomSheetDialog.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // 可拖拽
        bottomSheetDialog.behavior.isDraggable = true

        // 照搬及时记：设置展开状态（90% 屏幕高度）
        bottomSheetDialog.setOnShowListener {
            Timber.d("onCreateDialog: onShowListener called")
            try {
                val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let {
                    Timber.d("onCreateDialog: bottomSheet view found")
                    val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                    behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.9).toInt()
                    behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                    Timber.d("onCreateDialog: behavior state set to EXPANDED")
                } ?: Timber.w("onCreateDialog: bottomSheet view is null")
            } catch (e: Exception) {
                Timber.e(e, "onCreateDialog: error setting up bottom sheet behavior")
            }
        }

        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView: inflating dialog_type_selection layout")
        // 使用 XML 布局
        return inflater.inflate(R.layout.dialog_type_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated: setting up button listeners")

        try {
            // 灵感按钮
            view.findViewById<View>(R.id.btnIdea).setOnClickListener {
                Timber.d("btnIdea clicked")
                onTypeSelectedListener?.invoke(NoteType.IDEA)
                dismiss()
            }

            // 启发按钮
            view.findViewById<View>(R.id.btnInsight).setOnClickListener {
                Timber.d("btnInsight clicked")
                onTypeSelectedListener?.invoke(NoteType.INSIGHT)
                dismiss()
            }

            // 待办按钮
            view.findViewById<View>(R.id.btnTodo).setOnClickListener {
                Timber.d("btnTodo clicked")
                onTypeSelectedListener?.invoke(NoteType.TODO)
                dismiss()
            }

            // 取消按钮
            view.findViewById<View>(R.id.btnCancel).setOnClickListener {
                Timber.d("btnCancel clicked")
                dismiss()
            }
            Timber.d("onViewCreated: all button listeners set up successfully")
        } catch (e: Exception) {
            Timber.e(e, "onViewCreated: error setting up buttons")
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        Timber.d("onDismiss: dialog dismissed")
        onDismissListener?.invoke()
    }

    fun setOnTypeSelectedListener(listener: (NoteType) -> Unit) {
        onTypeSelectedListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    companion object {
        fun newInstance(): TypeSelectionDialogFragment {
            return TypeSelectionDialogFragment()
        }
    }
}
