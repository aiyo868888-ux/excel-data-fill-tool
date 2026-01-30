package com.jishi.clipboard.ui.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jishi.clipboard.R
import com.jishi.clipboard.data.richeditor.ContentItem
import java.io.File

/**
 * 富文本编辑器适配器
 * 支持文本、图片等多种内容类型的混合编辑
 */
class RichTextEditorAdapter(
    private val onTextChange: (String, Int) -> Unit, // (文本, position)
    private val onImageDelete: (Int) -> Unit,
    private val onItemImageClick: (String) -> Unit = {}
) : ListAdapter<ContentItem, RecyclerView.ViewHolder>(ContentItemDiffCallback()) {

    companion object {
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
        const val VIEW_TYPE_VOICE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ContentItem.TextItem -> VIEW_TYPE_TEXT
            is ContentItem.ImageItem -> VIEW_TYPE_IMAGE
            is ContentItem.VoiceItem -> VIEW_TYPE_VOICE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_editor_text, parent, false)
                TextViewHolder(view, onTextChange)
            }
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_editor_image, parent, false)
                ImageViewHolder(view, onImageDelete, onItemImageClick)
            }
            VIEW_TYPE_VOICE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_editor_voice, parent, false)
                VoiceViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is TextViewHolder -> holder.bind(item as ContentItem.TextItem, position)
            is ImageViewHolder -> holder.bind(item as ContentItem.ImageItem, position)
            is VoiceViewHolder -> holder.bind(item as ContentItem.VoiceItem)
        }
    }

    /**
     * 文本 ViewHolder
     */
    class TextViewHolder(
        itemView: View,
        private val onTextChange: (String, Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val etContent: EditText = itemView.findViewById(R.id.etContent)

        fun bind(item: ContentItem.TextItem, position: Int) {
            // 移除之前的监听器（防止重复添加）
            etContent.tag = null

            etContent.setText(item.text)
            // 确保光标在文本末尾
            val textLength = etContent.text?.length ?: 0
            etContent.setSelection(textLength.coerceAtLeast(0))

            // 监听文本变化
            etContent.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 标记此监听器，防止重复添加
                    etContent.tag = this
                    onTextChange(s?.toString() ?: "", position)
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    /**
     * 图片 ViewHolder
     */
    class ImageViewHolder(
        itemView: View,
        private val onDelete: (Int) -> Unit,
        private val onImageClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: android.widget.ImageView = itemView.findViewById(R.id.imageView)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val etCaption: EditText = itemView.findViewById(R.id.etCaption)

        fun bind(item: ContentItem.ImageItem, position: Int) {
            // 使用 Glide 加载图片
            com.bumptech.glide.Glide.with(itemView.context)
                .load(File(item.imagePath))
                .placeholder(R.drawable.ic_placeholder)
                .into(imageView)

            // 图片说明
            etCaption.setText(item.caption)

            // 删除按钮
            btnDelete.setOnClickListener { onDelete(position) }

            // 点击查看大图
            imageView.setOnClickListener { onImageClick(item.imagePath) }
        }
    }

    /**
     * 语音 ViewHolder（预留）
     */
    class VoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ContentItem.VoiceItem) {
            // TODO: 实现语音播放器 UI
        }
    }

    /**
     * DiffUtil 回调
     */
    private class ContentItemDiffCallback : DiffUtil.ItemCallback<ContentItem>() {
        override fun areItemsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean {
            return oldItem == newItem
        }
    }
}
