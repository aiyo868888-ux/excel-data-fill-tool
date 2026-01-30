package com.jishi.clipboard.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jishi.clipboard.R
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.repository.UnifiedContentRepository
import com.jishi.clipboard.ui.InsightAdapter
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import com.jishi.clipboard.utils.ShareHelper
import com.jishi.clipboard.utils.TagParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 启发 Fragment
 * 显示启发类型的内容（从数据库读取）
 */
@AndroidEntryPoint
class InsightFragment : Fragment() {

    @Inject
    lateinit var unifiedRepository: UnifiedContentRepository

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var fabAdd: FloatingActionButton

    private val items = mutableListOf<ClipboardEntity>()

    private val adapter = InsightAdapter(
        onItemClick = { item ->
            // 点击打开编辑对话框
            showEditDialog(item)
        },
        onShareClick = { item ->
            // 从内容提取标签进行分享
            val tags = TagParser.extractTags(item.content)
            ShareHelper.shareCardContent(requireContext(), item.content, tags)
        },
        onCopyClick = { item ->
            ShareHelper.copyToClipboard(requireContext(), item.content)
        },
        onAppendClick = { item ->
            showAppendDialog(item)
        },
        onDeleteClick = { item -> showDeleteDialog(item) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        fabAdd = view.findViewById(R.id.fabAdd)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        fabAdd.setOnClickListener { showClipboardEditDialog() }
    }

    /**
     * 监听数据库变化
     */
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                unifiedRepository.observeInsights().collect { data ->
                    items.clear()
                    items.addAll(data)
                    adapter.submitList(items.toList())

                    // 标签从内容解析，无需预加载

                    updateEmptyState()
                    Timber.d("启发数据更新: ${data.size} 条")
                }
            }
        }
    }

    private fun updateEmptyState() {
        if (items.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    private fun showClipboardEditDialog() {
        Timber.d("打开剪贴板编辑对话框（默认标签：启发）")
        val dialog = ClipboardEditDialogFragment.newInstance()
        dialog.setDefaultTag("启发")

        dialog.setOnSaveListener { content, tags ->
            Timber.d("对话框保存回调触发: $content, 标签=$tags")
            // 关闭对话框
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                Timber.e(e, "关闭对话框失败")
            }
            // Flow 会自动更新列表
            Toast.makeText(requireContext(), "✅ 已保存", Toast.LENGTH_SHORT).show()
        }

        dialog.setOnDismissListener {
            // 对话框关闭时的处理（可选）
        }

        dialog.show(childFragmentManager, "ClipboardEditDialog")
    }

    private fun showEditDialog(item: ClipboardEntity) {
        Timber.d("打开编辑对话框: id=${item.id}")
        val dialog = ClipboardEditDialogFragment.newInstance(
            initialContent = item.content,
            editMode = true,
            editClipboardId = item.id
        )
        dialog.setDefaultTag("启发")

        dialog.setOnSaveListener { content, tags ->
            Timber.d("编辑保存: $content, 标签=$tags")
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                Timber.e(e, "关闭对话框失败")
            }
        }

        dialog.show(childFragmentManager, "ClipboardEditDialog")
    }

    private fun showAppendDialog(item: ClipboardEntity) {
        val dialog = com.jishi.clipboard.ui.dialog.AppendDialog(
            context = requireContext(),
            originalContent = item.content,
            onConfirm = { appendedContent ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        unifiedRepository.appendContent(item.id, appendedContent)
                        Toast.makeText(requireContext(), "✅ 已追加到笔记", Toast.LENGTH_SHORT).show()
                        Timber.d("追加成功: id=${item.id}")
                    } catch (e: Exception) {
                        Timber.e(e, "追加失败")
                        Toast.makeText(requireContext(), "❌ 追加失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        dialog.show()
    }

    private fun showDeleteDialog(item: ClipboardEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定要删除这条启发吗？")
            .setPositiveButton("删除") { _, _ -> deleteItem(item) }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem(item: ClipboardEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                unifiedRepository.deleteInsight(item.id)
                // 不再需要手动调用 loadData()，Flow 会自动更新
                Toast.makeText(requireContext(), "✅ 已删除", Toast.LENGTH_SHORT).show()
                Timber.d("删除成功: id=${item.id}")
            } catch (e: Exception) {
                Timber.e(e, "删除失败")
                Toast.makeText(requireContext(), "❌ 删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance() = InsightFragment()
    }

    private fun TagParser.extractTags(content: String): List<String> {
        val tagPattern = Regex("#([\\u4e00-\\u9fa5a-zA-Z0-9_]+)")
        return tagPattern.findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }
}
