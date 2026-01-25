package com.jishi.clipboard.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.jishi.clipboard.R
import com.jishi.clipboard.data.TodoEntity
import com.jishi.clipboard.repository.TodoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 待办事项详情页面
 */
@AndroidEntryPoint
class TodoDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var todoRepository: TodoRepository

    private lateinit var todo: TodoEntity

    companion object {
        const val EXTRA_TODO_ID = "todo_id"

        fun start(context: android.content.Context, todoId: Long) {
            val intent = android.content.Intent(context, TodoDetailActivity::class.java).apply {
                putExtra(EXTRA_TODO_ID, todoId)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_detail)

        setupToolbar()
        loadTodoDetail()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "待办详情"
    }

    private fun loadTodoDetail() {
        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1)
        if (todoId == -1L) {
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                todo = todoRepository.getTodoById(todoId) ?: run {
                    Toast.makeText(this@TodoDetailActivity, "待办不存在", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                bindTodoDetail()

            } catch (e: Exception) {
                Timber.e(e, "加载待办详情失败")
                finish()
            }
        }
    }

    private fun bindTodoDetail() {
        // 任务描述
        findViewById<TextView>(R.id.taskTextView).text = todo.task

        // 原始内容
        val rawContentCard = findViewById<View>(R.id.rawContentCard)
        findViewById<TextView>(R.id.rawContentTextView).apply {
            text = todo.rawContent
            rawContentCard.visibility = if (todo.rawContent != todo.task) View.VISIBLE else View.GONE
        }

        // 时间信息
        val timeCard = findViewById<View>(R.id.timeCard)
        findViewById<TextView>(R.id.timeTextView).apply {
            if (todo.dueTimestamp != null) {
                val formattedTime = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
                    .format(Date(todo.dueTimestamp!!))
                text = "$formattedTime (${todo.originalTimeText ?: ""})"
                timeCard.visibility = View.VISIBLE
            } else {
                timeCard.visibility = View.GONE
            }
        }

        // 标签
        val tagsCard = findViewById<View>(R.id.tagsCard)
        val tagsChipGroup = findViewById<ChipGroup>(R.id.tagsChipGroup)
        tagsChipGroup.removeAllViews()
        if (todo.tags.isNotEmpty()) {
            todo.tags.split(",").forEach { tagName ->
                if (tagName.isNotBlank()) {
                    val chip = Chip(tagsChipGroup.context).apply {
                        text = tagName
                        textSize = 12f
                        chipCornerRadius = 12f
                    }
                    tagsChipGroup.addView(chip)
                }
            }
            tagsCard.visibility = View.VISIBLE
        } else {
            tagsCard.visibility = View.GONE
        }

        // 状态
        findViewById<TextView>(R.id.statusTextView).text = when (todo.status) {
            "PENDING" -> "待办"
            "COMPLETED" -> "已完成"
            "CANCELLED" -> "已取消"
            else -> todo.status
        }

        // 创建时间
        val createTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(todo.createdAt))
        findViewById<TextView>(R.id.createdAtTextView).text = "创建于 $createTime"

        // 完成时间
        findViewById<TextView>(R.id.completedAtTextView).apply {
            if (todo.completedAt != null) {
                val completedTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(todo.completedAt!!))
                text = "完成于 $completedTime"
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
