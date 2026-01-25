package com.jishi.clipboard.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jishi.clipboard.R
import com.jishi.clipboard.network.server.WebServerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Web同步测试界面
 * 用于启动/停止Web服务器并查看连接状态
 */
@AndroidEntryPoint
class WebSyncTestActivity : AppCompatActivity() {

    @Inject
    lateinit var webServerManager: WebServerManager

    private lateinit var tvStatus: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPairingCode: TextView
    private lateinit var tvConnectedDevices: TextView
    private lateinit var btnToggle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_sync_test)

        // 初始化视图
        tvStatus = findViewById(R.id.tv_status)
        tvAddress = findViewById(R.id.tv_address)
        tvPairingCode = findViewById(R.id.tv_pairing_code)
        tvConnectedDevices = findViewById(R.id.tv_connected_devices)
        btnToggle = findViewById(R.id.btn_toggle)

        // 设置按钮点击事件
        btnToggle.setOnClickListener {
            toggleServer()
        }

        // 观察服务器状态
        lifecycleScope.launch {
            webServerManager.isRunning.collect { isRunning ->
                updateUI(isRunning)
            }
        }

        // 观察服务器地址
        lifecycleScope.launch {
            webServerManager.serverAddress.collect { address ->
                tvAddress.text = "服务器地址：${address ?: "未启动"}"
            }
        }
    }

    /**
     * 切换服务器状态
     */
    private fun toggleServer() {
        lifecycleScope.launch {
            val isRunning = webServerManager.isRunning.value
            if (isRunning) {
                // 停止服务器
                webServerManager.stop()
            } else {
                // 启动服务器
                val success = webServerManager.start()
                if (success) {
                    // 生成配对码（这里需要实现配对码生成逻辑）
                    val pairingCode = generatePairingCode()
                    tvPairingCode.text = "配对码：$pairingCode"
                }
            }
        }
    }

    /**
     * 更新UI
     */
    private fun updateUI(isRunning: Boolean) {
        if (isRunning) {
            tvStatus.text = "状态：运行中"
            btnToggle.text = "停止服务器"
            tvStatus.setTextColor(getColor(R.color.success))
        } else {
            tvStatus.text = "状态：已停止"
            btnToggle.text = "启动服务器"
            tvStatus.setTextColor(getColor(R.color.error))
        }
    }

    /**
     * 生成6位配对码
     */
    private fun generatePairingCode(): String {
        return (100000..999999).random().toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止服务器
        webServerManager.stop()
    }
}
