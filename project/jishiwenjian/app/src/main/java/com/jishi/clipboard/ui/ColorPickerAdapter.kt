package com.jishi.clipboard.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.R

/**
 * 颜色选择器适配器
 */
class ColorPickerAdapter(
    private val onColorSelected: (String) -> Unit
) : ListAdapter<String, ColorPickerAdapter.ViewHolder>(DiffCallback()) {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_picker, parent, false)
        return ViewHolder(view) { position ->
            val prevPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(prevPosition)
            notifyItemChanged(selectedPosition)
            onColorSelected(getItem(position))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    class ViewHolder(
        view: View,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val colorItem: View = view.findViewById(R.id.colorItem)

        fun bind(color: String, isSelected: Boolean) {
            try {
                colorItem.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    Color.parseColor(color)
                )
            } catch (e: Exception) {
                // 颜色解析失败，使用默认颜色
            }

            // 选中状态：添加边框
            colorItem.isSelected = isSelected
            if (isSelected) {
                colorItem.alpha = 1f
                colorItem.scaleX = 1.1f
                colorItem.scaleY = 1.1f
            } else {
                colorItem.alpha = 0.7f
                colorItem.scaleX = 1f
                colorItem.scaleY = 1f
            }

            itemView.setOnClickListener { onClick(bindingAdapterPosition) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(old: String, new: String) = old == new
        override fun areContentsTheSame(old: String, new: String) = old == new
    }
}
