package com.jishi.clipboard.ui.dialog

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.jishi.clipboard.R
import com.jishi.clipboard.reminder.DateTimeExtractor
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.ui.adapter.ImagePreviewAdapter
import com.jishi.clipboard.util.ImageUtils
import com.jishi.clipboard.utils.ClipboardUpdateEvent
import com.jishi.clipboard.utils.DialogManager
import com.jishi.clipboard.utils.PasteRequestEvent
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
    lateinit var todoRepository: com.jishi.clipboard.repository.TodoRepository

    @Inject
    lateinit var reminderScheduler: com.jishi.clipboard.reminder.ReminderScheduler

    private lateinit var contentEditText: TextInputEditText
    private lateinit var imagePreviewRecyclerView: RecyclerView
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter

    // 优先级选择控件
    private lateinit var priorityContainer: LinearLayout
    private lateinit var priorityRadioGroup: RadioGroup

    private val selectedImages = mutableListOf<String>()
    private var onSaveListener: ((String, List<String>) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null

    // 内容类型：灵感/启发/待办（与导航栏对应）
    private var contentType: String = "灵感"

    // 待应用的内容类型（在 onViewCreated 后应用）
    private var pendingContentType: String? = null

    // 优先级选择
    private var selectedPriority: String = "MEDIUM"  // 默认中优先级

    // 记录上次粘贴的内容，用于防重复检测
    private var lastPastedContent: String? = null

    private var editMode = false
    private var editClipboardId = -1L

    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris?.forEach { uri ->
            lifecycleScope.launch {
                try {
                    val imagePath = ImageUtils.saveImageToLocal(requireContext(), uri)
                    if (imagePath != null) {
                        selectedImages.add(imagePath)
                        updateImagePreview()
                    } else {
                        Toast.makeText(requireContext(), "图片保存失败", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "保存图片失败")
                    Toast.makeText(requireContext(), "图片保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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

    /**
     * 监听悬浮窗粘贴请求
     * 使用 EventBus 避免时序问题
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPasteRequestEvent(event: PasteRequestEvent) {
        Timber.d("收到粘贴请求事件: ${event.content.take(30)}...")

        // 延迟一小段时间确保 EditText 完全初始化
        contentEditText.postDelayed({
            pasteToCursor(event.content)
        }, 100)
    }

    private fun showNewContentDetectedDialog(newContent: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("检测到新剪切板内容")
            .setMessage("剪切板已更新为新内容，是否清空当前对话框重新添加？\n\n新内容预览：\n${newContent.take(100)}${if(newContent.length > 100) "..." else ""}")
            .setPositiveButton("清空重新添加") { _, _ ->
                // 清空当前状态
                contentEditText.text?.clear()
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

    private fun initViews() {
        contentEditText = requireView().findViewById(R.id.contentEditText)

        // NestedScrollView 会自动处理嵌套滚动，不需要额外设置
        contentEditText.setVerticalScrollBarEnabled(true)

        imagePreviewRecyclerView = requireView().findViewById(R.id.imagePreviewRecyclerView)

        // 优先级选择控件
        priorityContainer = requireView().findViewById(R.id.priorityContainer)
        priorityRadioGroup = requireView().findViewById(R.id.priorityRadioGroup)

        // 应用待定内容类型（如果有）
        applyContentTypeVisibility()

        // 初始化元数据上下文
        initMetadata()

        // 初始化图片预览适配器
        imagePreviewAdapter = ImagePreviewAdapter(
            onDeleteClick = { imagePath ->
                selectedImages.remove(imagePath)
                ImageUtils.deleteImage(imagePath)
                updateImagePreview()
            }
        )
        
        imagePreviewRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = imagePreviewAdapter
        }

        // 标签按钮 - 插入 # 符号
        requireView().findViewById<View>(R.id.btnTag).setOnClickListener {
            val cursor = contentEditText.selectionStart
            contentEditText.text?.insert(cursor, "#")
        }

        // 添加图片按钮
        requireView().findViewById<View>(R.id.btnAddImage).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // 更多按钮 - 打开格式工具弹窗
        requireView().findViewById<View>(R.id.btnMore).setOnClickListener {
            showFormatToolsDialog()
        }

        // 保存按钮
        requireView().findViewById<View>(R.id.btnSave).setOnClickListener {
            saveContent()
        }

        // 取消按钮
        requireView().findViewById<View>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        // 优先级选择监听
        priorityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedPriority = when (checkedId) {
                R.id.rbHigh -> "HIGH"
                R.id.rbMedium -> "MEDIUM"
                R.id.rbLow -> "LOW"
                else -> "MEDIUM"
            }
        }
    }
    
    private fun updateImagePreview() {
        if (selectedImages.isEmpty()) {
            imagePreviewRecyclerView.visibility = View.GONE
        } else {
            imagePreviewRecyclerView.visibility = View.VISIBLE
            imagePreviewAdapter.submitList(selectedImages.toList())
        }
    }

    /**
     * 显示格式工具弹窗
     */
    private fun showFormatToolsDialog() {
        try {
            // 先创建对话框实例
            val dialog = FormatToolsDialog()
            dialog.setOnToolSelected { tool ->
                when (tool) {
                    "quote" -> insertQuote()
                    "list" -> insertList()
                    "code" -> insertCodeBlock()
                    "divider" -> insertDivider()
                    "clear" -> clearFormat()
                    "voice" -> {
                        Toast.makeText(requireContext(), "语音功能即将推出", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // 使用 childFragmentManager 显示
            dialog.show(childFragmentManager, FormatToolsDialog.TAG)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "无法打开工具菜单: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 插入引用块
     */
    private fun insertQuote() {
        val cursor = contentEditText.selectionStart
        contentEditText.text?.insert(cursor, "> ")
        contentEditText.requestFocus()
    }

    /**
     * 插入列表
     */
    private fun insertList() {
        val cursor = contentEditText.selectionStart
        contentEditText.text?.insert(cursor, "- ")
        contentEditText.requestFocus()
    }

    /**
     * 插入代码块
     */
    private fun insertCodeBlock() {
        val cursor = contentEditText.selectionStart
        contentEditText.text?.insert(cursor, "```\n```\n")
        // 将光标移动到代码块中间
        contentEditText.setSelection(cursor + 4)
        contentEditText.requestFocus()
    }

    /**
     * 插入分割线
     */
    private fun insertDivider() {
        val cursor = contentEditText.selectionStart
        contentEditText.text?.insert(cursor, "\n---\n")
        contentEditText.requestFocus()
    }

    /**
     * 清空格式
     * 如果有选中文本，清除选中部分的格式
     * 如果没有选中文本，清除全部格式
     */
    private fun clearFormat() {
        val start = contentEditText.selectionStart
        val end = contentEditText.selectionEnd

        if (start != end) {
            // 有选中文本：清除选中文本的格式
            val text = contentEditText.text?.toString()?.substring(start, end) ?: ""
            val cleanText = text.replace(Regex("""[*_`#>\-\[\]()"""), "")
            contentEditText.text?.replace(start, end, cleanText)
        } else {
            // 无选中文本：清除全部格式
            val fullText = contentEditText.text?.toString() ?: ""
            val cleanText = fullText.replace(Regex("""[*_`#>\-\[\]()"""), "")
            contentEditText.setText(cleanText)
        }
        contentEditText.requestFocus()
    }

    private fun handleArguments() {
        editMode = arguments?.getBoolean(ARG_EDIT_MODE, false) ?: false
        editClipboardId = arguments?.getLong(ARG_EDIT_CLIPBOARD_ID, -1L) ?: -1L

        when {
            editMode && editClipboardId != -1L -> {
                // 编辑模式：从数据库加载现有内容
                lifecycleScope.launch {
                    try {
                        val clipboard = clipboardRepository.getClipboardById(editClipboardId)
                        if (clipboard != null) {
                            // 设置内容
                            contentEditText.setText(clipboard.content)
                            contentEditText.setSelection(contentEditText.length())

                            // 加载图片
                            clipboard.images?.let { imagesJson ->
                                val images = com.jishi.clipboard.util.ImageUtils.parseImagesFromJson(imagesJson)
                                selectedImages.clear()
                                selectedImages.addAll(images)
                                updateImagePreview()
                            }

                            // 解析并设置优先级
                            val metadataMap = try {
                                com.google.gson.Gson().fromJson(clipboard.metadata, Map::class.java) as? Map<String, Any>
                            } catch (e: Exception) {
                                null
                            }
                            val priority = metadataMap?.get("priority") as? String ?: "MEDIUM"
                            selectedPriority = priority
                            when (priority) {
                                "HIGH" -> priorityRadioGroup.check(R.id.rbHigh)
                                "MEDIUM" -> priorityRadioGroup.check(R.id.rbMedium)
                                "LOW" -> priorityRadioGroup.check(R.id.rbLow)
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "加载待办内容失败")
                    }
                }
            }
            else -> {
                // 新建模式：使用 initialContent（如果有的话）
                val initialContent = arguments?.getString(ARG_INITIAL_CONTENT)
                if (!initialContent.isNullOrEmpty()) {
                    contentEditText.setText(initialContent)
                    contentEditText.setSelection(contentEditText.length())
                }
            }
        }
    }

    private fun saveContent() {
        val content = contentEditText.text?.toString()?.trim() ?: ""

        if (content.isEmpty()) {
            contentEditText.error = "内容不能为空"
            return
        }

        // 从内容中提取标签
        val tags = extractTagsFromContent(content)

        // 检查内容类型是否为"待办"
        val isTodoType = contentType == "待办"

        android.util.Log.d("ClipboardEdit", "保存检查: contentType=$contentType, isTodoType=$isTodoType, tags=$tags")

        when {
            // 优先检查编辑模式
            editMode && editClipboardId != -1L -> {
                // 编辑模式：更新现有条目
                lifecycleScope.launch {
                    try {
                        // 读取优先级
                        val checkedId = priorityRadioGroup.checkedRadioButtonId
                        selectedPriority = when (checkedId) {
                            R.id.rbHigh -> "HIGH"
                            R.id.rbMedium -> "MEDIUM"
                            R.id.rbLow -> "LOW"
                            else -> "MEDIUM"
                        }

                        android.util.Log.d("ClipboardEdit", "编辑模式 - 更新待办，id=$editClipboardId, 优先级: $selectedPriority")

                        // 构建 metadata
                        val metadata = mapOf(
                            "priority" to selectedPriority,
                            "status" to "PENDING"
                        )

                        // 更新现有条目
                        clipboardRepository.updateClipboard(
                            id = editClipboardId,
                            content = content,
                            tags = tags,
                            type = contentType,
                            images = selectedImages,
                            metadata = com.google.gson.Gson().toJson(metadata)
                        )

                        android.util.Log.d("ClipboardEdit", "更新成功，id=$editClipboardId")

                        // 触发监听器并关闭对话框
                        onSaveListener?.invoke(content, tags)
                        safeDismissDialog()

                        // 提取时间并显示提醒确认（可选）
                        val extractedTime = DateTimeExtractor.extract(content)
                        if (extractedTime != null) {
                            showReminderConfirmationForTodo(editClipboardId, content, extractedTime)
                        }

                    } catch (e: Exception) {
                        android.util.Log.e("ClipboardEdit", "更新失败", e)
                        Timber.e(e, "更新失败")
                        Toast.makeText(requireContext(), "❌ 更新失败: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            isTodoType -> {
                // 新建模式：创建新待办
                lifecycleScope.launch {
                try {
                    // 1. 保存剪贴板（带类型和优先级）
                    // 注意：待办类型不过滤标签，直接使用 tags

                    // 保存前确保 selectedPriority 是最新值（从 RadioGroup 读取）
                    val checkedId = priorityRadioGroup.checkedRadioButtonId
                    selectedPriority = when (checkedId) {
                        R.id.rbHigh -> "HIGH"
                        R.id.rbMedium -> "MEDIUM"
                        R.id.rbLow -> "LOW"
                        else -> "MEDIUM"
                    }

                    android.util.Log.d("ClipboardEdit", "新建模式 - 保存待办，优先级: $selectedPriority, RadioGroup选中ID: $checkedId")

                    // 构建 metadata，包含优先级
                    val metadata = mapOf(
                        "priority" to selectedPriority,
                        "status" to "PENDING"
                    )

                    val clipboardId = clipboardRepository.saveClipboard(
                        content = content,
                        tags = tags,  // 待办类型不过滤标签
                        type = contentType,
                        images = selectedImages,
                        metadata = com.google.gson.Gson().toJson(metadata)
                    )

                        android.util.Log.d("ClipboardEdit", "保存剪贴板成功，clipboardId=$clipboardId，标签=$tags")

                        // 先触发保存监听器并关闭当前对话框
                        onSaveListener?.invoke(content, tags)
                        try {
                            dismiss()
                        } catch (e: Exception) {
                            android.util.Log.e("ClipboardEdit", "dismiss失败", e)
                        }

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
                // 从内容中提取标签
                val tags = extractTagsFromContent(content)

                // 检查内容类型是否为"待办"
                val isTodoType = contentType == "待办"

                android.util.Log.d("ClipboardEdit", "快速保存检查: contentType=$contentType, isTodoType=$isTodoType")

                if (isTodoType) {
                    // 待办类型，保存优先级

                    // 保存前确保 selectedPriority 是最新值（从 RadioGroup 读取）
                    val checkedId = priorityRadioGroup.checkedRadioButtonId
                    selectedPriority = when (checkedId) {
                        R.id.rbHigh -> "HIGH"
                        R.id.rbMedium -> "MEDIUM"
                        R.id.rbLow -> "LOW"
                        else -> "MEDIUM"
                    }

                    android.util.Log.d("ClipboardEdit", "快速保存待办，优先级: $selectedPriority, RadioGroup选中ID: $checkedId")

                    // 构建 metadata，包含优先级
                    val metadata = mapOf(
                        "priority" to selectedPriority,
                        "status" to "PENDING"
                    )

                    val clipboardId = clipboardRepository.saveClipboard(
                        content = content,
                        tags = tags,  // 待办类型不过滤标签
                        type = contentType,
                        images = selectedImages,
                        metadata = com.google.gson.Gson().toJson(metadata)
                    )

                    val extractedTime = DateTimeExtractor.extract(content)
                    showReminderConfirmationForTodo(clipboardId, content, extractedTime ?: createDefaultTime())
                } else {
                    // 普通保存（带类型）
                    clipboardRepository.saveClipboard(content, tags, contentType, selectedImages)
                    Toast.makeText(requireContext(), "✅ 已保存到【$contentType】", Toast.LENGTH_SHORT).show()

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
                // 先保存到数据库（带类型）
                val clipboardId = clipboardRepository.saveClipboard(content, tags, contentType, selectedImages)
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
        android.util.Log.d("ClipboardEdit", "========== saveToDatabase 开始 ==========")
        android.util.Log.d("ClipboardEdit", "editMode=$editMode, clipboardId=$editClipboardId")
        android.util.Log.d("ClipboardEdit", "内容长度: ${content.length}, 标签数量: ${tags.size}, 图片数量: ${selectedImages.size}")
        android.util.Log.d("ClipboardEdit", "标签列表: $tags")
        android.util.Log.d("ClipboardEdit", "内容类型: $contentType")

        lifecycleScope.launch {
            try {
                if (editMode && editClipboardId != -1L) {
                    android.util.Log.d("ClipboardEdit", "执行更新操作")
                    clipboardRepository.updateClipboard(editClipboardId, content, tags, contentType, selectedImages)
                    Toast.makeText(requireContext(), "✅ 已更新", Toast.LENGTH_SHORT).show()
                } else {
                    android.util.Log.d("ClipboardEdit", "执行新增操作，类型=$contentType")
                    val savedId = clipboardRepository.saveClipboard(content, tags, contentType, selectedImages)
                    android.util.Log.d("ClipboardEdit", "保存成功！返回 ID: $savedId")
                    Toast.makeText(requireContext(), "✅ 已保存到【$contentType】(ID:$savedId)", Toast.LENGTH_SHORT).show()
                }

                // ✅ 保存后清空所有状态
                DialogManager.clearState()

                android.util.Log.d("ClipboardEdit", "触发保存监听器")
                // 触发保存监听器（由 EditActivity 负责 dismiss 和 finish）
                onSaveListener?.invoke(content, tags)
                android.util.Log.d("ClipboardEdit", "========== saveToDatabase 完成 ==========")
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

                android.util.Log.d("ClipboardEdit", "✅ 已创建待办，标签从内容解析")

                Toast.makeText(
                    requireContext(),
                    "✅ 已创建待办：${todoEntity.task}",
                    Toast.LENGTH_SHORT
                ).show()

                onSaveListener?.invoke(content, emptyList()) // 标签从内容解析

            } else {
                android.util.Log.w("ClipboardEdit", "⚠️ 解析失败，保留剪贴板记录")
                Toast.makeText(
                    requireContext(),
                    "⚠️ 无法解析待办内容，已保存为普通剪贴板",
                    Toast.LENGTH_SHORT
                ).show()
                onSaveListener?.invoke(content, emptyList()) // 标签从内容解析
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
     * 注意：这里设置的是内容类型，不是标签！
     */
    fun setDefaultTag(typeName: String) {
        // 保存内容类型
        contentType = typeName
        pendingContentType = typeName

        // 尝试立即应用，如果视图已创建
        applyContentTypeVisibility()
    }

    /**
     * 应用内容类型的可见性设置
     */
    private fun applyContentTypeVisibility() {
        if (!::priorityContainer.isInitialized) return

        try {
            val typeName = pendingContentType ?: contentType
            // 如果是待办类型，显示优先级选择
            if (typeName == "待办") {
                priorityContainer.visibility = View.VISIBLE
            } else {
                priorityContainer.visibility = View.GONE
            }
            Timber.d("设置内容类型: $typeName, 优先级选择${if (typeName == "待办") "显示" else "隐藏"}")
        } catch (e: Exception) {
            Timber.e(e, "设置内容类型失败")
        }
    }

    /**
     * 初始化元数据上下文（时间 + 来源）
     */
    private fun initMetadata() {
        try {
            val timeText = requireView().findViewById<android.widget.TextView>(R.id.timeText)
            val sourceText = requireView().findViewById<android.widget.TextView>(R.id.sourceText)

            // 格式化时间：显示为"今天 14:30"格式
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)
            timeText.text = "📅 今天 ${String.format("%02d:%02d", hour, minute)}"

            // 根据是否来自剪贴板显示来源
            val initialContent = arguments?.getString(ARG_INITIAL_CONTENT, "") ?: ""
            if (initialContent.isNotEmpty()) {
                sourceText.visibility = android.view.View.VISIBLE
                sourceText.text = "📺 来自复制"
            } else {
                sourceText.visibility = android.view.View.GONE
            }

            Timber.d("元数据初始化完成：${timeText.text}, 来源=${if(sourceText.visibility == android.view.View.VISIBLE) sourceText.text else "无"}")
        } catch (e: Exception) {
            Timber.e(e, "元数据初始化失败")
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

    /**
     * 安全关闭对话框
     */
    private fun safeDismissDialog() {
        try {
            dismiss()
        } catch (e: Exception) {
            android.util.Log.e("ClipboardEdit", "关闭对话框失败", e)
            Timber.e(e, "关闭对话框失败")
        }
    }

    /**
     * 从内容中提取标签
     * 匹配 #标签 格式
     */
    private fun extractTagsFromContent(content: String): List<String> {
        val tagPattern = Regex("#([\\u4e00-\\u9fa5a-zA-Z0-9_]+)")
        return tagPattern.findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
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
