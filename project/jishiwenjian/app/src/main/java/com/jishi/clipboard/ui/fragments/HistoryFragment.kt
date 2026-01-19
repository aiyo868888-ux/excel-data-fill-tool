package com.jishi.clipboard.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.R
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.repository.ClipboardRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.jishi.clipboard.ui.ClipboardAdapter

/**
 * 待办 Fragment
 * 显示所有已保存的笔记本内容
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {

    @Inject
    lateinit var repository: ClipboardRepository

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        loadData()
    }

    private fun initViews() {
        recyclerView = requireView().findViewById(R.id.recyclerView)
        emptyView = requireView().findViewById(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllClipboards().collect { clipboards ->
                updateList(clipboards)
            }
        }
    }

    private fun updateList(clipboards: List<ClipboardEntity>) {
        if (clipboards.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            // 移除setText调用，因为emptyView现在是LinearLayout，文本已在XML中定义
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            adapter.submitList(clipboards)
        }
    }

    private fun openDetailActivity(item: ClipboardEntity) {
        val intent = Intent(requireContext(), com.jishi.clipboard.ui.DetailActivity::class.java).apply {
            putExtra(com.jishi.clipboard.ui.DetailActivity.EXTRA_CLIPBOARD_ID, item.id)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(item: ClipboardEntity) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("删除确认")
            .setMessage("确定要删除这条记录吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteItem(item)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteItem(item: ClipboardEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.deleteClipboard(item)
                Toast.makeText(requireContext(), "✅ 已删除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Timber.e(e, "删除失败")
                Toast.makeText(requireContext(), "❌ 删除失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "HistoryFragment"

        fun newInstance() = HistoryFragment()
    }
}
