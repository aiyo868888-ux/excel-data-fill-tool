package com.jishi.clipboard.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.jishi.clipboard.R
import com.jishi.clipboard.data.json.TodoItem
import com.jishi.clipboard.utils.CardStyleHelper
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 待办事项适配器
 */
class TodoAdapter(
    private val onItemClick: (TodoItem) -> Unit,
    private val onCheckChange: (TodoItem, Boolean) -> Unit,
    private val onDeleteClick: (TodoItem) -> Unit,
    private val onAppendClick: (TodoItem) -> Unit = {}
) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    private var items = listOf<TodoItem>()

    fun submitList(newItems: List<TodoItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        val indicatorView: View = itemView.findViewById(R.id.indicatorView)
        val priorityText: TextView = itemView.findViewById(R.id.priorityTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val taskText: TextView = itemView.findViewById(R.id.taskTextView)
        val tagsContainer: com.google.android.material.chip.ChipGroup = itemView.findViewById(R.id.tagsChipGroup)
        val timeText: TextView = itemView.findViewById(R.id.timeTextView)
        val deleteButton: TextView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.taskText.text = item.task

        // 设置完成状态
        val isCompleted = item.status == "COMPLETED"
        holder.checkBox.setOnCheckedChangeListener(null)  // 先清除旧的 listener
        holder.checkBox.isChecked = isCompleted
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckChange(item, isChecked)
        }

        // 已完成任务视觉样式
        if (isCompleted) {
            holder.taskText.paintFlags = holder.taskText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskText.alpha = 0.6f
            holder.cardView.alpha = 0.7f
        } else {
            holder.taskText.paintFlags = holder.taskText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.taskText.alpha = 1.0f
            holder.cardView.alpha = 1.0f
        }

        // 设置优先级样式
        setPriorityStyle(holder, item.priority)

        // 设置标签
        holder.tagsContainer.removeAllViews()
        item.tags.forEach { tagName ->
            if (tagName.isNotBlank()) {
                val chip = Chip(holder.tagsContainer.context).apply {
                    text = tagName
                    textSize = 12f
                    chipCornerRadius = 12f
                }
                holder.tagsContainer.addView(chip)
            }
        }

        // 设置截止时间
        if (item.dueTimestamp != null) {
            holder.timeText.visibility = View.VISIBLE
            holder.timeText.text = formatTimestamp(item.dueTimestamp)
        } else {
            holder.timeText.visibility = View.GONE
        }

        // 删除按钮
        holder.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }

        // 点击事件
        holder.itemView.setOnClickListener {
            Timber.d("卡片被点击: id=${item.id}, task=${item.task}")
            onItemClick(item)
        }

        // 长按显示菜单
        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, item)
            true
        }
    }

    private fun showPopupMenu(view: View, item: TodoItem) {
        android.widget.PopupMenu(view.context, view).apply {
            menuInflater.inflate(R.menu.item_popup_menu, menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_share -> true
                    R.id.menu_copy -> true
                    R.id.menu_append -> {
                        onAppendClick(item)
                        true
                    }
                    R.id.menu_delete -> {
                        onDeleteClick(item)
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    override fun getItemCount(): Int = items.size

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun setPriorityStyle(holder: ViewHolder, priority: String) {
        val emoji = when (priority) {
            "HIGH" -> "🔴"
            "MEDIUM" -> "🟡"
            "LOW" -> "🟢"
            else -> "🟡"
        }
        holder.priorityText.text = emoji
    }
}
