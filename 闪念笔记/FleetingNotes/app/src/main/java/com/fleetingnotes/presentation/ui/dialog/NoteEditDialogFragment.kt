package com.fleetingnotes.presentation.ui.dialog

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.fleetingnotes.R
import com.fleetingnotes.ServiceLocator
import com.fleetingnotes.data.model.IdeaNote
import com.fleetingnotes.data.model.InsightNote
import com.fleetingnotes.data.model.InsightSource
import com.fleetingnotes.data.model.NoteType
import com.fleetingnotes.data.model.Priority
import com.fleetingnotes.data.model.TodoNote
import com.fleetingnotes.presentation.theme.FleetingNotesTheme
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.UUID
import kotlin.time.Duration.Companion.days

/**
 * 笔记编辑对话框 - 照搬及时记的 BottomSheetDialog 方式
 */
class NoteEditDialogFragment : DialogFragment() {

    private var noteType: NoteType = NoteType.IDEA
    private var onSaveListener: (() -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        // 设置圆角背景（照搬及时记）
        bottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 设置软键盘调整模式
        bottomSheetDialog.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // 可拖拽
        bottomSheetDialog.behavior.isDraggable = true

        // 照搬及时记：跳过半折叠状态，直接到展开状态
        bottomSheetDialog.behavior.skipCollapsed = true

        // 设置展开状态（照搬及时记：90% 屏幕高度）
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.9).toInt()
                behavior.isHideable = false  // 禁止向下滑动关闭
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return bottomSheetDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 获取传入的类型
        val typeStr = arguments?.getString(ARG_NOTE_TYPE) ?: "IDEA"
        noteType = try {
            NoteType.valueOf(typeStr)
        } catch (e: Exception) {
            NoteType.IDEA
        }
        Timber.d("NoteEditDialogFragment created with type: $noteType")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FleetingNotesTheme {
                    NoteEditDialogContent(
                        noteType = noteType,
                        onDismiss = { dismiss() },
                        onSave = { onSaveListener?.invoke() }
                    )
                }
            }
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    fun setOnSaveListener(listener: () -> Unit) {
        onSaveListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    companion object {
        private const val ARG_NOTE_TYPE = "note_type"

        fun newInstance(noteType: NoteType): NoteEditDialogFragment {
            return NoteEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOTE_TYPE, noteType.name)
                }
            }
        }
    }
}

/**
 * 笔记编辑内容 - 照搬及时记的方式读取剪切板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditDialogContent(
    noteType: NoteType,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = ServiceLocator.noteRepository

    // 照搬及时记：点击时读取剪切板（而非持续监听）
    val clipboardText = remember {
        try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString()
            } else null
        } catch (e: Exception) {
            Timber.e(e, "读取剪切板失败")
            null
        }
    }

    // 根据类型显示不同内容
    when (noteType) {
        NoteType.IDEA -> IdeaEditContent(
            repository = repository,
            clipboardText = clipboardText,
            onDismiss = onDismiss,
            onSave = onSave
        )
        NoteType.INSIGHT -> InsightEditContent(
            repository = repository,
            clipboardText = clipboardText,
            onDismiss = onDismiss,
            onSave = onSave
        )
        NoteType.TODO -> TodoEditContent(
            repository = repository,
            clipboardText = clipboardText,
            onDismiss = onDismiss,
            onSave = onSave
        )
    }
}

/**
 * 灵感编辑内容
 */
