package com.jishi.clipboard.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.R
import com.jishi.clipboard.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 待办事项界面
 * 显示所有设置了提醒的剪贴板内容，按提醒时间排序
 */
@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {

    @Inject
    lateinit var clipboardRepository: com.jishi.clipboard.repository.ClipboardRepository

    @Inject
    lateinit var reminderRepository: ReminderRepository

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private val adapter = ClipboardAdapter(
        onDeleteClick = { item ->
            showDeleteDialog(item)
        },
        onItemClick = { item ->
            openDetailActivity(item)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        title = "待办"

        initViews()
        loadTodoItems()
    }

    private fun initViews() {
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 搜索监听
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchClipboards(newText ?: "")
                return true
            }
        })
    }

    /**
     * 加载待办事项（有提醒的剪贴板记录）
     */
    private fun loadTodoItems() {
        lifecycleScope.launch {
            reminderRepository.getAllReminders().collect { reminders ->
                // 获取对应的剪贴板记录
                val clipboardIds = reminders.map { it.clipboardId }.toSet()
                if (clipboardIds.isEmpty()) {
                    updateEmptyList()
                    return@collect
                }

                // 按提醒时间排序
                val sortedReminders = reminders.sortedBy { it.timestamp }
                val sortedClipboardIds = sortedReminders.map { it.clipboardId }

                // 获取剪贴板内容并按提醒时间排序
                val clipboards = mutableListOf<com.jishi.clipboard.data.ClipboardEntity>()
                sortedClipboardIds.forEach { clipboardId ->
                    clipboardRepository.getClipboardById(clipboardId)?.let {
                        clipboards.add(it)
                    }
                }

                updateList(clipboards)
            }
        }
    }

    private fun searchClipboards(query: String) {
        lifecycleScope.launch {
            if (query.isEmpty()) {
                loadTodoItems()
            } else {
                // 搜索待办事项
                reminderRepository.getAllReminders().collect { reminders ->
                    val clipboardIds = reminders.map { it.clipboardId }.toSet()

                    clipboardRepository.searchClipboards(query).collect { clipboards ->
                        // 只保留有待办提醒的
                        val filtered = clipboards.filter { it.id in clipboardIds }
                        updateList(filtered)
                    }
                }
            }
        }
    }

    private fun updateEmptyList() {
        recyclerView.visibility = android.view.View.GONE
        emptyView.visibility = android.view.View.VISIBLE
        emptyView.text = "暂无待办事项\n保存内容时设置提醒"
    }

    private fun updateList(clipboards: List<com.jishi.clipboard.data.ClipboardEntity>) {
        if (clipboards.isEmpty()) {
            updateEmptyList()
        } else {
            recyclerView.visibility = android.view.View.VISIBLE
            emptyView.visibility = android.view.View.GONE
            adapter.submitList(clipboards)
        }
    }

    private fun openDetailActivity(item: com.jishi.clipboard.data.ClipboardEntity) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_CLIPBOARD_ID, item.id)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(item: com.jishi.clipboard.data.ClipboardEntity) {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除这条待办事项吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteItem(item)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem(item: com.jishi.clipboard.data.ClipboardEntity) {
        lifecycleScope.launch {
            try {
                clipboardRepository.deleteClipboard(item)
                // 同时删除相关提醒
                reminderRepository.deleteRemindersForClipboard(item.id)
                Toast.makeText(this@HistoryActivity, "✅ 已删除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Timber.e(e, "删除失败")
                Toast.makeText(this@HistoryActivity, "❌ 删除失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
