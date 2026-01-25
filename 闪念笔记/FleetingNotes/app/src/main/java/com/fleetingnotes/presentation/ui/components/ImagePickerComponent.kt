package com.fleetingnotes.presentation.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File

/**
 * 图片数据
 */
data class ImageData(
    val uri: String,
    val file: File? = null
)

/**
 * 图片选择器组件
 */
@Composable
fun ImagePicker(
    images: List<ImageData> = emptyList(),
    onImagesSelected: (List<ImageData>) -> Unit = {},
    onImageRemoved: (ImageData) -> Unit = {},
    maxImages: Int = 3
) {
    var showImagePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 图片选择 Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val imageData = ImageData(uri.toString())
            onImagesSelected(listOf(imageData))
        }
    }

    // 相机 Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // 保存到临时文件
            val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
            }
            val imageData = ImageData(tempFile.absolutePath, tempFile)
            onImagesSelected(listOf(imageData))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 已选择的图片预览
        if (images.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEach { imageData ->
                    ImagePreviewCard(
                        imageData = imageData,
                        onRemove = { onImageRemoved(imageData) }
                    )
                }

                // 如果还可以添加更多图片
                if (images.size < maxImages) {
                    AddImageButton(
                        onClick = { showImagePicker = true },
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        } else {
            // 添加第一个图片
            AddImageButton(
                onClick = { showImagePicker = true },
                modifier = Modifier.size(80.dp)
            )
        }

        // 图片来源选择对话框
        if (showImagePicker) {
            AlertDialog(
                onDismissRequest = { showImagePicker = false },
                title = { Text("选择图片来源") },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                                showImagePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📷 从相册选择")
                        }
                        TextButton(
                            onClick = {
                                cameraLauncher.launch(null)
                                showImagePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📸 拍照")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showImagePicker = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

/**
 * 图片预览卡片
 */
@Composable
fun ImagePreviewCard(
    imageData: ImageData,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(imageData.uri)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = "预览图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 删除按钮
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(2.dp)
                )
            }
        }
    }
}

/**
 * 添加图片按钮
 */
@Composable
fun AddImageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
