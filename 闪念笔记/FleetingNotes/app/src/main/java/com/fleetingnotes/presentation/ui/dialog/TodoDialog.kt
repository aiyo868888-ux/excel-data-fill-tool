package com.fleetingnotes.presentation.ui.dialog
import kotlinx.datetime.LocalTime

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fleetingnotes.data.model.Priority
import com.fleetingnotes.presentation.theme.Red500
import com.fleetingnotes.presentation.theme.Yellow500
import com.fleetingnotes.presentation.theme.Green500
import com.fleetingnotes.presentation.ui.components.ImageData
import com.fleetingnotes.presentation.ui.components.ImagePicker
import com.fleetingnotes.presentation.ui.components.VoiceInput
import timber.log.Timber
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

/**
 * 待办对话框状态
 */
data class TodoDialogState(
    val content: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val memo: String = "",
    val clipboardText: String? = null,
    val images: List<ImageData> = emptyList()
)

/**
 * 待办对话框 - 用于记录待办事项
 */
@Composable
fun TodoDialog(
    state: TodoDialogState = TodoDialogState(),
    onDismiss: () -> Unit = {},
    onSave: (TodoDialogState) -> Unit = {},
    onSaveAndContinue: (TodoDialogState) -> Unit = {}
) {
    var content by remember { mutableStateOf(state.content) }
    var selectedPriority by remember { mutableStateOf(state.priority) }
    var selectedDateOption by remember { mutableStateOf<DateOption?>(null) }
    var selectedTime by remember { mutableStateOf("全天") }
    var memo by remember { mutableStateOf(state.memo) }
    var images by remember { mutableStateOf(state.images) }
    var showClipboardPreview by remember { mutableStateOf(state.clipboardText != null) }

    // 日期选项 - 简单实现
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val tomorrow = Clock.System.now().plus(1.days).toLocalDateTime(TimeZone.currentSystemDefault()).date

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 主要内容
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 语音输入
                    VoiceInput(
                        onTextRecognized = { text ->
                            content = if (content.isBlank()) text else "$content\n$text"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 任务内容
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("要做什么？") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Red500
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 优先级选择
                    Text(
                        text = "优先级",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
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
                                        Priority.HIGH -> Red500
                                        Priority.MEDIUM -> Yellow500
                                        Priority.LOW -> Green500
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
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DateOption.entries.forEach { option ->
                            FilterChip(
                                selected = selectedDateOption == option,
                                onClick = { selectedDateOption = option },
                                label = {
                                    Text(
                                        when (option) {
                                            DateOption.TODAY -> "今天"
                                            DateOption.TOMORROW -> "明天"
                                            DateOption.CUSTOM -> "选日期"
                                            null -> "无截止"
                                        }
                                    )
                                }
                            )
                        }
                    }

                    // 时间选择（简化版）
                    if (selectedDateOption != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("全天", "上午", "下午", "晚上").forEach { time ->
                                FilterChip(
                                    selected = selectedTime == time,
                                    onClick = { selectedTime = time },
                                    label = { Text(time) }
                                )
                            }
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
                            focusedBorderColor = Red500
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 图片选择
                    Text(
                        text = "📷 添加图片（可选）",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ImagePicker(
                        images = images,
                        onImagesSelected = { newImages ->
                            images = images + newImages
                        },
                        onImageRemoved = { imageToRemove ->
                            images = images.filterNot { it == imageToRemove }
                        },
                        maxImages = 3
                    )

                    // 剪切板预览
                    if (showClipboardPreview && state.clipboardText != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Red500.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "📋 剪切板内容",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    TextButton(onClick = {
                                        content = state.clipboardText
                                        showClipboardPreview = false
                                    }) {
                                        Text("粘贴")
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.clipboardText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.ui.graphics.Color.Gray,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 按钮组
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 保存按钮
                        OutlinedButton(
                            onClick = {
                                val dueDate = when (selectedDateOption) {
                                    DateOption.TODAY -> today
                                    DateOption.TOMORROW -> tomorrow
                                    DateOption.CUSTOM, null -> null
                                }
                                onSave(
                                    TodoDialogState(
                                        content = content,
                                        priority = selectedPriority,
                                        dueDate = dueDate,
                                        memo = memo,
                                        images = images
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("保存")
                        }

                        // 保存并继续记录按钮
                        Button(
                            onClick = {
                                val dueDate = when (selectedDateOption) {
                                    DateOption.TODAY -> today
                                    DateOption.TOMORROW -> tomorrow
                                    DateOption.CUSTOM, null -> null
                                }
                                onSaveAndContinue(
                                    TodoDialogState(
                                        content = content,
                                        priority = selectedPriority,
                                        dueDate = dueDate,
                                        memo = memo,
                                        images = images
                                    )
                                )
                                content = ""
                                memo = ""
                                images = emptyList()
                                selectedPriority = Priority.MEDIUM
                                selectedDateOption = null
                                selectedTime = "全天"
                                Timber.d("Saved and continuing...")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Red500
                            )
                        ) {
                            Text("保存并继续")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 日期选项
 */
enum class DateOption {
    TODAY,
    TOMORROW,
    CUSTOM
}
