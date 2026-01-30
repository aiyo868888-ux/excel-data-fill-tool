package com.jishi.clipboard.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jishi.clipboard.R
import java.io.File

/**
 * 图片预览适配器
 * 用于在编辑对话框中显示已选择的图片
 */
class ImagePreviewAdapter(
    private val onDeleteClick: (String) -> Unit,
    private val onImageClick: ((String) -> Unit)? = null
) : ListAdapter<String, ImagePreviewAdapter.ViewHolder>(ImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_preview, parent, false)
        return ViewHolder(view, onDeleteClick, onImageClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onDeleteClick: (String) -> Unit,
        private val onImageClick: ((String) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(imagePath: String) {
            // 加载图片
            Glide.with(itemView.context)
                .load(File(imagePath))
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imageView)

            // 删除按钮点击
            btnDelete.setOnClickListener {
                onDeleteClick(imagePath)
            }

            // 图片点击（可选，用于查看大图）
            imageView.setOnClickListener {
                onImageClick?.invoke(imagePath)
            }
        }
    }

    private class ImageDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
