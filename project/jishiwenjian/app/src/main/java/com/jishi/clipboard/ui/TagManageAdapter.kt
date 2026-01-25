package com.jishi.clipboard.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.R
import com.jishi.clipboard.data.TagDefinition

/**
 * 标签管理列表适配器（支持层级显示）
 */
class TagManageAdapter(
    private val onEditClick: (TagDefinition) -> Unit,
    private val onDeleteClick: (TagDefinition) -> Unit,
    private val allTags: List<TagDefinition> = emptyList()
) : ListAdapter<TagDefinition, TagManageAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag_manage, parent, false)
        return ViewHolder(view, onEditClick, onDeleteClick, allTags)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        view: View,
        private val onEditClick: (TagDefinition) -> Unit,
        private val onDeleteClick: (TagDefinition) -> Unit,
        private val allTags: List<TagDefinition>
    ) : RecyclerView.ViewHolder(view) {
        private val container: LinearLayout = view.findViewById(R.id.tagContainer)
        private val indentIndicator: View = view.findViewById(R.id.indentIndicator)
        private val colorIndicator: View = view.findViewById(R.id.colorIndicator)
        private val nameText: TextView = view.findViewById(R.id.tagName)
        private val parentTagText: TextView = view.findViewById(R.id.parentTagName)
        private val usageText: TextView = view.findViewById(R.id.tagUsage)
        private val btnEdit: View = view.findViewById(R.id.btnEdit)
        private val btnDelete: View = view.findViewById(R.id.btnDelete)

        fun bind(item: TagDefinition) {
            nameText.text = item.name
            usageText.text = "使用 ${item.useCount} 次"

            // 设置层级缩进
            val indentPx = item.level * 48
            (indentIndicator.layoutParams as ViewGroup.LayoutParams).width = indentPx

            // 显示父标签名称
            if (item.parentId != null) {
                val parentTag = allTags.find { it.id == item.parentId }
                if (parentTag != null) {
                    parentTagText.text = "${parentTag.name} ·"
                    parentTagText.visibility = View.VISIBLE
                } else {
                    parentTagText.visibility = View.GONE
                }
            } else {
                parentTagText.visibility = View.GONE
            }

            // 设置颜色
            try {
                colorIndicator.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    Color.parseColor(item.color)
                )
            } catch (e: Exception) {
                // 颜色解析失败，使用默认颜色
            }

            itemView.setOnClickListener { onEditClick(item) }
            btnEdit.setOnClickListener { onEditClick(item) }
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TagDefinition>() {
        override fun areItemsTheSame(old: TagDefinition, new: TagDefinition) = old.id == new.id
        override fun areContentsTheSame(old: TagDefinition, new: TagDefinition) = old == new
    }
}
