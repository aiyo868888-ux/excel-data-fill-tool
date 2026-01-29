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
import com.jishi.clipboard.data.json.TodoItem
import com.jishi.clipboard.repository.UnifiedContentRepository
import com.jishi.clipboard.ui.TodoAdapter
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import com.jishi.clipboard.utils.ShareHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 待办事项 Fragment
 * 显示待办类型的内容（从数据库读取）
 */
@AndroidEntryPoint
class TodoFragment : Fragment() {

    @Inject
    lateinit var unifiedRepository: UnifiedContentRepository

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyLayout: LinearLayout
    private lateinit var fabAdd: FloatingActionButton

    private val items = mutableListOf<TodoItem>()

    private val adapter = TodoAdapter(
        onItemClick = { item ->
            showEditDialog(item)
        },
        onCheckChange = { item, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val newStatus = if (isChecked) "COMPLETED" else "PENDING"
                    val id = item.id.toLong()
                    unifiedRepository.updateTodoStatus(id, newStatus)
                    Timber.d("更新待办状态: id=$id, status=$newStatus")
                } catch (e: Exception) {
                    Timber.e(e, "更新状态失败")
                    Toast.makeText(requireContext(), "❌ 更新失败", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onDeleteClick = { item ->
            showDeleteDialog(item)
        },
        onAppendClick = { item ->
            showAppendDialog(item)
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
        // 移除滑动删除功能，避免误操作和UI不同步问题
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
                unifiedRepository.observeTodos().collect { entities ->
                    Timber.d("========== 待办数据更新 ==========")
                    Timber.d("收到 ClipboardEntity 数量: ${entities.size}")

                    // 转换为 TodoItem，并为每个 entity 预加载标签
                    val todoItems = entities.map { entity ->
                        val tags = unifiedRepository.getTagsForClipboard(entity.id)
                            .map { it.name }
                        unifiedRepository.clipboardToTodoItem(entity, tags)
                    }

                    Timber.d("转换后 TodoItem 数量: ${todoItems.size}")
                    todoItems.forEachIndexed { index, item ->
                        Timber.d("  [$index] id=${item.id}, task=${item.task.take(30)}..., status=${item.status}, priority=${item.priority}")
                    }
                    Timber.d("===================================")

                    items.clear()
                    items.addAll(todoItems)
                    adapter.submitList(items.toList())
                    updateEmptyState()
                }
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
        Timber.d("打开剪贴板编辑对话框（默认标签：待办）")
        val dialog = ClipboardEditDialogFragment.newInstance()
        dialog.setDefaultTag("待办")

        dialog.setOnSaveListener { content, tags ->
            Timber.d("对话框保存回调触发: $content, 标签=$tags")
            safeDismissDialog(dialog)
            // Flow 会自动更新列表
            Toast.makeText(requireContext(), "✅ 已保存", Toast.LENGTH_SHORT).show()
        }

        dialog.setOnDismissListener {
            // 对话框关闭时的处理（可选）
        }

        dialog.show(childFragmentManager, "ClipboardEditDialog")
    }

    private fun showEditDialog(item: TodoItem) {
        Timber.d("打开编辑对话框: id=${item.id}, task=${item.task}")
        val dialog = ClipboardEditDialogFragment.newInstance(
            editMode = true,
            editClipboardId = item.id.toLong()
        )
        dialog.setDefaultTag("待办")

        dialog.setOnSaveListener { content, tags ->
            Timber.d("编辑保存回调触发: $content, 标签=$tags")
            safeDismissDialog(dialog)
            // Flow 会自动更新列表
            Toast.makeText(requireContext(), "✅ 已更新", Toast.LENGTH_SHORT).show()
        }

        dialog.show(childFragmentManager, "EditTodoDialog")
    }

    private fun showAppendDialog(item: TodoItem) {
        val dialog = com.jishi.clipboard.ui.dialog.AppendDialog(
            context = requireContext(),
            originalContent = item.task,
            onConfirm = { appendedContent ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val id = item.id.toLong()
                        unifiedRepository.appendContent(id, appendedContent)
                        Toast.makeText(requireContext(), "✅ 已追加到笔记", Toast.LENGTH_SHORT).show()
                        Timber.d("追加成功: id=$id")
                    } catch (e: Exception) {
                        Timber.e(e, "追加失败")
                        Toast.makeText(requireContext(), "❌ 追加失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        dialog.show()
    }

    private fun showDeleteDialog(item: TodoItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定要删除这条待办吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteItem(item)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem(item: TodoItem) {
        Timber.d("开始删除: id=${item.id}, task=${item.task}")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val id = item.id.toLong()
                Timber.d("转换后的 Long ID: $id")
                unifiedRepository.deleteTodo(id)
                // 不再需要手动调用 loadData()，Flow 会自动更新
                Toast.makeText(requireContext(), "✅ 已删除", Toast.LENGTH_SHORT).show()
                Timber.d("删除成功: id=$id")
            } catch (e: Exception) {
                Timber.e(e, "删除失败")
                Toast.makeText(requireContext(), "❌ 删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun safeDismissDialog(dialog: ClipboardEditDialogFragment) {
        try {
            dialog.dismiss()
        } catch (e: Exception) {
            Timber.e(e, "关闭对话框失败")
        }
    }

    companion object {
        fun newInstance() = TodoFragment()
    }
}
