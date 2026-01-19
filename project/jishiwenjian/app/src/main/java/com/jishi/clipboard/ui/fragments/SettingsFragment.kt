package com.jishi.clipboard.ui.fragments

import android.app.AppOpsManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jishi.clipboard.R
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.service.FloatingWindowService
import com.jishi.clipboard.utils.ReminderPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 设置 Fragment
 * 管理悬浮窗、权限和数据
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var repository: ClipboardRepository

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button
    private lateinit var permissionButton: Button
    private lateinit var clearDataButton: Button
    private lateinit var todoReminderSwitch: Switch
    private lateinit var todoReminderTimeText: TextView
    private lateinit var todoReminderTimeLayout: View

    private lateinit var reminderPrefs: ReminderPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reminderPrefs = ReminderPreferences(requireContext())
        initViews()
        checkFloatingWindowStatus()
        loadReminderSettings()
    }

    private fun initViews() {
        statusText = requireView().findViewById(R.id.statusText)
        toggleButton = requireView().findViewById(R.id.toggleButton)
        permissionButton = requireView().findViewById(R.id.permissionButton)
        clearDataButton = requireView().findViewById(R.id.clearDataButton)
        todoReminderSwitch = requireView().findViewById(R.id.todoReminderSwitch)
        todoReminderTimeText = requireView().findViewById(R.id.todoReminderTimeText)
        todoReminderTimeLayout = requireView().findViewById(R.id.todoReminderTimeLayout)

        // 悬浮窗开关
        toggleButton.setOnClickListener {
            toggleFloatingWindow()
        }

        // 权限按钮
        permissionButton.setOnClickListener {
            requestPermission()
        }

        // 清空数据按钮
        clearDataButton.setOnClickListener {
            showClearDataDialog()
        }

        // 待办提醒开关
        todoReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            reminderPrefs.setTodoReminderEnabled(isChecked)
            Toast.makeText(requireContext(),
                if (isChecked) "✅ 已启用待办提醒" else "❌ 已禁用待办提醒",
                Toast.LENGTH_SHORT).show()
        }

        // 待办提醒时间设置
        todoReminderTimeLayout.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun loadReminderSettings() {
        // 加载待办提醒设置
        todoReminderSwitch.isChecked = reminderPrefs.isTodoReminderEnabled()
        updateReminderTimeText()
    }

    private fun updateReminderTimeText() {
        todoReminderTimeText.text = reminderPrefs.getFormattedReminderTime()
    }

    private fun showTimePickerDialog() {
        val currentHour = reminderPrefs.getTodoReminderHour()
        val currentMinute = reminderPrefs.getTodoReminderMinute()

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                reminderPrefs.setTodoReminderTime(hourOfDay, minute)
                updateReminderTimeText()
                Toast.makeText(
                    requireContext(),
                    "✅ 提醒时间已设置为 ${String.format("%02d:%02d", hourOfDay, minute)}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            currentHour,
            currentMinute,
            true // 24小时制
        ).show()
    }

    private fun checkFloatingWindowStatus() {
        if (com.jishi.clipboard.service.FloatingWindowService.isRunning) {
            statusText.text = "悬浮窗：运行中"
            toggleButton.text = "关闭"
            toggleButton.isEnabled = true
        } else if (hasFloatingWindowPermission()) {
            statusText.text = "悬浮窗：已授权（未运行）"
            toggleButton.text = "开启"
            toggleButton.isEnabled = true
        } else {
            statusText.text = "悬浮窗：未授权"
            toggleButton.text = "开启"
            toggleButton.isEnabled = false
        }
    }

    private fun toggleFloatingWindow() {
        if (com.jishi.clipboard.service.FloatingWindowService.isRunning) {
            // 关闭悬浮窗
            Intent(requireContext(), com.jishi.clipboard.service.FloatingWindowService::class.java).apply {
                action = com.jishi.clipboard.service.FloatingWindowService.ACTION_HIDE
                requireContext().startService(this)
            }
            Toast.makeText(requireContext(), "悬浮窗已关闭", Toast.LENGTH_SHORT).show()
            checkFloatingWindowStatus()
        } else {
            // 开启悬浮窗
            Intent(requireContext(), com.jishi.clipboard.service.FloatingWindowService::class.java).apply {
                action = com.jishi.clipboard.service.FloatingWindowService.ACTION_SHOW
                requireContext().startService(this)
            }
            Toast.makeText(requireContext(), "悬浮窗已开启", Toast.LENGTH_SHORT).show()
            checkFloatingWindowStatus()
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            ).also {
                startActivityForResult(it, REQUEST_OVERLAY_PERMISSION)
            }
        }
    }

    private fun showClearDataDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("清空数据")
            .setMessage("确定要清空所有剪贴板记录吗？此操作不可恢复。")
            .setPositiveButton("清空") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun clearAllData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.deleteAllClipboards()
                Toast.makeText(requireContext(), "✅ 数据已清空", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Timber.e(e, "清空数据失败")
                Toast.makeText(requireContext(), "❌ 清空失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasFloatingWindowPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ops = requireContext().getSystemService(AppOpsManager::class.java)
            ops?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return it.checkOpNoThrow(
                        AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                        android.os.Process.myUid(),
                        requireContext().packageName
                    ) == AppOpsManager.MODE_ALLOWED
                }
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            checkFloatingWindowStatus()
        }
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1001
        fun newInstance() = SettingsFragment()
    }
}
