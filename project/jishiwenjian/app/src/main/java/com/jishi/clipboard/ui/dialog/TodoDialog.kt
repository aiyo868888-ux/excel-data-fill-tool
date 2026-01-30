package com.jishi.clipboard.ui.dialog

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import android.text.method.ScrollingMovementMethod
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jishi.clipboard.R
import com.jishi.clipboard.data.json.TodoItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 待办对话框
 */
class TodoDialog : BottomSheetDialogFragment() {
    private var onSaveListener: ((TodoItem) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null
    private var initialContent: String = ""

    private lateinit var etTask: EditText
    private lateinit var btnPickTime: Button
    private lateinit var btnSave: Button
    private lateinit var priorityRadioGroup: RadioGroup
    private lateinit var rbHigh: RadioButton
    private lateinit var rbMedium: RadioButton
    private lateinit var rbLow: RadioButton

    private var dueTimestamp: Long? = null
    private var selectedPriority: String = "MEDIUM"  // 默认中优先级

    fun setOnSaveListener(listener: (TodoItem) -> Unit) {
        onSaveListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    /**
     * 设置初始内容
     */
    fun setInitialContent(content: String) {
        initialContent = content
        if (::etTask.isInitialized) {
            etTask.setText(content)
            etTask.setSelection(content.length)
        }
    }

    /**
     * 聚焦到输入框并显示键盘
     */
    fun requestFocusAndShowKeyboard() {
        if (::etTask.isInitialized) {
            etTask.requestFocus()
            etTask.post {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etTask, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTask = view.findViewById(R.id.etTask)
        btnPickTime = view.findViewById(R.id.btnPickTime)
        btnSave = view.findViewById(R.id.btnSave)
        priorityRadioGroup = view.findViewById(R.id.priorityRadioGroup)
        rbHigh = view.findViewById(R.id.rbHigh)
        rbMedium = view.findViewById(R.id.rbMedium)
        rbLow = view.findViewById(R.id.rbLow)

        // 启用 EditText 滚动
        etTask.movementMethod = ScrollingMovementMethod.getInstance()
        etTask.setVerticalScrollBarEnabled(true)

        // 设置初始内容
        if (initialContent.isNotEmpty()) {
            etTask.setText(initialContent)
            etTask.setSelection(initialContent.length)
        }

        // 设置默认优先级
        rbMedium.isChecked = true

        // 优先级选择监听
        priorityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedPriority = when (checkedId) {
                R.id.rbHigh -> "HIGH"
                R.id.rbMedium -> "MEDIUM"
                R.id.rbLow -> "LOW"
                else -> "MEDIUM"
            }
        }

        btnPickTime.setOnClickListener {
            showDatePicker()
        }

        btnSave.setOnClickListener {
            saveTodo()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                dueTimestamp = calendar.timeInMillis

                val sdf = SimpleDateFormat("MM月dd日", Locale.getDefault())
                btnPickTime.text = "截止时间：${sdf.format(calendar.time)}"
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun saveTodo() {
        val task = etTask.text?.toString()?.trim() ?: ""
        if (task.isEmpty()) {
            etTask.error = "内容不能为空"
            return
        }

        val item = TodoItem(
            task = task,
            dueTimestamp = dueTimestamp,
            status = "PENDING",
            priority = selectedPriority
        )

        onSaveListener?.invoke(item)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismissListener?.invoke()
        super.onDismiss(dialog)
    }

    companion object {
        fun newInstance() = TodoDialog()
    }
}
