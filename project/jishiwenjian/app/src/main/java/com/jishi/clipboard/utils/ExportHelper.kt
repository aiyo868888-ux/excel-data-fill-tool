package com.jishi.clipboard.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.ui.dialog.ExportDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 导出功能辅助类
 */
object ExportHelper {

    /**
     * 显示导出对话框
     */
    fun showExportDialog(fragment: Fragment, onExportRequest: (ExportDialog.ExportType) -> Unit) {
        ExportDialog().apply {
            setOnExportListener(onExportRequest)
            show(fragment.childFragmentManager, "export_dialog")
        }
    }

    /**
     * 执行导出操作
     * @param context 上下文
     * @param inspirations 灵感列表
     * @param insights 启发列表
     * @param todos 待办列表
     * @param exportType 导出类型
     * @param tagsMap 标签映射 (entityId -> tags)
     */
    suspend fun exportNotes(
        context: Context,
        inspirations: List<ClipboardEntity>,
        insights: List<ClipboardEntity>,
        todos: List<ClipboardEntity>,
        exportType: ExportDialog.ExportType,
        tagsMap: Map<Long, List<String>> = emptyMap()
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // 保存文件到应用外部缓存目录（不需要存储权限）
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            // 创建图片子目录
            val imagesDir = File(exportDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            // 生成 Markdown 内容
            val markdown = when (exportType) {
                ExportDialog.ExportType.ALL -> MarkdownExporter.exportToMarkdown(
                    inspirations, insights, todos, tagsMap, imagesDir.absolutePath
                )
                ExportDialog.ExportType.INSPIRATION -> MarkdownExporter.exportByType(
                    inspirations, "灵感", tagsMap, imagesDir.absolutePath
                )
                ExportDialog.ExportType.INSIGHT -> MarkdownExporter.exportByType(
                    insights, "启发", tagsMap, imagesDir.absolutePath
                )
                ExportDialog.ExportType.TODO -> MarkdownExporter.exportByType(
                    todos, "待办", tagsMap, imagesDir.absolutePath
                )
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val mdFileName = "及时记_导出_$timestamp.md"
            val mdFile = File(exportDir, mdFileName)

            FileOutputStream(mdFile).use { it.write(markdown.toByteArray(Charsets.UTF_8)) }

            // 创建ZIP文件,包含Markdown和图片
            val zipFileName = "及时记_导出_$timestamp.zip"
            val zipFile = File(exportDir, zipFileName)

            ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                // 添加Markdown文件
                zip.putNextEntry(ZipEntry(mdFileName))
                FileInputStream(mdFile).use { input ->
                    input.copyTo(zip)
                }
                zip.closeEntry()

                // 添加所有图片
                val imageFiles = imagesDir.listFiles() ?: emptyArray()
                imageFiles.forEach { imageFile ->
                    zip.putNextEntry(ZipEntry("images/${imageFile.name}"))
                    FileInputStream(imageFile).use { input ->
                        input.copyTo(zip)
                    }
                    zip.closeEntry()
                }
            }

            Result.success(zipFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 启动分享菜单
     */
    fun shareFile(context: Context, file: File) {
        try {
            // 使用 FileProvider 生成 URI
            val authority = "${context.packageName}.fileprovider"
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "分享导出文件"))
        } catch (e: Exception) {
            Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
