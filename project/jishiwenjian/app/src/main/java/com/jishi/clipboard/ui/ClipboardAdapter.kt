package com.jishi.clipboard.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.databinding.ItemClipboardBinding
import com.jishi.clipboard.utils.CardStyleHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 剪贴板列表适配器
 * 支持卡片式样式，根据内容类型自动应用颜色
 */
class ClipboardAdapter(
    private val onDeleteClick: (ClipboardEntity) -> Unit,
    private val onItemClick: (ClipboardEntity) -> Unit
) : ListAdapter<ClipboardEntity, ClipboardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClipboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onDeleteClick, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemClipboardBinding,
        private val onDeleteClick: (ClipboardEntity) -> Unit,
        private val onItemClick: (ClipboardEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ClipboardEntity) {
            binding.contentText.text = item.content
            binding.timeText.text = formatTime(item.createdAt)

            // 点击整个卡片跳转到详情页
            binding.root.setOnClickListener {
                onItemClick(item)
            }

            // 删除按钮
            binding.deleteButton.setOnClickListener {
                onDeleteClick(item)
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ClipboardEntity>() {
        override fun areItemsTheSame(oldItem: ClipboardEntity, newItem: ClipboardEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClipboardEntity, newItem: ClipboardEntity): Boolean {
            return oldItem == newItem
        }
    }
}
