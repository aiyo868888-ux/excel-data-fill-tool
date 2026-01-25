package com.fleetingnotes.presentation.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fleetingnotes.presentation.theme.Yellow500
import com.fleetingnotes.presentation.ui.components.ImageData
import com.fleetingnotes.presentation.ui.components.ImagePicker
import com.fleetingnotes.presentation.ui.components.VoiceInput
import timber.log.Timber

/**
 * 灵感对话框状态
 */
data class IdeaDialogState(
    val content: String = "",
    val scene: String = "",
    val memo: String = "",
    val clipboardText: String? = null,
    val images: List<ImageData> = emptyList()
)

/**
 * 灵感对话框 - 用于快速捕获灵感
 */
@Composable
fun IdeaDialog(
    state: IdeaDialogState = IdeaDialogState(),
    onDismiss: () -> Unit = {},
    onSave: (IdeaDialogState) -> Unit = {},
    onSaveAndContinue: (IdeaDialogState) -> Unit = {}
) {
    var content by remember { mutableStateOf(state.content) }
    var scene by remember { mutableStateOf(state.scene) }
    var memo by remember { mutableStateOf(state.memo) }
    var images by remember { mutableStateOf(state.images) }
    var showClipboardPreview by remember { mutableStateOf(state.clipboardText != null) }

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
                        text = "💡 捕获灵感",
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
                    // 语音输入和图片选择工具栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 语音输入
                        VoiceInput(
                            onTextRecognized = { text ->
                                content = if (content.isBlank()) text else "$content\n$text"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 灵感内容输入
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("这个灵感是什么？") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Yellow500
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
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "场景"
                            )
                        },
                        singleLine = true
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
                            focusedBorderColor = Yellow500
                        ),
                        maxLines = 3
                    )

                    // 剪切板预览
                    if (showClipboardPreview && state.clipboardText != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Yellow500.copy(alpha = 0.1f)
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
                                    color = Color.Gray,
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
                                    IdeaDialogState(
                                        content = content,
                                        scene = scene,
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
                                onSaveAndContinue(
                                    IdeaDialogState(
                                        content = content,
                                        scene = scene,
                                        memo = memo,
                                        images = images
                                    )
                                )
                                content = ""
                                scene = ""
                                memo = ""
                                images = emptyList()
                                Timber.d("Saved and continuing...")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Yellow500
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
