package com.jishi.clipboard.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jishi.clipboard.R
import com.jishi.clipboard.data.Reminder
import com.jishi.clipboard.reminder.DateTimeExtractor
import com.jishi.clipboard.reminder.ReminderScheduler
import com.jishi.clipboard.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 提醒确认对话框 - 简化版
 */
@AndroidEntryPoint
class ReminderConfirmDialog : BottomSheetDialogFragment() {

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var todoRepository: com.jishi.clipboard.repository.TodoRepository

    @Inject
    lateinit var tagRepository: com.jishi.clipboard.repository.TagRepository

    private lateinit var contentText: android.widget.TextView
    private lateinit var extractedTimeText: android.widget.TextView
    private lateinit var typeNotification: android.widget.CheckBox
    private lateinit var typeAlarm: android.widget.CheckBox
    private lateinit var typeNotificationCard: com.google.android.material.card.MaterialCardView
    private lateinit var typeAlarmCard: com.google.android.material.card.MaterialCardView
    private lateinit var editTimeButton: com.google.android.material.button.MaterialButton

    private var content: String = ""
    private var extractedTime: DateTimeExtractor.ExtractedDateTime? = null
    private var selectedTimestamp: Long = 0
    private var clipboardId: Long = -1
    private var isTodoMode: Boolean = false  // 是否为待办模式

