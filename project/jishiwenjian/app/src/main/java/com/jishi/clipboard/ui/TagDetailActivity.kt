package com.jishi.clipboard.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.R
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.data.TagDefinition
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.repository.TagRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 标签详情页 - 显示某个标签下的所有剪贴板记录
 */
@AndroidEntryPoint
class TagDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    @Inject
    lateinit var tagRepository: TagRepository

    private lateinit var tagName: String
    private var tagDefinitionId: Long = -1
    private val adapter = ClipboardAdapter(
        onDeleteClick = { item -> showDeleteDialog(item) },
        onItemClick = { item -> openDetailActivity(item) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        tagName = intent.getStringExtra(EXTRA_TAG_NAME) ?: ""
        title = tagName

        // 隐藏搜索栏（TagDetailActivity复用了activity_history布局但不需要搜索）
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        searchView?.visibility = View.GONE

        setupRecyclerView()
        loadTagClipboards()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadTagClipboards() {
        lifecycleScope.launch {
            try {
                // 先获取标签定义
                val tagDef = tagRepository.getTagDefinitionByName(tagName)
                if (tagDef != null) {
                    tagDefinitionId = tagDef.id
                    // 使用 tagDefinitionId 获取剪贴板
                    clipboardRepository.getClipboardsByTagDefinition(tagDef.id).collect { clipboards ->
                        adapter.submitList(clipboards)
                    }
                } else {
                    Toast.makeText(this@TagDetailActivity, "标签不存在", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "加载标签内容失败")
                Toast.makeText(this@TagDetailActivity, "加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDetailActivity(item: ClipboardEntity) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_CLIPBOARD_ID, item.id)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(item: ClipboardEntity) {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除这条记录吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteItem(item)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem(item: ClipboardEntity) {
        lifecycleScope.launch {
            try {
                clipboardRepository.deleteClipboard(item)
                Toast.makeText(this@TagDetailActivity, "✅ 已删除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Timber.e(e, "删除失败")
                Toast.makeText(this@TagDetailActivity, "❌ 删除失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_TAG_NAME = "tag_name"
    }
}
