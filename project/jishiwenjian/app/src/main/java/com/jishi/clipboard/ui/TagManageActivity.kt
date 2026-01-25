package com.jishi.clipboard.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jishi.clipboard.R
import com.jishi.clipboard.data.TagDefinition
import com.jishi.clipboard.repository.TagRepository
import com.jishi.clipboard.ui.dialog.TagEditDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 标签管理页面
 * 支持增删改查、颜色选择
 */
@AndroidEntryPoint
class TagManageActivity : AppCompatActivity() {

    @Inject
    lateinit var tagRepository: TagRepository

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var fabAddTag: FloatingActionButton

    private val allTags = mutableListOf<TagDefinition>()
    private val adapter = TagManageAdapter(
        onEditClick = { tag -> showEditDialog(tag) },
        onDeleteClick = { tag -> showDeleteDialog(tag) },
        allTags = allTags
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_manage)

        setupToolbar()
        initViews()
        loadTags()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        fabAddTag = findViewById(R.id.fabAddTag)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAddTag.setOnClickListener {
            showAddDialog()
        }
    }

    private fun loadTags() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                try {
                    tagRepository.getAllTagsHierarchical().collect { tags ->
                        if (!isFinishing) {
                            allTags.clear()
                            allTags.addAll(tags)

                            if (tags.isEmpty()) {
                                recyclerView.visibility = View.GONE
                                emptyView.visibility = View.VISIBLE
                            } else {
                                recyclerView.visibility = View.VISIBLE
                                emptyView.visibility = View.GONE
                                adapter.submitList(tags)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "加载标签失败")
                    if (!isFinishing) {
                        Toast.makeText(this@TagManageActivity, "加载失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showAddDialog() {
        TagEditDialogFragment.newInstance().apply {
            setOnTagSavedListener {
                loadTags()
            }
            show(supportFragmentManager, "TagEditDialog")
        }
    }

    private fun showEditDialog(tag: TagDefinition) {
        TagEditDialogFragment.newInstance(tag).apply {
            setOnTagSavedListener {
                loadTags()
            }
            show(supportFragmentManager, "TagEditDialog")
        }
    }

    private fun showDeleteDialog(tag: TagDefinition) {
        AlertDialog.Builder(this)
            .setTitle("删除标签")
            .setMessage("确定要删除标签「${tag.name}」吗？\n\n注意：删除标签不会删除该标签下的剪贴板内容。")
            .setPositiveButton("删除") { _, _ ->
                deleteTag(tag)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteTag(tag: TagDefinition) {
        lifecycleScope.launch {
            try {
                tagRepository.deleteTagDefinition(tag.id)
                Toast.makeText(this@TagManageActivity, "✅ 已删除标签", Toast.LENGTH_SHORT).show()
                loadTags()
            } catch (e: Exception) {
                Timber.e(e, "删除标签失败")
                Toast.makeText(this@TagManageActivity, "❌ 删除失败", Toast.LENGTH_SHORT).show()
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