    private var onConfirmListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_reminder_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        handleArguments()
    }

    private fun initViews() {
        contentText = requireView().findViewById(R.id.contentText)
        extractedTimeText = requireView().findViewById(R.id.extractedTimeText)
        typeNotification = requireView().findViewById(R.id.typeNotification)
        typeAlarm = requireView().findViewById(R.id.typeAlarm)
        typeNotificationCard = requireView().findViewById(R.id.typeNotificationCard)
        typeAlarmCard = requireView().findViewById(R.id.typeAlarmCard)
        editTimeButton = requireView().findViewById(R.id.btnEditTime)

        // 默认选中通知
        typeNotification.isChecked = true

        // 点击通知卡片切换选中状态
        typeNotificationCard.setOnClickListener {
            typeNotification.isChecked = !typeNotification.isChecked
        }

        // 点击闹铃卡片切换选中状态
        typeAlarmCard.setOnClickListener {
            typeAlarm.isChecked = !typeAlarm.isChecked
        }

        // 编辑时间按钮
        editTimeButton.setOnClickListener {
            showTimePicker()
        }

        // 取消按钮
        requireView().findViewById<View>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        // 确认按钮
        requireView().findViewById<View>(R.id.btnConfirm).setOnClickListener {
            saveReminder()
        }
    }

    private fun handleArguments() {
        content = arguments?.getString(ARG_CONTENT) ?: ""
        val timestamp = arguments?.getLong(ARG_TIMESTAMP) ?: 0L
        clipboardId = arguments?.getLong(ARG_CLIPBOARD_ID) ?: -1L
        isTodoMode = arguments?.getBoolean(ARG_TODO_MODE, false) ?: false

        android.util.Log.d("ReminderConfirmDialog", "handleArguments 开始")
        android.util.Log.d("ReminderConfirmDialog", "接收内容: ${content.take(50)}")
        android.util.Log.d("ReminderConfirmDialog", "接收时间戳: $timestamp, ${DateTimeExtractor.formatTimestamp(timestamp)}")
        android.util.Log.d("ReminderConfirmDialog", "接收clipboardId: $clipboardId")
        android.util.Log.d("ReminderConfirmDialog", "接收isTodoMode: $isTodoMode")

        if (timestamp > 0) {
            // 待办模式使用原始时间，普通模式提前30分钟
            selectedTimestamp = if (isTodoMode) {
                timestamp
            } else {
                timestamp - (30 * 60 * 1000)
            }

            android.util.Log.d("ReminderConfirmDialog", "最终提醒时间: $selectedTimestamp, ${DateTimeExtractor.formatTimestamp(selectedTimestamp)} (isTodoMode=$isTodoMode)")

            // 检查时间戳是否合法
            if (selectedTimestamp < System.currentTimeMillis()) {
                android.util.Log.w("ReminderConfirmDialog", "⚠️ 提醒时间已过期，使用原时间戳")
                selectedTimestamp = timestamp
            }

            extractedTime = DateTimeExtractor.ExtractedDateTime(
                timestamp = selectedTimestamp,
                originalText = "",
                confidence = arguments?.getFloat(ARG_CONFIDENCE) ?: 0.8f
            )
            android.util.Log.d("ReminderConfirmDialog", "创建extractedTime对象: ${extractedTime?.timestamp}")
        } else {
            android.util.Log.e("ReminderConfirmDialog", "❌ 时间戳无效: $timestamp")
        }

        // 显示内容和时间
        android.util.Log.d("ReminderConfirmDialog", "设置UI内容...")
        contentText.text = content
        extractedTime?.let {
            val formattedTime = DateTimeExtractor.formatTimestamp(it.timestamp)
            val timeLabel = if (isTodoMode) {
                formattedTime
            } else {
                "$formattedTime (提前30分钟)"
            }
            extractedTimeText.text = timeLabel
            android.util.Log.d("ReminderConfirmDialog", "显示时间: $timeLabel")
        } ?: run {
            android.util.Log.e("ReminderConfirmDialog", "❌ extractedTime为null，无法显示时间")
        }
        android.util.Log.d("ReminderConfirmDialog", "handleArguments 完成")
    }

    /**
     * 显示日期时间选择器
     */
    private fun showTimePicker() {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = selectedTimestamp
        }

        // 先显示日期选择器
        android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // 更新日期
                calendar.set(java.util.Calendar.YEAR, year)
                calendar.set(java.util.Calendar.MONTH, month)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)

                // 再显示时间选择器
                android.app.TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        // 更新时间
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(java.util.Calendar.MINUTE, minute)
                        selectedTimestamp = calendar.timeInMillis

                        // 更新显示
                        extractedTime = DateTimeExtractor.ExtractedDateTime(
                            timestamp = selectedTimestamp,
                            originalText = "",
                            confidence = 1.0f  // 用户手动修改，置信度最高
                        )

                        val formattedTime = DateTimeExtractor.formatTimestamp(selectedTimestamp)
                        extractedTimeText.text = "$formattedTime (已修改)"
                    },
                    calendar.get(java.util.Calendar.HOUR_OF_DAY),
                    calendar.get(java.util.Calendar.MINUTE),
                    true // 24小时制
                ).show()
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveReminder() {
        android.util.Log.d("ReminderConfirm", "saveReminder 开始: clipboardId=$clipboardId, timestamp=$selectedTimestamp")
        android.util.Log.d("ReminderConfirm", "通知=${typeNotification.isChecked}, 闹铃=${typeAlarm.isChecked}")

        // 检查是否至少选择了一个提醒方式
        if (!typeNotification.isChecked && !typeAlarm.isChecked) {
            Toast.makeText(requireContext(), "请至少选择一种提醒方式", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                android.util.Log.d("ReminderConfirm", "开始保存提醒...")
                var hasNotification = false
                var hasAlarm = false
                var lastReminderId: Long = -1

                // 保存通知提醒
                if (typeNotification.isChecked) {
                    android.util.Log.d("ReminderConfirm", "保存通知提醒")
                    val notificationReminder = Reminder(
                        clipboardId = clipboardId,
                        timestamp = selectedTimestamp,
                        type = "NOTIFICATION",
                        originalText = extractedTimeText.text.toString(),
                        content = content
                    )
                    lastReminderId = reminderRepository.insertReminder(notificationReminder)
                    android.util.Log.d("ReminderConfirm", "通知提醒已保存, id=$lastReminderId")
                    hasNotification = true
                }

                // 保存闹铃提醒
                if (typeAlarm.isChecked) {
                    android.util.Log.d("ReminderConfirm", "保存闹铃提醒")
                    val alarmReminder = Reminder(
                        clipboardId = clipboardId,
                        timestamp = selectedTimestamp,
                        type = "ALARM",
                        originalText = extractedTimeText.text.toString(),
                        content = content
                    )
                    lastReminderId = reminderRepository.insertReminder(alarmReminder)
                    android.util.Log.d("ReminderConfirm", "闹铃提醒已保存, id=$lastReminderId")
                    hasAlarm = true
                }

                // 调度提醒
                android.util.Log.d("ReminderConfirm", "开始调度提醒...")
                val scheduler = ReminderScheduler(requireContext())
                var success = true

                if (hasNotification) {
                    android.util.Log.d("ReminderConfirm", "调度通知提醒")
                    val notificationReminder = Reminder(
                        clipboardId = clipboardId,
                        timestamp = selectedTimestamp,
                        type = "NOTIFICATION",
                        originalText = extractedTimeText.text.toString(),
                        content = content,
                        id = lastReminderId
                    )
                    if (!scheduler.schedule(notificationReminder)) {
                        android.util.Log.e("ReminderConfirm", "通知提醒调度失败")
                        success = false
                    } else {
                        android.util.Log.d("ReminderConfirm", "通知提醒调度成功")
                    }
                }

                if (hasAlarm) {
                    android.util.Log.d("ReminderConfirm", "调度闹铃提醒")
                    val alarmReminder = Reminder(
                        clipboardId = clipboardId,
                        timestamp = selectedTimestamp,
                        type = "ALARM",
                        originalText = extractedTimeText.text.toString(),
                        content = content,
                        id = lastReminderId
                    )
                    if (!scheduler.schedule(alarmReminder)) {
                        android.util.Log.e("ReminderConfirm", "闹铃提醒调度失败")
                        success = false
                    } else {
                        android.util.Log.d("ReminderConfirm", "闹铃提醒调度成功")
                    }
                }

                if (success) {
                    val types = mutableListOf<String>()
                    if (hasNotification) types.add("通知")
                    if (hasAlarm) types.add("闹铃")
                    android.util.Log.d("ReminderConfirm", "所有提醒设置成功: ${types.joinToString("、")}")
                    Toast.makeText(requireContext(), "✅ 已设置${types.joinToString("、")}提醒", Toast.LENGTH_SHORT).show()

                    // 待办模式：创建待办记录
                    if (isTodoMode) {
                        android.util.Log.d("ReminderConfirm", "待办模式，创建待办记录...")
                        createTodoRecord()
                    }

                    onConfirmListener?.invoke()
                    dismiss()
                } else {
                    android.util.Log.d("ReminderConfirm", "显示权限对话框")
                    showPermissionDialog()
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderConfirm", "保存提醒失败", e)
                Timber.e(e, "保存提醒失败")
                Toast.makeText(requireContext(), "❌ 设置失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showPermissionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("需要精确闹钟权限")
            .setMessage("为了准时提醒，请开启精确闹钟权限")
            .setPositiveButton("去设置") { _, _ ->
                val scheduler = ReminderScheduler(requireContext())
                val intent = scheduler.createExactAlarmPermissionIntent()
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 创建待办记录
     */
    private fun createTodoRecord() {
        lifecycleScope.launch {
            try {
                // 解析任务描述
                val parseResult = com.jishi.clipboard.parser.TaskExtractor.extract(content)

                android.util.Log.d("ReminderConfirm", "创建待办: task=${parseResult.task}, timestamp=$selectedTimestamp")

                val todoEntity = com.jishi.clipboard.data.TodoEntity(
                    task = parseResult.task,
                    rawContent = content,
                    dueTimestamp = selectedTimestamp,
                    originalTimeText = extractedTimeText.text.toString(),
                    status = "PENDING",
                    tags = parseResult.tags.joinToString(","),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val todoId = todoRepository.insertTodo(todoEntity)
                android.util.Log.d("ReminderConfirm", "✅ 待办记录已创建, id=$todoId")

            } catch (e: Exception) {
                android.util.Log.e("ReminderConfirm", "❌ 创建待办记录失败", e)
                Timber.e(e, "创建待办记录失败")
            }
        }
    }

    fun setOnConfirmListener(listener: () -> Unit) {
        onConfirmListener = listener
    }

    companion object {
        private const val ARG_CONTENT = "content"
        private const val ARG_TIMESTAMP = "timestamp"
        private const val ARG_CLIPBOARD_ID = "clipboard_id"
        private const val ARG_CONFIDENCE = "confidence"
        private const val ARG_TODO_MODE = "todo_mode"

        fun newInstance(
            content: String,
            extractedTime: DateTimeExtractor.ExtractedDateTime,
            clipboardId: Long,
            isTodoMode: Boolean = false
        ): ReminderConfirmDialog {
            return ReminderConfirmDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT, content)
                    putLong(ARG_TIMESTAMP, extractedTime.timestamp)
                    putLong(ARG_CLIPBOARD_ID, clipboardId)
                    putFloat(ARG_CONFIDENCE, extractedTime.confidence)
                    putBoolean(ARG_TODO_MODE, isTodoMode)
                }
            }
        }
    }
}
