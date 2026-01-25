package com.fleetingnotes.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fleetingnotes.BuildConfig
import com.fleetingnotes.presentation.theme.FleetingNotesTheme
import com.fleetingnotes.presentation.ui.home.MainScreen
import com.fleetingnotes.service.FloatingWindowService
import com.fleetingnotes.service.ClipboardService
import com.fleetingnotes.data.model.NoteType
import timber.log.Timber

/**
 * 主 Activity - 单 Activity 架构
 */
class MainActivity : ComponentActivity() {

    private lateinit var permissions: Array<String>
    private var pendingDialogType by mutableStateOf<NoteType?>(null)

    // 权限请求 Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        permissionsMap.forEach { (permission, isGranted) ->
            if (!isGranted) {
                Timber.w("Permission denied: $permission")
                // 可以在此显示说明为何需要权限的对话框
            } else {
                Timber.d("Permission granted: $permission")
            }
        }
    }

    // 悬浮窗权限 Launcher
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 检查悬浮窗权限
        if (Settings.canDrawOverlays(this)) {
            Timber.d("Overlay permission granted")
            startFloatingWindowService()
        } else {
            Timber.w("Overlay permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化权限列表
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        }

        // 初始化 Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(timber.log.Timber.DebugTree())
        }

        setContent {
            FleetingNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        pendingDialogType = pendingDialogType,
                        onDialogShown = { pendingDialogType = null }
                    )
                }
            }
        }

        // 请求权限
        checkAndRequestPermissions()

        // 启动悬浮窗服务（如果有权限）
        if (Settings.canDrawOverlays(this)) {
            startFloatingWindowService()
        }

        // 暂时禁用剪切板监听服务（导致手机死机）
        // startClipboardService()
        Timber.d("ClipboardService disabled temporarily")
    }

    /**
     * 检查并请求权限
     */
    private fun checkAndRequestPermissions() {
        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            // 引导用户开启悬浮窗权限
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }

        // 检查其他权限
        val missingPermissions = permissions.filter { permission ->
            checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val dialogTypeStr = intent.getStringExtra("dialog_type")
        Timber.d("onNewIntent received, dialog_type: $dialogTypeStr")
        if (dialogTypeStr != null) {
            try {
                pendingDialogType = NoteType.valueOf(dialogTypeStr)
                Timber.d("Pending dialog type set to: $pendingDialogType")
            } catch (e: IllegalArgumentException) {
                Timber.e("Invalid dialog type: $dialogTypeStr")
            }
        }
    }

    /**
     * 启动悬浮窗服务
     */
    private fun startFloatingWindowService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Timber.d("FloatingWindowService started")
    }

    /**
     * 启动剪切板监听服务
     */
    private fun startClipboardService() {
        val intent = Intent(this, ClipboardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Timber.d("ClipboardService started")
    }
}
