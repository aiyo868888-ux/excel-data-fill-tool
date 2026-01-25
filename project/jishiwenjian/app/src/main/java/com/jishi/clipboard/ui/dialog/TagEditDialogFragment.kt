package com.jishi.clipboard.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.jishi.clipboard.R
import com.jishi.clipboard.data.TagDefinition
import com.jishi.clipboard.repository.TagRepository
import com.jishi.clipboard.ui.ColorPickerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView

/**
 * 标签编辑对话框
 */
@AndroidEntryPoint
class TagEditDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var tagRepository: TagRepository

    private lateinit var nameInput: TextInputEditText
    private lateinit var parentTagDropdown: AutoCompleteTextView
    private lateinit var colorRecyclerView: RecyclerView
    private lateinit var selectedColorPreview: View
    private lateinit var dialogTitle: View

    private var editTag: TagDefinition? = null
    private var selectedParentId: Long? = null
    private var selectedColor = "#4ECDC4"
    private var onTagSavedListener: (() -> Unit)? = null
    private val rootTags = mutableListOf<TagDefinition>()

    private val colorAdapter = ColorPickerAdapter { color ->
        selectedColor = color
        updateColorPreview()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        bottomSheetDialog.behavior.isDraggable = true
        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_tag_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupColorPicker()
        handleArguments()
    }

    private fun initViews() {
        nameInput = requireView().findViewById(R.id.nameInput)
        parentTagDropdown = requireView().findViewById(R.id.parentTagDropdown)
        colorRecyclerView = requireView().findViewById(R.id.colorRecyclerView)
        selectedColorPreview = requireView().findViewById(R.id.selectedColorPreview)
        dialogTitle = requireView().findViewById(R.id.dialogTitle)

        // 保存按钮
        requireView().findViewById<View>(R.id.btnSave).setOnClickListener {
            saveTag()
        }

        // 取消按钮
        requireView().findViewById<View>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        loadRootTags()
    }

    private fun loadRootTags() {
        lifecycleScope.launch {
            try {
                tagRepository.getRootTags().collect { tags ->
                    rootTags.clear()
                    rootTags.addAll(tags)
                    setupParentDropdown()
                }
            } catch (e: Exception) {
                Timber.e(e, "加载根标签失败")
            }
        }
    }

    private fun setupParentDropdown() {
        val items = mutableListOf("无（作为根标签）")
        items.addAll(rootTags.map { it.name })

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        parentTagDropdown.setAdapter(adapter)

        parentTagDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedParentId = if (position == 0) null else rootTags[position - 1].id
        }
    }

    private fun setupColorPicker() {
        val colors = listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
            "#98D8C8", "#F39C12", "#9B59B6", "#3498DB",
            "#E74C3C", "#2ECC71", "#F39C12", "#1ABC9C"
        )

        colorRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        colorRecyclerView.adapter = colorAdapter
        colorAdapter.submitList(colors)

        if (editTag != null) {
            selectedColor = editTag!!.color
            updateColorPreview()
        }
    }

    private fun updateColorPreview() {
        try {
            selectedColorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor(selectedColor)
            )
        } catch (e: Exception) {
            Timber.e(e, "颜色解析失败: $selectedColor")
        }
    }

    private fun handleArguments() {
        @Suppress("DEPRECATION")
        editTag = arguments?.getParcelable<TagDefinition>(ARG_TAG)

        if (editTag != null) {
            (dialogTitle as TextView).text = "编辑标签"
            nameInput.setText(editTag!!.name)
            nameInput.setSelection(editTag!!.name.length)
            selectedColor = editTag!!.color
            selectedParentId = editTag!!.parentId
            updateColorPreview()
            updateParentDropdown()
        }
    }

    private fun updateParentDropdown() {
        if (editTag?.parentId != null) {
            val parent = rootTags.find { it.id == editTag!!.parentId }
            parentTagDropdown.setText(parent?.name ?: "无（作为根标签）", false)
        }
    }

    private fun saveTag() {
        val name = nameInput.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            nameInput.error = "标签名称不能为空"
            return
        }

        lifecycleScope.launch {
            try {
                if (editTag != null) {
                    // 更新标签
                    val level = if (selectedParentId == null) 0 else 1
                    val updatedTag = editTag!!.copy(
                        name = name,
                        color = selectedColor,
                        parentId = selectedParentId,
                        level = level
                    )
                    tagRepository.updateTagDefinition(updatedTag)
                    Toast.makeText(requireContext(), "✅ 已更新标签", Toast.LENGTH_SHORT).show()
                } else {
                    // 创建新标签
                    // 检查是否已存在同名标签
                    val existing = tagRepository.getTagDefinitionByName(name)
                    if (existing != null) {
                        nameInput.error = "标签名称已存在"
                        return@launch
                    }

                    val maxOrder = try {
                        tagRepository.getAllTagDefinitions().first().maxOfOrNull { it.displayOrder } ?: 0
                    } catch (e: Exception) {
                        0
                    }

                    val level = if (selectedParentId == null) 0 else 1

                    val newTag = TagDefinition(
                        name = name,
                        color = selectedColor,
                        displayOrder = maxOrder + 1,
                        parentId = selectedParentId,
                        level = level
                    )
                    tagRepository.insertTagDefinition(newTag)
                    Toast.makeText(requireContext(), "✅ 已添加标签", Toast.LENGTH_SHORT).show()
                }

                onTagSavedListener?.invoke()
                dismiss()
            } catch (e: Exception) {
                Timber.e(e, "保存标签失败")
                Toast.makeText(requireContext(), "❌ 保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setOnTagSavedListener(listener: () -> Unit) {
        onTagSavedListener = listener
    }

    companion object {
        private const val ARG_TAG = "tag"

        fun newInstance(tag: TagDefinition? = null): TagEditDialogFragment {
            return TagEditDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TAG, tag)
                }
            }
        }
    }
}
