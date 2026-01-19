package com.jishi.clipboard.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jishi.clipboard.R
import kotlinx.coroutines.flow.first
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 详情页面
 * 显示剪贴板内容的完整信息
 */
@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: com.jishi.clipboard.repository.ClipboardRepository

    private var clipboardId: Long = -1
    private var clipboard: com.jishi.clipboard.data.ClipboardEntity? = null

    private lateinit var contentText: android.widget.TextView
    private lateinit var timeText: android.widget.TextView
    private lateinit var tagsContainer: android.widget.LinearLayout
    private lateinit var copyButton: android.widget.Button
    private lateinit var editButton: android.widget.Button
    private lateinit var deleteButton: android.widget.Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 获取传入的 ID
        clipboardId = intent.getLongExtra(EXTRA_CLIPBOARD_ID, -1)
        if (clipboardId == -1L) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadData()
    }

    private fun initViews() {
        contentText = findViewById(R.id.contentText)
        timeText = findViewById(R.id.timeText)
        tagsContainer = findViewById(R.id.tagsContainer)
        copyButton = findViewById(R.id.copyButton)
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.deleteButton)

        // 复制按钮
        copyButton.setOnClickListener {
            copyToClipboard()
        }

        // 编辑按钮
        editButton.setOnClickListener {
            openEditActivity()
        }

        // 删除按钮
        deleteButton.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                clipboard = repository.getClipboardById(clipboardId)
                if (clipboard == null) {
                    Toast.makeText(this@DetailActivity, "记录不存在", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                // 显示内容
                contentText.text = clipboard!!.content
                timeText.text = "创建时间: ${formatTime(clipboard!!.createdAt)}"

                // 加载标签 - 使用 first() 获取单次结果
                val tags = repository.getTagsForClipboard(clipboardId).first()
                displayTags(tags)
            } catch (e: Exception) {
                Timber.e(e, "加载数据失败")
                Toast.makeText(this@DetailActivity, "加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayTags(tags: List<com.jishi.clipboard.data.TagEntity>) {
        tagsContainer.removeAllViews()

        if (tags.isEmpty()) {
            val emptyText = android.widget.TextView(this).apply {
                text = "暂无标签"
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_hint, null))
                setPadding(16, 8, 16, 8)
            }
            tagsContainer.addView(emptyText)
            return
        }

        tags.forEach { tag ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = tag.name
                textSize = 14f
                chipCornerRadius = 16f
                setChipBackgroundColorResource(R.color.primary)
                setTextColor(resources.getColor(R.color.primary, null))
                // 使用透明度让文字可见
                setTextColor(android.graphics.Color.parseColor("#FFFFFF"))

                // 点击跳转到标签详情
                setOnClickListener {
                    openTagDetail(tag.name)
                }
            }
            val layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 16)
            }
            chip.layoutParams = layoutParams
            tagsContainer.addView(chip)
        }
    }

    private fun openTagDetail(tagName: String) {
        val intent = Intent(this, TagDetailActivity::class.java).apply {
            putExtra(TagDetailActivity.EXTRA_TAG_NAME, tagName)
        }
        startActivity(intent)
    }

    private fun copyToClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("剪贴板内容", clipboard?.content)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(this, "✅ 已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    private fun openEditActivity() {
        val dialog = com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment.newInstance(
            initialContent = clipboard?.content ?: "",
            autoFillClipboard = false,
            editMode = true,
            editClipboardId = clipboardId
        )

        dialog.setOnSaveListener { _, _ ->
            loadData() // 刷新数据
        }

        dialog.setOnDismissListener {
            // 可选：关闭时也刷新
        }

        dialog.show(supportFragmentManager, "EditClipboardDialog")
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除这条记录吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteItem()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem() {
        lifecycleScope.launch {
            try {
                clipboard?.let { repository.deleteClipboard(it) }
                Toast.makeText(this@DetailActivity, "✅ 已删除", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Timber.e(e, "删除失败")
                Toast.makeText(this@DetailActivity, "❌ 删除失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    companion object {
        const val EXTRA_CLIPBOARD_ID = "clipboard_id"
    }
}
