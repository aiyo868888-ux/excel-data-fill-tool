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
import com.jishi.clipboard.ui.InspirationAdapter
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import com.jishi.clipboard.utils.ShareHelper
import com.jishi.clipboard.utils.TagParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 灵感 Fragment
 * 显示灵感类型的内容（从数据库读取）
 */
@AndroidEntryPoint
class InspirationFragment : Fragment() {

    @Inject
    lateinit var unifiedRepository: UnifiedContentRepository

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyLayout: LinearLayout
    private lateinit var fabAdd: FloatingActionButton

    private val items = mutableListOf<ClipboardEntity>()

    private val adapter = InspirationAdapter(
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
        onDeleteClick = { item ->
            showDeleteDialog(item)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recentRecyclerView)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        fabAdd = view.findViewById(R.id.fabAdd)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            showClipboardEditDialog()
        }
    }

    /**
     * 监听数据库变化
     */
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                unifiedRepository.observeInspirations().collect { data ->
                    Timber.d("========== 灵感数据更新 ==========")
                    Timber.d("收到数据数量: ${data.size}")
                    data.forEachIndexed { index, item ->
                        Timber.d("  [$index] id=${item.id}, 内容=${item.content.take(30)}...")
                    }
                    Timber.d("===================================")

                    items.clear()
                    items.addAll(data)
                    adapter.submitList(items.toList())

                    // 标签从内容解析，无需预加载

                    updateEmptyState()
                }
            }
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Timber.d("开始加载灵感数据")
                // 从数据库读取数据
                val data = unifiedRepository.getInspirations()

                items.clear()
                items.addAll(data)
                adapter.submitList(items.toList())

                updateEmptyState()
                Timber.d("灵感数据加载完成: ${data.size} 条")
            } catch (e: Exception) {
                Timber.e(e, "加载灵感失败")
                Toast.makeText(requireContext(), "❌ 加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState() {
        if (items.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
        }
    }

    private fun showClipboardEditDialog() {
        Timber.d("打开剪贴板编辑对话框（默认标签：灵感）")
        val dialog = ClipboardEditDialogFragment.newInstance()
        dialog.setDefaultTag("灵感")

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
        dialog.setDefaultTag("灵感")

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
            .setMessage("确定要删除这条灵感吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteItem(item)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem(item: ClipboardEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                unifiedRepository.deleteInspiration(item.id)
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
        fun newInstance() = InspirationFragment()
    }
}
