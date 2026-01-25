package com.fleetingnotes.presentation.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fleetingnotes.ServiceLocator
import com.fleetingnotes.service.FloatingWindowService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    // 悬浮窗服务状态
    var isFloatingWindowEnabled by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }

    // 检查悬浮窗权限和状态
    LaunchedEffect(Unit) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
        // 检查悬浮窗服务是否正在运行
        isFloatingWindowEnabled = FloatingWindowService.isRunning
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 悬浮窗设置部分
            Section(title = "悬浮窗") {
                // 悬浮窗权限
                SettingItem(
                    title = "悬浮窗权限",
                    subtitle = if (hasOverlayPermission)
                        "已授权"
                    else
                        "未授权 - 点击开启",
                    onClick = {
                        if (!hasOverlayPermission) {
                            // 打开系统设置
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.packageName)
                            )
                            context.startActivity(intent)
                        }
                    }
                )

                // 悬浮窗开关（照搬及时记的方式）
                if (hasOverlayPermission) {
                    SwitchSettingItem(
                        title = "启用悬浮窗",
                        subtitle = "点击悬浮窗快速记录笔记",
                        checked = isFloatingWindowEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                try {
                                    if (enabled) {
                                        // 开启悬浮窗（照搬及时记的 ACTION_SHOW 方式）
                                        Intent(context, FloatingWindowService::class.java).apply {
                                            action = FloatingWindowService.ACTION_SHOW
                                            context.startService(this)
                                        }
                                        isFloatingWindowEnabled = true
                                        Toast.makeText(context, "悬浮窗已开启", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // 关闭悬浮窗（照搬及时记的 ACTION_HIDE 方式）
                                        Intent(context, FloatingWindowService::class.java).apply {
                                            action = FloatingWindowService.ACTION_HIDE
                                            context.startService(this)
                                        }
                                        isFloatingWindowEnabled = false
                                        Toast.makeText(context, "悬浮窗已关闭", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }

                // 使用说明（照搬及时记）
                SettingItem(
                    title = "使用说明",
                    subtitle = "点击悬浮窗 → 选择类型 → 自动读取剪切板内容"
                )
            }

            HorizontalDivider()

            // 关于部分
            Section(title = "关于") {
                SettingItem(
                    title = "版本",
                    subtitle = getAppVersion(context)
                )
            }

            HorizontalDivider()

            // 数据管理部分
            Section(title = "数据管理") {
                SettingItem(
                    title = "清除所有数据",
                    subtitle = "删除所有本地存储的笔记",
                    onClick = { showClearDialog = true },
                    isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 底部版权信息
            Text(
                text = "闪念笔记 v${getAppVersion(context)}\n简单快速的笔记工具",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // 清除数据确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清除") },
            text = { Text("确定要删除所有笔记数据吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                // 清除所有数据
                                ServiceLocator.noteRepository.clearAll()
                                Toast.makeText(
                                    context,
                                    "数据已清除",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showClearDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "清除失败: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDestructive)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 带开关的设置项
 */
@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

private fun getAppVersion(context: Context): String {
    return try {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "未知"
    } catch (e: PackageManager.NameNotFoundException) {
        "未知"
    }
}
