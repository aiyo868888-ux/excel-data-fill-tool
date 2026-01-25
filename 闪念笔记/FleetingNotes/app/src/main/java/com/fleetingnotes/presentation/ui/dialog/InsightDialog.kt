package com.fleetingnotes.presentation.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fleetingnotes.data.model.InsightSource
import com.fleetingnotes.presentation.theme.Blue500
import com.fleetingnotes.presentation.ui.components.ImageData
import com.fleetingnotes.presentation.ui.components.ImagePicker
import com.fleetingnotes.presentation.ui.components.VoiceInput
import timber.log.Timber
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * 启发对话框状态
 */
data class InsightDialogState(
    val source: InsightSource? = null,
    val sourceDetail: String = "",
    val keyInsight: String = "",
    val content: String = "",
    val memo: String = "",
    val keywords: String = "",
    val clipboardText: String? = null,
    val images: List<ImageData> = emptyList()
)

/**
 * 获取来源图标
 */
private fun getSourceIcon(source: InsightSource): String = when (source) {
    InsightSource.BOOK -> "📚"
    InsightSource.PODCAST -> "🎙️"
    InsightSource.WEB -> "🌐"
    InsightSource.CONVERSATION -> "💬"
    InsightSource.COURSE -> "👨‍🏫"
    InsightSource.OTHER -> "📝"
}

/**
 * 获取来源标签
 */
private fun getSourceLabel(source: InsightSource): String = when (source) {
    InsightSource.BOOK -> "书籍"
    InsightSource.PODCAST -> "播客"
    InsightSource.WEB -> "网络"
    InsightSource.CONVERSATION -> "对话"
    InsightSource.COURSE -> "课程"
    InsightSource.OTHER -> "其他"
}

/**
 * 启发对话框 - 用于记录启发式思考
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightDialog(
    state: InsightDialogState = InsightDialogState(),
    onDismiss: () -> Unit = {},
    onSave: (InsightDialogState) -> Unit = {},
    onSaveAndContinue: (InsightDialogState) -> Unit = {}
) {
    var selectedSource by remember { mutableStateOf(state.source) }
    var sourceDetail by remember { mutableStateOf(state.sourceDetail) }
    var keyInsight by remember { mutableStateOf(state.keyInsight) }
    var content by remember { mutableStateOf(state.content) }
    var memo by remember { mutableStateOf(state.memo) }
    var keywords by remember { mutableStateOf(state.keywords) }
    var images by remember { mutableStateOf(state.images) }
    var showClipboardPreview by remember { mutableStateOf(state.clipboardText != null) }
    var showSourceMenu by remember { mutableStateOf(false) }

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
                        text = "📖 记录启发",
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
                    // 来源选择
                    ExposedDropdownMenuBox(
                        expanded = showSourceMenu,
                        onExpandedChange = { showSourceMenu = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSource?.let { getSourceLabel(it) } ?: "选择来源",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            placeholder = { Text("来自哪里？") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSourceMenu)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue500
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = showSourceMenu,
                            onDismissRequest = { showSourceMenu = false }
                        ) {
                            InsightSource.entries.forEach { source ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(getSourceIcon(source), modifier = Modifier.padding(end = 8.dp))
                                            Text(getSourceLabel(source))
                                        }
                                    },
                                    onClick = {
                                        selectedSource = source
                                        showSourceMenu = false
                                        // 自动识别 URL 或书名
                                        if (state.clipboardText != null) {
                                            when {
                                                state.clipboardText.startsWith("http") -> {
                                                    sourceDetail = state.clipboardText
                                                }
                                                state.clipboardText.contains("《") -> {
                                                    sourceDetail = state.clipboardText
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 来源详情
                    OutlinedTextField(
                        value = sourceDetail,
                        onValueChange = { sourceDetail = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                when (selectedSource) {
                                    InsightSource.BOOK -> "书名、作者、章节"
                                    InsightSource.PODCAST -> "播客名称、集数"
                                    InsightSource.WEB -> "URL 或文章标题"
                                    InsightSource.CONVERSATION -> "与谁交流"
                                    InsightSource.COURSE -> "课程名称、讲师"
                                    null -> "来源详情（可选）"
                                    else -> "详情"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "来源详情"
                            )
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 语音输入
                    VoiceInput(
                        onTextRecognized = { text ->
                            keyInsight = if (keyInsight.isBlank()) text else "$keyInsight\n$text"
                        },
                        modifier = Modifier.fillMaxWidth()
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
                            focusedBorderColor = Blue500
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
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = "关键词"
                            )
                        },
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
                                containerColor = Blue500.copy(alpha = 0.1f)
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
                                onSave(
                                    InsightDialogState(
                                        source = selectedSource,
                                        sourceDetail = sourceDetail,
                                        keyInsight = keyInsight,
                                        content = content,
                                        memo = memo,
                                        keywords = keywords,
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
                                onSaveAndContinue(
                                    InsightDialogState(
                                        source = selectedSource,
                                        sourceDetail = sourceDetail,
                                        keyInsight = keyInsight,
                                        content = content,
                                        memo = memo,
                                        keywords = keywords,
                                        images = images
                                    )
                                )
                                keyInsight = ""
                                content = ""
                                memo = ""
                                keywords = ""
                                images = emptyList()
                                Timber.d("Saved and continuing...")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue500
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
