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
import com.jishi.clipboard.data.TagDefinition
import com.jishi.clipboard.reminder.DateTimeExtractor
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.repository.TagRepository
import com.jishi.clipboard.utils.ClipboardUpdateEvent
import com.jishi.clipboard.utils.DialogManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

/**
 * 剪贴板编辑对话框 - 微信风格底部弹出
 */
@AndroidEntryPoint
class ClipboardEditDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    @Inject
    lateinit var tagRepository: TagRepository

    @Inject
    lateinit var todoRepository: com.jishi.clipboard.repository.TodoRepository

    @Inject
    lateinit var reminderScheduler: com.jishi.clipboard.reminder.ReminderScheduler

    private lateinit var contentEditText: TextInputEditText
    private lateinit var tagsChipGroup: com.google.android.material.chip.ChipGroup
    private lateinit var customTagInput: TextInputEditText

    private val selectedTags = mutableSetOf<String>()
    private var onSaveListener: ((String, List<String>) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null

    // 记录上次粘贴的内容，用于防重复检测
    private var lastPastedContent: String? = null

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

        // 订阅剪切板更新事件
        EventBus.getDefault().register(this)

        // 注册到 DialogManager
        DialogManager.setCurrentEditDialog(this)

        initViews()
        loadTagsFromDatabase()
        handleArguments()
    }

    override fun onDestroyView() {
        // 清空所有保存的状态
        DialogManager.clearState()

        EventBus.getDefault().unregister(this)
        DialogManager.clearCurrentEditDialog()
        lastPastedContent = null // 清空记录
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        // 不再恢复状态，每次都是初始状态
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClipboardUpdateEvent(event: ClipboardUpdateEvent) {
        val newContent = event.content

        // 记录初次剪切板内容hash
        if (DialogManager.lastClipboardHash == 0) {
            DialogManager.lastClipboardHash = newContent.hashCode()
        }

        // 检测是否为新内容
        if (newContent.isNotEmpty() && DialogManager.isNewClipboardContent(newContent)) {
            // 检测到新内容，清空对话框并重新添加
            showNewContentDetectedDialog(newContent)
        } else if (newContent.isNotEmpty()) {
            // 相同内容，正常追加
            appendClipboardContent(newContent)
        }
    }

    private fun showNewContentDetectedDialog(newContent: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("检测到新剪切板内容")
            .setMessage("剪切板已更新为新内容，是否清空当前对话框重新添加？\n\n新内容预览：\n${newContent.take(100)}${if(newContent.length > 100) "..." else ""}")
            .setPositiveButton("清空重新添加") { _, _ ->
                // 清空当前状态
                contentEditText.text?.clear()
                selectedTags.clear()
                tagsChipGroup.clearCheck()
                DialogManager.clearState()

                // 添加新内容
                appendClipboardContent(newContent)
            }
            .setNegativeButton("追加到当前内容") { _, _ ->
                // 追加到当前内容
                appendClipboardContent(newContent)
            }
            .setNeutralButton("取消", null)
            .show()
    }

    private fun appendClipboardContent(newContent: String) {
        // 防重复检测：如果与上次内容相同，询问用户
        if (newContent == lastPastedContent) {
            showDuplicatePasteDialog(newContent)
            return
        }

        // 记录本次粘贴的内容
        lastPastedContent = newContent

        val currentText = contentEditText.text?.toString() ?: ""

        // 构建新内容：当前文本 + 换行 + 新内容
        val updatedText = if (currentText.isEmpty()) {
            newContent
        } else {
            "$currentText\n$newContent"
        }

        // 更新内容（静默更新，无 Toast）
        contentEditText.setText(updatedText)

        // 定位光标到新内容末尾
        contentEditText.setSelection(updatedText.length)
    }

    private fun showDuplicatePasteDialog(content: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("重复粘贴")
            .setMessage("检测到与上次相同的内容，是否要重复粘贴？\n\n内容预览：\n${content.take(50)}${if(content.length > 50) "..." else ""}")
            .setPositiveButton("重复粘贴") { _, _ ->
                // 用户确认重复粘贴
                val currentText = contentEditText.text?.toString() ?: ""
                val updatedText = if (currentText.isEmpty()) {
                    content
                } else {
                    "$currentText\n$content"
                }
                contentEditText.setText(updatedText)
                contentEditText.setSelection(updatedText.length)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun loadTagsFromDatabase() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 初始化默认标签（如果数据库为空）
                tagRepository.initDefaultTagsIfNeeded()

                // 加载所有标签（✅ 过滤掉"待办"标签）
                tagRepository.getAllTagDefinitions().collect { tags ->
                    tags.filter { it.name != "待办" }.forEach { tagDef ->
                        val chip = createChip(tagDef.name)
                        tagsChipGroup.addView(chip)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "加载标签失败")
            }
        }
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

                        // ✅ 点击标签后显示子标签（不保存）
                        lifecycleScope.launch {
                            showChildTags(tagName)
                        }
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

    /**
     * 显示子标签（不保存，只导航）
     */
    private suspend fun showChildTags(parentTagName: String) {
        try {
            // 获取父标签
            val parentTag = tagRepository.getTagDefinitionByName(parentTagName)
            if (parentTag != null) {
                // 获取子标签
                val childTags = tagRepository.getChildTagsSync(parentTag.id)

                if (childTags.isNotEmpty()) {
                    // 清空当前标签显示
                    tagsChipGroup.removeAllViews()

                    // 显示子标签
                    childTags.forEach { childTag ->
                        val chip = createChip(childTag.name)
                        tagsChipGroup.addView(chip)
                    }

                    Toast.makeText(requireContext(), "📁 ${parentTagName} 的子标签", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "加载子标签失败")
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

        when {
            editMode && editClipboardId != -1L -> {
                // 编辑模式：加载现有内容和标签
                contentEditText.setText(initialContent)
                contentEditText.setSelection(contentEditText.length())

                // 加载标签
                lifecycleScope.launch {
                    try {
                        val tags = clipboardRepository.getTagsForClipboard(editClipboardId)
                        tags.forEach { tagDef ->
                            // 找到对应的 chip 并选中
                            val chipCount = tagsChipGroup.childCount
                            for (i in 0 until chipCount) {
                                val chip = tagsChipGroup.getChildAt(i) as? Chip
                                if (chip?.text == tagDef.name) {
                                    chip.isChecked = true
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "加载标签失败")
                    }
                }
            }
            !initialContent.isNullOrEmpty() -> {
                // 使用传入的内容
                contentEditText.setText(initialContent)
                contentEditText.setSelection(contentEditText.length())
            }
        }
    }

    private fun saveContent() {
        val content = contentEditText.text?.toString()?.trim() ?: ""

        if (content.isEmpty()) {
            contentEditText.error = "内容不能为空"
            return
        }

        val tags = synchronized(selectedTags) { selectedTags.toList() }

        // 检查是否有"待办"标签
        val hasTodoTag = tags.contains("待办")

        when {
            hasTodoTag -> {
                // 有"待办"标签，先保存剪贴板，然后显示提醒确认对话框
                lifecycleScope.launch {
                    try {
                        // 1. 保存剪贴板（✅ 过滤掉"待办"标签）
                        val filteredTags = tags.filter { it != "待办" }
                        val clipboardId = clipboardRepository.saveClipboard(content, filteredTags)

                        android.util.Log.d("ClipboardEdit", "保存剪贴板成功，clipboardId=$clipboardId，过滤后标签=$filteredTags")

                        // 2. 提取时间
                        val extractedTime = DateTimeExtractor.extract(content)

                        android.util.Log.d("ClipboardEdit", "待办标签 - 提取时间结果: ${extractedTime != null}, timestamp=${extractedTime?.timestamp}, ${if (extractedTime != null) DateTimeExtractor.formatTimestamp(extractedTime.timestamp) else "null"}")

                        // 3. 显示提醒确认对话框（如果没有提取到时间，使用默认时间）
                        showReminderConfirmationForTodo(clipboardId, content, extractedTime ?: createDefaultTime())

                    } catch (e: Exception) {
                        android.util.Log.e("ClipboardEdit", "保存失败", e)
                        Timber.e(e, "保存失败")
                        Toast.makeText(requireContext(), "❌ 保存失败: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> {
                // ✅ 非待办内容直接保存，不弹提醒对话框
                android.util.Log.d("ClipboardEdit", "非待办内容，直接保存")
                saveToDatabase(content, tags)
            }
        }
    }

    /**
     * 保存当前剪切板内容（从悬浮窗点击触发）
     */
    fun saveCurrentClipboardContent(content: String) {
        lifecycleScope.launch {
            try {
                val tags = synchronized(selectedTags) { selectedTags.toList() }

                // 检查是否有"待办"标签
                val hasTodoTag = tags.contains("待办")

                if (hasTodoTag) {
                    // 有"待办"标签，特殊处理
                    val filteredTags = tags.filter { it != "待办" }
                    val clipboardId = clipboardRepository.saveClipboard(content, filteredTags)

                    val extractedTime = DateTimeExtractor.extract(content)
                    showReminderConfirmationForTodo(clipboardId, content, extractedTime ?: createDefaultTime())
                } else {
                    // 普通保存
                    clipboardRepository.saveClipboard(content, tags)
                    Toast.makeText(requireContext(), "✅ 已保存", Toast.LENGTH_SHORT).show()

                    // 清空内容输入框，但保留已选标签
                    contentEditText.text?.clear()
                }
            } catch (e: Exception) {
                Timber.e(e, "保存剪切板内容失败")
                Toast.makeText(requireContext(), "❌ 保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 为待办事项显示提醒确认对话框
     */
    private fun showReminderConfirmationForTodo(
        clipboardId: Long,
        content: String,
        extractedTime: com.jishi.clipboard.reminder.DateTimeExtractor.ExtractedDateTime
    ) {
        android.util.Log.d("ClipboardEdit", "========== showReminderConfirmationForTodo 开始 ==========")
        android.util.Log.d("ClipboardEdit", "clipboardId=$clipboardId, 内容长度=${content.length}")
        android.util.Log.d("ClipboardEdit", "提取时间=${extractedTime.originalText}, timestamp=${extractedTime.timestamp}")

        lifecycleScope.launch {
            try {
                android.util.Log.d("ClipboardEdit", ">>> 开始创建提醒确认对话框...")

                val reminderDialog = ReminderConfirmDialog.newInstance(
                    content = content,
                    extractedTime = extractedTime,
                    clipboardId = clipboardId,
                    isTodoMode = true  // 标记为待办模式
                )
                android.util.Log.d("ClipboardEdit", ">>> 对话框创建成功")

                reminderDialog.setOnConfirmListener {
                    android.util.Log.d("ClipboardEdit", "用户确认提醒（待办模式）")
                    Toast.makeText(requireContext(), "✅ 已保存并设置提醒", Toast.LENGTH_SHORT).show()
                    // 触发保存监听器（由 EditActivity 负责 dismiss 和 finish）
                    onSaveListener?.invoke(content, listOf())
                }
                android.util.Log.d("ClipboardEdit", ">>> 监听器设置完成")

                android.util.Log.d("ClipboardEdit", ">>> 开始显示对话框...")
                reminderDialog.show(childFragmentManager, "ReminderConfirm")
                android.util.Log.d("ClipboardEdit", "========== 对话框显示成功 ==========")

            } catch (e: Exception) {
                android.util.Log.e("ClipboardEdit", "❌ 对话框创建/显示失败", e)
                Toast.makeText(requireContext(), "❌ 对话框显示失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 创建默认时间（明天上午9点）
     */
    private fun createDefaultTime(): com.jishi.clipboard.reminder.DateTimeExtractor.ExtractedDateTime {
        val calendar = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        android.util.Log.d("ClipboardEdit", "创建默认时间: ${DateTimeExtractor.formatTimestamp(calendar.timeInMillis)}")

        return com.jishi.clipboard.reminder.DateTimeExtractor.ExtractedDateTime(
            timestamp = calendar.timeInMillis,
            originalText = "默认时间（明天上午9点）",
            confidence = 0.5f
        )
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
                val clipboardId = clipboardRepository.saveClipboard(content, tags)
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
                        // 触发保存监听器（由 EditActivity 负责 dismiss 和 finish）
                        onSaveListener?.invoke(content, tags)
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

    private fun saveToDatabase(content: String, tags: List<String>) {
        android.util.Log.d("ClipboardEdit", "saveToDatabase 开始: editMode=$editMode, clipboardId=$editClipboardId")
        android.util.Log.d("ClipboardEdit", "内容长度: ${content.length}, 标签数量: ${tags.size}")

        lifecycleScope.launch {
            try {
                if (editMode && editClipboardId != -1L) {
                    android.util.Log.d("ClipboardEdit", "执行更新操作")
                    clipboardRepository.updateClipboard(editClipboardId, content, tags)
                    Toast.makeText(requireContext(), "✅ 已更新", Toast.LENGTH_SHORT).show()
                } else {
                    android.util.Log.d("ClipboardEdit", "执行新增操作")
                    clipboardRepository.saveClipboard(content, tags)
                    Toast.makeText(requireContext(), "✅ 已保存", Toast.LENGTH_SHORT).show()
                }

                // ✅ 保存后清空所有状态
                DialogManager.clearState()

                // 触发保存监听器（由 EditActivity 负责 dismiss 和 finish）
                onSaveListener?.invoke(content, tags)
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

    /**
     * 将剪贴板内容转换为待办事项
     */
    private suspend fun convertToTodo(
        clipboardId: Long,
        content: String,
        tags: List<com.jishi.clipboard.data.TagDefinition>
    ) {
        try {
            android.util.Log.d("ClipboardEdit", "========== convertToTodo 开始 ==========")
            android.util.Log.d("ClipboardEdit", "clipboardId=$clipboardId, 内容长度=${content.length}, 标签数量=${tags.size}")

            // 1. 解析内容
            val todoEntity = com.jishi.clipboard.parser.TodoParser.parse(content)

            if (todoEntity != null) {
                android.util.Log.d("ClipboardEdit", "✅ 解析成功: 任务=${todoEntity.task}, 时间=${todoEntity.dueTimestamp}")

                // 2. 创建待办事项
                val todoId = todoRepository.insertTodo(todoEntity)
                android.util.Log.d("ClipboardEdit", "✅ 创建待办 ID=$todoId")

                // 3. 如果有时间，创建提醒
                if (todoEntity.dueTimestamp != null &&
                    todoEntity.dueTimestamp!! > System.currentTimeMillis()) {

                    val reminder = com.jishi.clipboard.data.Reminder(
                        clipboardId = todoId,
                        timestamp = todoEntity.dueTimestamp!!,
                        type = "NOTIFICATION",
                        originalText = todoEntity.originalTimeText ?: "",
                        content = todoEntity.task,
                        isNotified = false
                    )

                    val success = reminderScheduler.schedule(reminder)
                    android.util.Log.d("ClipboardEdit", "✅ 提醒调度${if (success) "成功" else "失败"}")
                }

                // 4. 移除"待办"标签（防止重复转换）
                val updatedTags = tags.filter { it.name != "待办" }
                clipboardRepository.updateClipboardTags(clipboardId, updatedTags)
                android.util.Log.d("ClipboardEdit", "✅ 已移除「待办」标签")

                Toast.makeText(
                    requireContext(),
                    "✅ 已创建待办：${todoEntity.task}",
                    Toast.LENGTH_SHORT
                ).show()

                onSaveListener?.invoke(content, updatedTags.map { it.name })

            } else {
                android.util.Log.w("ClipboardEdit", "⚠️ 解析失败，保留剪贴板记录")
                Toast.makeText(
                    requireContext(),
                    "⚠️ 无法解析待办内容，已保存为普通剪贴板",
                    Toast.LENGTH_SHORT
                ).show()
                onSaveListener?.invoke(content, tags.map { it.name })
            }

        } catch (e: Exception) {
            android.util.Log.e("ClipboardEdit", "❌ 转换失败", e)
            Timber.e(e, "转换失败")
            Toast.makeText(
                requireContext(),
                "❌ 转换失败，已保存为普通剪贴板",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 粘贴内容到光标位置
     * 由悬浮窗点击触发
     * 粘贴后自动换行，光标保持在换行后
     */
    fun pasteToCursor(content: String) {
        try {
            // 检查 EditText 是否可用
            if (!::contentEditText.isInitialized || contentEditText.text == null) {
                Timber.w("EditText 未初始化或文本为空，无法粘贴")
                return
            }

            val selectionStart = contentEditText.selectionStart
            val selectionEnd = contentEditText.selectionEnd

            contentEditText.text?.let { editable ->
                // 确定粘贴位置：默认在光标位置，如果无效则在末尾
                val pastePosition = if (selectionStart >= 0) selectionStart else editable.length

                // 边界检查：确保位置不超过文本长度
                val validStart = Math.min(pastePosition, selectionEnd).coerceAtMost(editable.length)
                val validEnd = Math.min(Math.max(pastePosition, selectionEnd), editable.length)

                // 在光标位置替换选区或插入内容
                editable.replace(validStart, validEnd, content)

                // 在粘贴内容后添加换行
                val insertPosition = pastePosition + content.length
                editable.insert(insertPosition.coerceAtMost(editable.length), "\n")

                // 光标定位到换行后
                contentEditText.setSelection((insertPosition + 1).coerceAtMost(editable.length))
            }

            Timber.d("粘贴到光标位置: 位置=${contentEditText.selectionStart}, 内容=${content.take(30)}...")
        } catch (e: Exception) {
            Timber.e(e, "粘贴到光标位置失败")
        }
    }

    /**
     * 设置默认标签（根据类型选择）
     * 使用统一的标签创建逻辑，确保所有类型（灵感/启发/待办）行为一致
     */
    fun setDefaultTag(tagName: String) {
        lifecycleScope.launch {
            try {
                // 使用 getOrCreateContentTypeTag 确保标签存在
                val tagDef = tagRepository.getOrCreateContentTypeTag(tagName)

                selectedTags.add(tagName)
                // 创建并选中标签
                val chip = createChip(tagName)
                chip.isChecked = true
                tagsChipGroup.addView(chip)

                Timber.d("设置默认标签: $tagName, 颜色: ${tagDef.color}")
            } catch (e: Exception) {
                Timber.e(e, "设置默认标签失败: $tagName")
            }
        }
    }

    /**
     * 聚焦到输入框并显示键盘
     * 光标默认定位到文本末尾
     */
    fun requestFocusAndShowKeyboard() {
        try {
            contentEditText.requestFocus()

            // 延迟一小段时间，确保 EditText 完全准备好
            contentEditText.post {
                // 将光标定位到文本末尾
                val text = contentEditText.text?.toString() ?: ""
                val position = if (text.isNotEmpty()) text.length else 0
                contentEditText.setSelection(position)

                // 显示键盘
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(contentEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        } catch (e: Exception) {
            Timber.e(e, "显示键盘失败")
        }
    }

    companion object {
        private const val ARG_INITIAL_CONTENT = "initial_content"
        private const val ARG_EDIT_MODE = "edit_mode"
        private const val ARG_EDIT_CLIPBOARD_ID = "edit_clipboard_id"

        fun newInstance(
            initialContent: String = "",
            editMode: Boolean = false,
            editClipboardId: Long = -1L
        ): ClipboardEditDialogFragment {
            return ClipboardEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_INITIAL_CONTENT, initialContent)
                    putBoolean(ARG_EDIT_MODE, editMode)
                    putLong(ARG_EDIT_CLIPBOARD_ID, editClipboardId)
                }
            }
        }
    }
}
