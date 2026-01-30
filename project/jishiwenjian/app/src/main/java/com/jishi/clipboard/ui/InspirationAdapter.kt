package com.jishi.clipboard.ui

import android.graphics.Color
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.jishi.clipboard.R
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.ui.adapter.ImagePreviewAdapter
import com.jishi.clipboard.util.ImageUtils
import com.jishi.clipboard.utils.CardStyleHelper
import com.jishi.clipboard.utils.TagParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 灵感列表适配器（使用数据库实体）
 */
class InspirationAdapter(
    private val onItemClick: (ClipboardEntity) -> Unit = {},
    private val onShareClick: (ClipboardEntity) -> Unit = {},
    private val onCopyClick: (ClipboardEntity) -> Unit = {},
    private val onDeleteClick: (ClipboardEntity) -> Unit = {},
    private val onAppendClick: (ClipboardEntity) -> Unit = {}
) : RecyclerView.Adapter<InspirationAdapter.ViewHolder>() {

    private var items = listOf<ClipboardEntity>()

    fun submitList(newItems: List<ClipboardEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.cardView)
        val indicatorView: View = itemView.findViewById(R.id.indicatorView)
        val tagsChipGroup: ChipGroup = itemView.findViewById(R.id.tagsChipGroup)
        val contentText: TextView = itemView.findViewById(R.id.contentText)
        val imagePreviewRecyclerView: RecyclerView = itemView.findViewById(R.id.imagePreviewRecyclerView)
        val timeText: TextView = itemView.findViewById(R.id.timeText)
        val deleteButton: TextView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inspiration, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 解析内容：分离标签和文本
        val parsed = TagParser.parse(item.content)
        holder.contentText.text = parsed.contentWithoutTags

        // 显示图片（如果有）
        val images = ImageUtils.parseImagesFromJson(item.images)
        if (images.isNotEmpty()) {
            holder.imagePreviewRecyclerView.visibility = View.VISIBLE

            // 初始化图片适配器（只读模式，不显示删除按钮）
            val imageAdapter = ImagePreviewAdapter(
                onDeleteClick = {}, // 列表中不支持删除
                onImageClick = null // 可以后续添加查看大图功能
            )

            holder.imagePreviewRecyclerView.apply {
                layoutManager = GridLayoutManager(holder.itemView.context, 3)
                adapter = imageAdapter
            }

            imageAdapter.submitList(images)
        } else {
            holder.imagePreviewRecyclerView.visibility = View.GONE
        }

        // 显示标签
        holder.tagsChipGroup.removeAllViews()
        parsed.tags.forEach { tagName ->
            val chip = createChip(tagName, holder.tagsChipGroup)
            holder.tagsChipGroup.addView(chip)
        }

        // 设置时间
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        holder.timeText.text = sdf.format(Date(item.createdAt))

        // 点击事件
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        // 删除按钮点击事件
        holder.deleteButton.setOnClickListener {
            onDeleteClick(item)
        }

        // 长按显示菜单
        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, item)
            true
        }
    }

    private fun showPopupMenu(view: View, item: ClipboardEntity) {
        PopupMenu(view.context, view).apply {
            menuInflater.inflate(R.menu.item_popup_menu, menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_share -> {
                        onShareClick(item)
                        true
                    }
                    R.id.menu_copy -> {
                        onCopyClick(item)
                        true
                    }
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

    private fun createChip(tagName: String, parent: ViewGroup): Chip {
        val chip = Chip(parent.context)
        chip.text = tagName
        chip.chipBackgroundColor = ColorStateList.valueOf(
            Color.parseColor("#E5E7EB")
        )
        chip.setTextColor(Color.parseColor("#374151"))
        chip.chipCornerRadius = dpToPx(parent.context, 8f).toFloat()
        chip.textSize = 12f
        chip.layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, dpToPx(parent.context, 8f), 0)
        }
        return chip
    }

    private fun dpToPx(context: android.content.Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
}
