package com.jishi.clipboard.utils

import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import com.jishi.clipboard.ui.dialog.ContentTypeSelectionDialog
import java.lang.ref.WeakReference

/**
 * 对话框管理器单例
 * 用于跟踪当前显示的对话框实例
 */
object DialogManager {
    private var currentEditDialog: WeakReference<ClipboardEditDialogFragment>? = null
    private var currentTypeDialog: WeakReference<ContentTypeSelectionDialog>? = null

    // 对话框状态保存
    var savedContent: String = ""
    var savedTags: Set<String> = emptySet()
    var lastClipboardHash: Int = 0
    var currentContentType: String? = null  // 当前选择的内容类型

    /**
     * 检查编辑对话框是否正在显示
     * 不仅检查引用，还验证对话框是否真的附加到 Activity
     */
    fun isEditDialogShowing(): Boolean {
        val dialog = currentEditDialog?.get() ?: return false
        // 检查对话框是否真的在显示（isAdded 检查 Fragment 是否附加到 Activity）
        return try {
            dialog.isAdded && dialog.isVisible && dialog.dialog?.isShowing == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查类型选择对话框是否正在显示
     * 验证对话框是否真的附加到 Activity 并正在显示
     */
    fun isTypeDialogShowing(): Boolean {
        val dialog = currentTypeDialog?.get() ?: return false
        return try {
            dialog.isAdded && dialog.isVisible && dialog.dialog?.isShowing == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查任何对话框是否正在显示
     */
    fun isDialogShowing(): Boolean {
        return isEditDialogShowing() || isTypeDialogShowing()
    }

    /**
     * 设置当前编辑对话框实例
     */
    fun setCurrentEditDialog(dialog: ClipboardEditDialogFragment) {
        currentEditDialog = WeakReference(dialog)
    }

    /**
     * 设置当前类型选择对话框实例
     */
    fun setCurrentTypeDialog(dialog: ContentTypeSelectionDialog) {
        currentTypeDialog = WeakReference(dialog)
    }

    /**
     * 清除当前编辑对话框引用
     */
    fun clearCurrentEditDialog() {
        currentEditDialog = null
    }

    /**
     * 清除当前类型选择对话框引用
     */
    fun clearCurrentTypeDialog() {
        currentTypeDialog = null
    }

    /**
     * 清除所有对话框引用
     */
    fun clearCurrentDialog() {
        currentEditDialog = null
        currentTypeDialog = null
    }

    /**
     * 获取当前编辑对话框实例
     */
    fun getCurrentEditDialog(): ClipboardEditDialogFragment? {
        return currentEditDialog?.get()
    }

    /**
     * 保存对话框状态
     */
    fun saveState(content: String, tags: Set<String>) {
        savedContent = content
        savedTags = tags.toSet()
    }

    /**
     * 清空保存的状态
     */
    fun clearState() {
        savedContent = ""
        savedTags = emptySet()
        lastClipboardHash = 0
        currentContentType = null
    }

    /**
     * 检查剪切板内容是否为新内容
     */
    fun isNewClipboardContent(content: String): Boolean {
        val newHash = content.hashCode()
        val isNew = newHash != lastClipboardHash && lastClipboardHash != 0
        if (isNew) {
            lastClipboardHash = newHash
        }
        return isNew
    }
}
