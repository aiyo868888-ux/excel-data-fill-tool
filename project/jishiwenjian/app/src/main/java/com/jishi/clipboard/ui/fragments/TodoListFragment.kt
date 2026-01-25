package com.jishi.clipboard.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.jishi.clipboard.R
import com.jishi.clipboard.data.TodoEntity
import com.jishi.clipboard.repository.TodoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 待办事项列表 Fragment
 */
@AndroidEntryPoint
class TodoListFragment : Fragment() {

    @Inject
    lateinit var todoRepository: TodoRepository

    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: View
    private lateinit var filterTabLayout: TabLayout

    private lateinit var todoAdapter: TodoAdapter
    private var currentFilter = Filter.ALL

    private enum class Filter {
        ALL, PENDING, COMPLETED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupFilterTabs()
        loadTodos()
    }

    private fun initViews(view: View) {
        todoRecyclerView = view.findViewById(R.id.todoRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        filterTabLayout = view.findViewById(R.id.filterTabLayout)
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            onTodoClick = { todo ->
                // 打开待办详情页面
                com.jishi.clipboard.ui.TodoDetailActivity.start(requireContext(), todo.id)
            },
            onTodoCheckChange = { todo, isChecked ->
                if (isChecked) {
                    markAsCompleted(todo)
                } else {
                    markAsPending(todo)
                }
            },
            onTodoDelete = { todo ->
                deleteTodo(todo)
            }
        )

        todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        todoRecyclerView.adapter = todoAdapter
    }

    private fun setupFilterTabs() {
        filterTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    0 -> Filter.ALL
                    1 -> Filter.PENDING
                    2 -> Filter.COMPLETED
                    else -> Filter.ALL
                }
                loadTodos()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadTodos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val todos = when (currentFilter) {
                    Filter.ALL -> todoRepository.getAllTodos().first()
                    Filter.PENDING -> todoRepository.getPendingTodos().first()
                    Filter.COMPLETED -> todoRepository.getCompletedTodos().first()
                }

                Timber.d("加载待办列表: ${todos.size} 条")

                todoAdapter.submitList(todos)

                // 显示/隐藏空状态
                emptyStateLayout.isVisible = todos.isEmpty()

            } catch (e: Exception) {
                Timber.e(e, "加载待办列表失败")
            }
        }
    }

    private fun markAsCompleted(todo: TodoEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                todoRepository.markAsCompleted(todo.id)
                Timber.d("标记为完成: ${todo.task}")
                loadTodos()
                Toast.makeText(requireContext(), "✅ 已完成", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Timber.e(e, "标记完成失败")
            }
        }
    }

    private fun markAsPending(todo: TodoEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                todoRepository.markAsPending(todo.id)
                Timber.d("标记为待办: ${todo.task}")
                loadTodos()
            } catch (e: Exception) {
                Timber.e(e, "标记待办失败")
            }
        }
    }

    private fun deleteTodo(todo: TodoEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                todoRepository.deleteTodo(todo)
                Timber.d("删除待办: ${todo.task}")
                loadTodos()
                Toast.makeText(requireContext(), "🗑️ 已删除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Timber.e(e, "删除失败")
            }
        }
    }

    /**
     * 待办列表适配器
     */
    inner class TodoAdapter(
        private val onTodoClick: (TodoEntity) -> Unit,
        private val onTodoCheckChange: (TodoEntity, Boolean) -> Unit,
        private val onTodoDelete: (TodoEntity) -> Unit
    ) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

        private var todos = listOf<TodoEntity>()

        fun submitList(newTodos: List<TodoEntity>) {
            todos = newTodos
            notifyDataSetChanged()
        }

        inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val checkBox: CheckBox = itemView.findViewById(R.id.todoCheckBox)
            val taskTextView: TextView = itemView.findViewById(R.id.taskTextView)
            val tagsChipGroup: com.google.android.material.chip.ChipGroup = itemView.findViewById(R.id.tagsChipGroup)
            val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
            val deleteImageView: TextView = itemView.findViewById(R.id.deleteImageView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_todo, parent, false)
            return TodoViewHolder(view)
        }

        override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
            val todo = todos[position]

            holder.apply {
                // 任务描述
                taskTextView.text = todo.task

                // 完成状态
                checkBox.isChecked = todo.status == "COMPLETED"
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    onTodoCheckChange(todo, isChecked)
                }

                // 标签
                tagsChipGroup.removeAllViews()
                if (todo.tags.isNotEmpty()) {
                    todo.tags.split(",").forEach { tagName ->
                        if (tagName.isNotBlank()) {
                            val chip = Chip(tagsChipGroup.context).apply {
                                text = tagName
                                textSize = 12f
                                chipCornerRadius = 12f
                            }
                            tagsChipGroup.addView(chip)
                        }
                    }
                }

                // 时间信息
                if (todo.dueTimestamp != null) {
                    timeTextView.isVisible = true
                    timeTextView.text = formatTimestamp(todo.dueTimestamp)
                } else {
                    timeTextView.isVisible = false
                }

                // 删除按钮
                deleteImageView.setOnClickListener {
                    onTodoDelete(todo)
                }

                // 点击整个卡片
                itemView.setOnClickListener {
                    onTodoClick(todo)
                }
            }
        }

        override fun getItemCount(): Int = todos.size

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    companion object {
        fun newInstance() = TodoListFragment()
    }
}
