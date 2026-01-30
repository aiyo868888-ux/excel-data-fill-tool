package com.jishi.clipboard.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jishi.clipboard.R
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.data.richeditor.ContentItem
import com.jishi.clipboard.data.richeditor.toContentItems
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.ui.adapter.RichTextEditorAdapter
import com.jishi.clipboard.util.ImageUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 富文本编辑器对话框
 * 支持文本、图片混合编辑
 */
@AndroidEntryPoint
class RichTextEditorDialog : BottomSheetDialogFragment() {

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    private var _binding: com.jishi.clipboard.databinding.DialogRichTextEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RichTextEditorAdapter
    private val contentItems = mutableListOf<ContentItem>()

    private var onSaveListener: ((List<ContentItem>) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null
    private var initialEntity: ClipboardEntity? = null

    // 结果接收
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleImageSelected(it) }
        }

    fun setOnSaveListener(listener: (List<ContentItem>) -> Unit) {
        onSaveListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    fun setInitialContent(entity: ClipboardEntity) {
        initialEntity = entity
        contentItems.clear()
        contentItems.addAll(entity.toContentItems())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = com.jishi.clipboard.databinding.DialogRichTextEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        // 设置圆角背景
        bottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 设置软键盘调整模式
        bottomSheetDialog.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // 可拖拽
        bottomSheetDialog.behavior.isDraggable = true

        // 设置展开状态
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                // 使用90%屏幕高度，保留可拖拽性
                behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.9).toInt()
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return bottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbar()
        setupInitialContent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener?.invoke()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = RichTextEditorAdapter(
            onTextChange = { text, position ->
                updateTextItem(text, position)
            },
            onImageDelete = { position ->
                deleteItem(position)
            },
            onItemImageClick = { imagePath ->
                showImagePreview(imagePath)
            }
        )

        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = this@RichTextEditorDialog.adapter
        }

        adapter.submitList(contentItems.toList())
    }

    private fun setupToolbar() {
        binding.btnAddText.setOnClickListener {
            addTextItem()
        }

        binding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 语音按钮预留
        binding.btnAddVoice.setOnClickListener {
            Toast.makeText(requireContext(), "语音功能即将推出", Toast.LENGTH_SHORT).show()
        }

        binding.btnSave.setOnClickListener {
            saveContent()
        }
    }

    private fun setupInitialContent() {
        if (contentItems.isEmpty()) {
            // 如果没有内容，默认添加一个文本输入框
            addTextItem()
        }
    }

    private fun addTextItem() {
        val textItem = ContentItem.TextItem()
        contentItems.add(textItem)
        adapter.submitList(contentItems.toList())

        // 滚动到底部
        binding.recyclerView.post {
            binding.recyclerView.smoothScrollToPosition(contentItems.size - 1)
        }
    }

    private fun handleImageSelected(uri: Uri) {
        lifecycleScope.launch {
            try {
                // 保存图片到本地
                val imagePath = ImageUtils.saveImageToLocal(requireContext(), uri)

                if (imagePath != null) {
                    // 获取图片尺寸
                    val options = android.graphics.BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    android.graphics.BitmapFactory.decodeFile(imagePath, options)

                    val imageItem = ContentItem.ImageItem(
                        imagePath = imagePath,
                        width = options.outWidth,
                        height = options.outHeight
                    )

                    contentItems.add(imageItem)
                    adapter.submitList(contentItems.toList())

                    // 滚动到新图片
                    binding.recyclerView.post {
                        binding.recyclerView.smoothScrollToPosition(contentItems.size - 1)
                    }
                } else {
                    Toast.makeText(requireContext(), "图片保存失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "保存图片失败")
                Toast.makeText(requireContext(), "图片保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTextItem(text: String, position: Int) {
        if (position < contentItems.size) {
            val item = contentItems[position]
            if (item is ContentItem.TextItem) {
                contentItems[position] = item.copy(text = text)
            }
        }
    }

    private fun deleteItem(position: Int) {
        if (position < contentItems.size) {
            val item = contentItems[position]

            // 如果是图片，删除文件
            if (item is ContentItem.ImageItem) {
                ImageUtils.deleteImage(item.imagePath)
            }

            contentItems.removeAt(position)
            adapter.submitList(contentItems.toList())
        }
    }

    private fun saveContent() {
        // 过滤掉空的文本项
        val validItems = contentItems.filterNot {
            it is ContentItem.TextItem && it.text.isBlank()
        }

        if (validItems.isEmpty()) {
            Toast.makeText(requireContext(), "内容不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        onSaveListener?.invoke(validItems)
        dismiss()
    }

    private fun showImagePreview(imagePath: String) {
        // TODO: 实现大图预览
        Toast.makeText(requireContext(), "大图预览功能开发中", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = RichTextEditorDialog()
    }
}