@Composable
private fun IdeaEditContent(
    repository: com.fleetingnotes.domain.repository.NoteRepository,
    clipboardText: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var content by remember { mutableStateOf(clipboardText ?: "") }
    var scene by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡 捕获灵感",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            TextButton(onClick = onDismiss) {
                Text("✕")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 灵感内容输入
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("这个灵感是什么？") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 场景输入
        OutlinedTextField(
            value = scene,
            onValueChange = { scene = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("📍 在什么场景下？（可选）") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 备注输入
        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            placeholder = { Text("补充说明（可选）") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF59E0B)
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 按钮组
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }

            Button(
                onClick = {
                    GlobalScope.launch {
                        try {
                            val now = Clock.System.now()
                            val note = IdeaNote(
                                id = UUID.randomUUID().toString(),
                                createdAt = now,
                                updatedAt = now,
                                content = content,
                                scene = scene.takeIf { it.isNotBlank() },
                                memo = memo.takeIf { it.isNotBlank() },
                                clipboardSources = emptyList()
                            )
                            repository.saveNote(note)
                            Timber.d("Idea note saved: ${note.id}")
                            onSave()
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to save idea note")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B)
                )
            ) {
                Text("保存")
            }
        }
    }
}

/**
 * 启发编辑内容
 */
@Composable
private fun InsightEditContent(
    repository: com.fleetingnotes.domain.repository.NoteRepository,
    clipboardText: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var selectedSource by remember { mutableStateOf<InsightSource?>(null) }
    var sourceDetail by remember { mutableStateOf(clipboardText ?: "") }
    var keyInsight by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }

    // 自动识别剪切板内容
    androidx.compose.runtime.LaunchedEffect(clipboardText) {
        if (clipboardText != null) {
            when {
                clipboardText.startsWith("http") -> {
                    selectedSource = InsightSource.WEB
                    sourceDetail = clipboardText
                }
                clipboardText.contains("《") -> {
                    selectedSource = InsightSource.BOOK
                    sourceDetail = clipboardText
                }
            }
        }
    }

    fun getSourceLabel(source: InsightSource): String = when (source) {
        InsightSource.BOOK -> "📚 书籍"
        InsightSource.PODCAST -> "🎙️ 播客"
        InsightSource.WEB -> "🌐 网络"
        InsightSource.CONVERSATION -> "💬 对话"
        InsightSource.COURSE -> "👨‍🏫 课程"
        InsightSource.OTHER -> "📝 其他"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📖 记录启发",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            TextButton(onClick = onDismiss) {
                Text("✕")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 来源选择
        Text(
            text = "来源",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InsightSource.entries.forEach { source ->
                FilterChip(
                    selected = selectedSource == source,
                    onClick = { selectedSource = source },
                    label = { Text(getSourceLabel(source)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3B82F6).copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 来源详情
        OutlinedTextField(
            value = sourceDetail,
            onValueChange = { sourceDetail = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("来源详情（可选）") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 关键启发
        OutlinedTextField(
            value = keyInsight,
            onValueChange = { keyInsight = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text("💎 关键启发是什么？") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3B82F6)
            ),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 补充内容
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            placeholder = { Text("详细阐述（可选）") },
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 关键词
        OutlinedTextField(
            value = keywords,
            onValueChange = { keywords = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("🏷️ 关键词（用空格分隔）") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 备注
        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            placeholder = { Text("补充说明（可选）") },
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 按钮组
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }

            Button(
                onClick = {
                    GlobalScope.launch {
                        try {
                            val now = Clock.System.now()
                            val note = InsightNote(
                                id = UUID.randomUUID().toString(),
                                createdAt = now,
                                updatedAt = now,
                                source = selectedSource,
                                sourceDetail = sourceDetail.takeIf { it.isNotBlank() },
                                keyInsight = keyInsight,
                                content = content,
                                memo = memo.takeIf { it.isNotBlank() },
                                keywords = keywords.takeIf { it.isNotBlank() }?.split(" ")?.filter { it.isNotEmpty() } ?: emptyList()
                            )
                            repository.saveNote(note)
                            Timber.d("Insight note saved: ${note.id}")
                            onSave()
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to save insight note")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                )
            ) {
                Text("保存")
            }
        }
    }
}

/**
 * 待办编辑内容
 */
@Composable
private fun TodoEditContent(
    repository: com.fleetingnotes.domain.repository.NoteRepository,
    clipboardText: String?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var content by remember { mutableStateOf(clipboardText ?: "") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedDateOption by remember { mutableStateOf<String?>(null) }
    var memo by remember { mutableStateOf("") }

    // 日期选项
    val today = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    val tomorrow = Clock.System.now().plus(1.days).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ 添加待办",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            TextButton(onClick = onDismiss) {
                Text("✕")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 任务内容
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = { Text("要做什么？") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFEF4444)
            ),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 优先级选择
        Text(
            text = "优先级",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Priority.values().forEach { priority ->
                FilterChip(
                    selected = selectedPriority == priority,
                    onClick = { selectedPriority = priority },
                    label = {
                        Text(
                            when (priority) {
                                Priority.HIGH -> "🔴 高"
                                Priority.MEDIUM -> "🟡 中"
                                Priority.LOW -> "🟢 低"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (priority) {
                            Priority.HIGH -> Color(0xFFEF4444)
                            Priority.MEDIUM -> Color(0xFFF59E0B)
                            Priority.LOW -> Color(0xFF10B981)
                        }.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 截止时间
        Text(
            text = "截止时间",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("今天", "明天", "无截止").forEach { option ->
                FilterChip(
                    selected = selectedDateOption == option,
                    onClick = {
                        selectedDateOption = if (selectedDateOption == option) null else option
                    },
                    label = { Text(option) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 备注
        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            placeholder = { Text("补充说明（可选）") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFEF4444)
            ),
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 按钮组
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }

            Button(
                onClick = {
                    GlobalScope.launch {
                        try {
                            val dueDate = when (selectedDateOption) {
                                "今天" -> today
                                "明天" -> tomorrow
                                else -> null
                            }
                            val now = Clock.System.now()
                            val note = TodoNote(
                                id = UUID.randomUUID().toString(),
                                createdAt = now,
                                updatedAt = now,
                                content = content,
                                priority = selectedPriority,
                                dueDate = dueDate,
                                dueTime = null,
                                memo = memo.takeIf { it.isNotBlank() }
                            )
                            repository.saveNote(note)
                            Timber.d("Todo note saved: ${note.id}")
                            onSave()
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to save todo note")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Text("保存")
            }
        }
    }
}
