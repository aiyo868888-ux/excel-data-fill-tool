package com.jishi.clipboard.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.R
import com.jishi.clipboard.data.TagWithCount
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.repository.TagRepository
import com.jishi.clipboard.ui.TagAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 标签管理 Fragment
 * 显示所有标签、筛选历史记录
 */
@AndroidEntryPoint
class TagsFragment : Fragment() {

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    @Inject
    lateinit var tagRepository: TagRepository

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private val adapter = TagAdapter(
        onItemClick = { tag -> openTagDetail(tag.name) },
        onDeleteClick = { tagName -> showDeleteDialog(tagName) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = requireView().findViewById(R.id.recyclerView)
        emptyView = requireView().findViewById(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadTags()
    }

    private fun loadTags() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                clipboardRepository.getAllTags().collect { tags ->
                    if (tags.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                        // 移除setText调用，因为emptyView现在是LinearLayout，文本已在XML中定义
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        adapter.submitList(tags)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "加载标签失败")
            }
        }
    }

    private fun openTagDetail(tagName: String) {
        val intent = Intent(requireContext(), com.jishi.clipboard.ui.TagDetailActivity::class.java).apply {
            putExtra(com.jishi.clipboard.ui.TagDetailActivity.EXTRA_TAG_NAME, tagName)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(tagName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除标签")
            .setMessage("确定要删除标签「$tagName」吗？\n\n注意：删除标签不会删除该标签下的剪贴板内容。")
            .setPositiveButton("删除") { _, _ ->
                deleteTag(tagName)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteTag(tagName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 通过 tagName 获取 tagDefinition，然后删除
                val tagDef = tagRepository.getTagDefinitionByName(tagName)
                if (tagDef != null) {
                    tagRepository.deleteTagDefinition(tagDef.id)
                    Toast.makeText(requireContext(), "✅ 已删除标签", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "❌ 标签不存在", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "删除标签失败")
                Toast.makeText(requireContext(), "❌ 删除失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance() = TagsFragment()
    }
}
