package com.jishi.clipboard.ui.dialog

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.jishi.clipboard.R
import com.jishi.clipboard.reminder.DateTimeExtractor
import com.jishi.clipboard.repository.ClipboardRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 剪贴板编辑对话框 - 微信风格底部弹出
 */
@AndroidEntryPoint
class ClipboardEditDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var repository: ClipboardRepository

    private lateinit var contentEditText: TextInputEditText
    private lateinit var tagsChipGroup: com.google.android.material.chip.ChipGroup
    private lateinit var customTagInput: TextInputEditText

    private val selectedTags = mutableSetOf<String>()
    private var onSaveListener: ((String, List<String>) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null

    private var editMode = false
    private var editClipboardId = -1L

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        // 设置圆角背景
        bottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 设置软键盘调整模式 - 调整大小而不是平移
        bottomSheetDialog.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // 可拖拽
        bottomSheetDialog.behavior.isDraggable = true

        // 设置展开状态
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                // 使用90%屏幕高度，保留可拖拽性
                behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.9).toInt()
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_clipboard_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupDefaultTags()
        handleArguments()
    }

    private fun initViews() {
        contentEditText = requireView().findViewById(R.id.contentEditText)
        tagsChipGroup = requireView().findViewById(R.id.tagsChipGroup)
        customTagInput = requireView().findViewById(R.id.customTagInput)
        val customTagInputLayout = requireView().findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.customTagInputLayout)

        // 保存按钮
        requireView().findViewById<View>(R.id.btnSave).setOnClickListener {
            saveContent()
        }

        // 取消按钮
        requireView().findViewById<View>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        // 自定义标签输入 - 监听软键盘完成按钮
        customTagInput.setOnEditorActionListener { _, _, _ ->
            addCustomTag()
            true
        }

        // 监听自定义标签输入框右侧的+号按钮
        customTagInputLayout?.setEndIconOnClickListener {
            addCustomTag()
        }
    }

    private fun setupDefaultTags() {
        val defaultTags = listOf("工作", "重要", "待办", "灵感", "学习", "生活")

        defaultTags.forEach { tagName ->
            val chip = createChip(tagName)
            tagsChipGroup.addView(chip)
        }
    }

    private fun createChip(tagName: String): Chip {
        return Chip(requireContext()).apply {
            text = tagName
            isCheckable = true
            isClickable = true

            // 统一椭圆风格
            chipCornerRadius = 16f

            // 设置关闭图标（删除按钮）
            isCloseIconVisible = false
            setCloseIconResource(android.R.drawable.ic_delete)
            closeIconSize = 36f
            setCloseIconTintResource(android.R.color.darker_gray)

            // 长按显示删除按钮
            setOnLongClickListener {
                isCloseIconVisible = true
                true
            }

            // 点击删除按钮
            setOnCloseIconClickListener {
                deleteTagFromGroup(this, tagName)
            }

            setOnCheckedChangeListener { _, isChecked ->
                synchronized(selectedTags) {
                    if (isChecked) {
                        selectedTags.add(tagName)
                        // 选中时：实心背景 + 白色文字
                        setChipBackgroundColorResource(R.color.primary)
                        setTextColor(android.graphics.Color.WHITE)
                    } else {
                        selectedTags.remove(tagName)
                        // 未选中时：透明背景 + 主题色文字
                        setChipBackgroundColorResource(android.R.color.transparent)
                        setTextColor(context.getColor(R.color.primary))
                        chipStrokeWidth = 0f
                    }
                }
            }

            // 初始化为未选中状态
            setChipBackgroundColorResource(android.R.color.transparent)
            setTextColor(context.getColor(R.color.primary))
            chipStrokeWidth = 0f
        }
    }

    private fun deleteTagFromGroup(chip: Chip, tagName: String) {
        synchronized(selectedTags) {
            selectedTags.remove(tagName)
        }
        tagsChipGroup.removeView(chip)
    }

    private fun addCustomTag() {
        val tagName = customTagInput.text.toString().trim()

        if (tagName.isNotEmpty() && !selectedTags.contains(tagName)) {
            val chip = createChip(tagName)
            chip.isChecked = true
            tagsChipGroup.addView(chip)
            customTagInput.text?.clear()
        }
    }

    private fun handleArguments() {
        editMode = arguments?.getBoolean(ARG_EDIT_MODE, false) ?: false
        editClipboardId = arguments?.getLong(ARG_EDIT_CLIPBOARD_ID, -1L) ?: -1L

        val initialContent = arguments?.getString(ARG_INITIAL_CONTENT)
        val autoFillClipboard = arguments?.getBoolean(ARG_AUTO_FILL_CLIPBOARD, false) ?: false

        when {
            editMode && editClipboardId != -1L -> {
                // 编辑模式：加载现有内容和标签
                contentEditText.setText(initialContent)
                contentEditText.setSelection(contentEditText.length())

                // 加载标签
                lifecycleScope.launch {
                    try {
                        val tags = repository.getTagsForClipboard(editClipboardId).first()
                        tags.forEach { tag ->
                            val chip = createChip(tag.name)
                            chip.isChecked = true
                            tagsChipGroup.addView(chip)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "加载标签失败")
                    }
                }
            }
            autoFillClipboard -> {
                // 从剪贴板读取
                val clipboardContent = readClipboard()
                contentEditText.setText(clipboardContent)
                contentEditText.setSelection(contentEditText.length())
                showKeyboard()
            }
            !initialContent.isNullOrEmpty() -> {
                // 使用传入的内容
                contentEditText.setText(initialContent)
                contentEditText.setSelection(contentEditText.length())
                showKeyboard()
            }
        }
    }

    private fun readClipboard(): String {
        return try {
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                clipboardManager.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            } else {
                @Suppress("DEPRECATION")
                clipboardManager.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            }
        } catch (e: Exception) {
            Timber.e(e, "读取剪贴板失败")
            ""
        }
    }

    private fun showKeyboard() {
        contentEditText.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(contentEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun saveContent() {
        val content = contentEditText.text?.toString()?.trim() ?: ""

        if (content.isEmpty()) {
            contentEditText.error = "内容不能为空"
            return
        }

        val tags = synchronized(selectedTags) { selectedTags.toList() }

        // 智能提取时间
        val extractedTime = DateTimeExtractor.extract(content)

        android.util.Log.d("ClipboardEdit", "提取时间结果: ${extractedTime != null}, timestamp=${extractedTime?.timestamp}, ${if (extractedTime != null) DateTimeExtractor.formatTimestamp(extractedTime.timestamp) else "null"}")

        // 检查是否有"待办"标签
        val hasTodoTag = tags.contains("待办")

        when {
            extractedTime != null -> {
                // 识别到时间，显示确认对话框
                showReminderConfirmation(content, tags, extractedTime)
            }
            hasTodoTag -> {
                // 有待办标签，使用待办提醒时间
                showTodoReminderConfirmation(content, tags)
            }
            else -> {
                // 直接保存
                saveToDatabase(content, tags)
            }
        }
    }

    private fun showReminderConfirmation(
        content: String,
        tags: List<String>,
        extractedTime: com.jishi.clipboard.reminder.DateTimeExtractor.ExtractedDateTime
    ) {
        android.util.Log.d("ClipboardEdit", "========== showReminderConfirmation 开始 ==========")
        android.util.Log.d("ClipboardEdit", "内容长度=${content.length}, 标签数量=${tags.size}")
        android.util.Log.d("ClipboardEdit", "提取时间=${extractedTime.originalText}, timestamp=${extractedTime.timestamp}")

        lifecycleScope.launch {
            try {
                android.util.Log.d("ClipboardEdit", ">>> 开始保存到数据库...")
                // 先保存到数据库
                val clipboardId = repository.saveClipboard(content, tags)
                android.util.Log.d("ClipboardEdit", ">>> 数据库保存完成, clipboardId=$clipboardId")

                // 检查 clipboardId 是否有效
                if (clipboardId <= 0) {
                    android.util.Log.e("ClipboardEdit", "❌ clipboardId 无效: $clipboardId")
                    Toast.makeText(requireContext(), "❌ 保存失败：ID错误", Toast.LENGTH_LONG).show()
                    return@launch
                }

                android.util.Log.d("ClipboardEdit", ">>> 开始创建提醒确认对话框...")

                // 使用 try-catch 包裹对话框创建过程
                try {
                    val reminderDialog = ReminderConfirmDialog.newInstance(
                        content = content,
                        extractedTime = extractedTime,
                        clipboardId = clipboardId
                    )
                    android.util.Log.d("ClipboardEdit", ">>> 对话框创建成功")

                    reminderDialog.setOnConfirmListener {
                        android.util.Log.d("ClipboardEdit", "用户确认提醒")
                        Toast.makeText(requireContext(), "✅ 已保存并设置提醒", Toast.LENGTH_SHORT).show()
                        onSaveListener?.invoke(content, tags)
                        dismiss()
                    }
                    android.util.Log.d("ClipboardEdit", ">>> 监听器设置完成")

                    android.util.Log.d("ClipboardEdit", ">>> 开始显示对话框...")
                    reminderDialog.show(childFragmentManager, "ReminderConfirm")
                    android.util.Log.d("ClipboardEdit", "========== 对话框显示成功 ==========")

                } catch (dialogError: Exception) {
                    android.util.Log.e("ClipboardEdit", "❌ 对话框创建/显示失败", dialogError)
                    Toast.makeText(requireContext(), "❌ 对话框显示失败: ${dialogError.message}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                android.util.Log.e("ClipboardEdit", "❌ 保存过程失败", e)
                Timber.e(e, "保存失败")
                Toast.makeText(requireContext(), "❌ 保存失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showTodoReminderConfirmation(content: String, tags: List<String>) {
        val reminderPrefs = com.jishi.clipboard.utils.ReminderPreferences(requireContext())

        if (!reminderPrefs.isTodoReminderEnabled()) {
            // 待办提醒未启用，直接保存
            saveToDatabase(content, tags)
            return
        }

        // 计算第二天的提醒时间
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, reminderPrefs.getTodoReminderHour())
        calendar.set(java.util.Calendar.MINUTE, reminderPrefs.getTodoReminderMinute())
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        val reminderTime = calendar.timeInMillis

        // 创建 ExtractedDateTime 对象
        val extractedTime = com.jishi.clipboard.reminder.DateTimeExtractor.ExtractedDateTime(
            timestamp = reminderTime,
            originalText = "明天 ${reminderPrefs.getFormattedReminderTime()}",
            confidence = 1.0f
        )

        lifecycleScope.launch {
            try {
                // 先保存到数据库
                val clipboardId = repository.saveClipboard(content, tags)

                // 显示提醒确认对话框
                val reminderDialog = ReminderConfirmDialog.newInstance(
                    content = content,
                    extractedTime = extractedTime,
                    clipboardId = clipboardId
                )

                reminderDialog.setOnConfirmListener {
                    Toast.makeText(requireContext(), "✅ 已保存并设置待办提醒", Toast.LENGTH_SHORT).show()
                    onSaveListener?.invoke(content, tags)
                    dismiss()
                }

                reminderDialog.show(childFragmentManager, "TodoReminderConfirm")

            } catch (e: Exception) {
                Timber.e(e, "保存失败")
                Toast.makeText(requireContext(), "❌ 保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToDatabase(content: String, tags: List<String>) {
        android.util.Log.d("ClipboardEdit", "saveToDatabase 开始: editMode=$editMode, clipboardId=$editClipboardId")
        android.util.Log.d("ClipboardEdit", "内容长度: ${content.length}, 标签数量: ${tags.size}")

        lifecycleScope.launch {
            try {
                if (editMode && editClipboardId != -1L) {
                    android.util.Log.d("ClipboardEdit", "执行更新操作")
                    repository.updateClipboard(editClipboardId, content, tags)
                    Toast.makeText(requireContext(), "✅ 已更新", Toast.LENGTH_SHORT).show()
                } else {
                    android.util.Log.d("ClipboardEdit", "执行新增操作")
                    repository.saveClipboard(content, tags)
                    Toast.makeText(requireContext(), "✅ 已保存", Toast.LENGTH_SHORT).show()
                }
                onSaveListener?.invoke(content, tags)
                dismiss()
            } catch (e: Exception) {
                android.util.Log.e("ClipboardEdit", "保存失败", e)
                Timber.e(e, "保存失败")
                Toast.makeText(requireContext(), "❌ 保存失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setOnSaveListener(listener: (String, List<String>) -> Unit) {
        onSaveListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    companion object {
        private const val ARG_INITIAL_CONTENT = "initial_content"
        private const val ARG_AUTO_FILL_CLIPBOARD = "auto_fill_clipboard"
        private const val ARG_EDIT_MODE = "edit_mode"
        private const val ARG_EDIT_CLIPBOARD_ID = "edit_clipboard_id"

        fun newInstance(
            initialContent: String = "",
            autoFillClipboard: Boolean = false,
            editMode: Boolean = false,
            editClipboardId: Long = -1L
        ): ClipboardEditDialogFragment {
            return ClipboardEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_INITIAL_CONTENT, initialContent)
                    putBoolean(ARG_AUTO_FILL_CLIPBOARD, autoFillClipboard)
                    putBoolean(ARG_EDIT_MODE, editMode)
                    putLong(ARG_EDIT_CLIPBOARD_ID, editClipboardId)
                }
            }
        }
    }
}
