package com.jishi.clipboard.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jishi.clipboard.R
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.ui.ClipboardAdapter
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 首页 Fragment
 * 显示最新的剪贴板记录
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var repository: ClipboardRepository

    private lateinit var recentRecyclerView: RecyclerView
    private lateinit var emptyLayout: LinearLayout
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var searchView: SearchView

    private val adapter = ClipboardAdapter(
        onDeleteClick = { item ->
            showDeleteDialog(item)
        },
        onItemClick = { item ->
            openDetailActivity(item)
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
        android.util.Log.d("HomeFragment", "========== HomeFragment.onViewCreated 开始 ==========")
        super.onViewCreated(view, savedInstanceState)

        initViews()
        android.util.Log.d("HomeFragment", "initViews 完成")
        setupFab()
        android.util.Log.d("HomeFragment", "setupFab 完成")
        loadRecentData()
        android.util.Log.d("HomeFragment", "loadRecentData 完成")
        android.util.Log.d("HomeFragment", "========== HomeFragment.onViewCreated 完成 ==========")
    }

    private fun initViews() {
        recentRecyclerView = requireView().findViewById(R.id.recentRecyclerView)
        emptyLayout = requireView().findViewById(R.id.emptyLayout)
        fabAdd = requireView().findViewById(R.id.fabAdd)
        searchView = requireView().findViewById(R.id.searchView)

        recentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recentRecyclerView.adapter = adapter

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

    private fun setupFab() {
        fabAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun loadRecentData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 获取最新的 20 条记录
                repository.getRecentClipboards(20).collect { clipboards ->
                    updateList(clipboards)
                }
            } catch (e: Exception) {
                Timber.e(e, "加载最新记录失败")
            }
        }
    }

    private fun searchClipboards(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val flow = if (query.isEmpty()) {
                    repository.getRecentClipboards(20)
                } else {
                    repository.searchClipboards(query)
                }
                flow.collect { clipboards -> updateList(clipboards) }
            } catch (e: Exception) {
                Timber.e(e, "搜索失败")
            }
        }
    }

    private fun updateList(clipboards: List<ClipboardEntity>) {
        if (clipboards.isEmpty()) {
            recentRecyclerView.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            recentRecyclerView.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
            adapter.submitList(clipboards)
        }
    }

    private fun openDetailActivity(item: ClipboardEntity) {
        val intent = android.content.Intent(requireContext(), com.jishi.clipboard.ui.DetailActivity::class.java).apply {
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
                android.widget.Toast.makeText(requireContext(), "✅ 已删除", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Timber.e(e, "删除失败")
                android.widget.Toast.makeText(requireContext(), "❌ 删除失败", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddDialog() {
        val dialog = ClipboardEditDialogFragment.newInstance(
            initialContent = ""
        )

        dialog.setOnSaveListener { _, _ ->
            // 保存成功后会自动刷新（因为使用 Flow）
        }

        dialog.show(childFragmentManager, "ClipboardEditDialog")
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
