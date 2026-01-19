package com.jishi.clipboard.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.R
import com.jishi.clipboard.data.TagWithCount

/**
 * 标签列表适配器
 */
class TagAdapter(
    private val onItemClick: (TagWithCount) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<TagWithCount, TagAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag, parent, false)
        return ViewHolder(view, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        view: View,
        private val onItemClick: (TagWithCount) -> Unit,
        private val onDeleteClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.tagName)
        private val countText: TextView = view.findViewById(R.id.tagCount)
        private val deleteButton: View = view.findViewById(R.id.deleteTag)

        fun bind(item: TagWithCount) {
            nameText.text = item.name
            countText.text = "${item.count}条记录"

            // 隐藏删除按钮
            deleteButton.visibility = View.GONE

            itemView.setOnClickListener { onItemClick(item) }

            // 长按显示删除按钮
            itemView.setOnLongClickListener {
                deleteButton.visibility = View.VISIBLE
                true // 消费长按事件
            }

            deleteButton.setOnClickListener {
                onDeleteClick(item.name)
                deleteButton.visibility = View.GONE
                true // 消费事件，防止冒泡
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TagWithCount>() {
        override fun areItemsTheSame(old: TagWithCount, new: TagWithCount) = old.name == new.name
        override fun areContentsTheSame(old: TagWithCount, new: TagWithCount) = old == new
    }
}
