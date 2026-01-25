package com.jishi.clipboard.utils

import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.data.TagDefinition
import com.jishi.clipboard.repository.ClipboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Markdown 导出器
 * 将剪贴板数据导出为格式化的 Markdown 文件
 */
class MarkdownExporter(
    private val repository: ClipboardRepository
) {

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    }

    /**
     * 导出所有剪贴板数据为 Markdown
     * @param outputDir 输出目录
     * @param includeTags 是否包含标签信息
     * @param groupByDate 是否按日期分组
     * @return 导出的文件路径
     */
    suspend fun exportToMarkdown(
        outputDir: File,
        includeTags: Boolean = true,
        groupByDate: Boolean = false
    ): File = withContext(Dispatchers.IO) {

        // 确保输出目录存在
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        // 获取所有数据
        val allClipboards = repository.getAllClipboardsSync()
        val tagDefinitions = repository.getAllTagDefinitionsSync()
        val allTags = repository.getAllTagsSync()

        // 生成文件名
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "clipboard_backup_$timestamp.md")

        // 构建 Markdown 内容
        val markdown = buildMarkdownContent(
            allClipboards,
            tagDefinitions,
            allTags,
            includeTags,
            groupByDate
        )

        // 写入文件
        outputFile.writeText(markdown, Charsets.UTF_8)

        outputFile
    }

    /**
     * 构建 Markdown 内容
     */
    private fun buildMarkdownContent(
        clipboards: List<ClipboardEntity>,
        tagDefinitions: List<TagDefinition>,
        tags: List<com.jishi.clipboard.data.TagEntity>,
        includeTags: Boolean,
        groupByDate: Boolean
    ): String {

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val sb = StringBuilder()

        // 标题
        sb.appendLine("# 剪贴板备份")
        sb.appendLine()
        sb.appendLine("**导出时间**: ${dateFormat.format(Date())}")
        sb.appendLine("**记录数量**: ${clipboards.size} 条")
        sb.appendLine("**标签数量**: ${tagDefinitions.size} 个")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        // 按日期分组或按顺序导出
        val groupedClipboards = if (groupByDate) {
            clipboards.groupBy {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.createdAt))
            }
        } else {
            mapOf("全部" to clipboards)
        }

        // 遍历并导出每条记录
        var index = 1
        groupedClipboards.forEach { (group, items) ->

            // 日期分组标题
            if (groupByDate && group != "全部") {
                sb.appendLine()
                sb.appendLine("## 📅 $group (${items.size}条)")
                sb.appendLine()
            }

            items.forEach { clipboard ->
                // 获取此剪贴板的标签
                val clipboardTags = if (includeTags) {
                    tags
                        .filter { it.clipboardId == clipboard.id }
                        .mapNotNull { tagRelation ->
                            tagDefinitions.find { it.id == tagRelation.tagDefinitionId }
                        }
                } else {
                    emptyList()
                }

                // 标题
                val preview = clipboard.content.take(50).let {
                    if (clipboard.content.length > 50) "$it..." else it
                }
                sb.appendLine("### $index. $preview")

                // 元数据
                sb.appendLine()
                sb.appendLine("**创建时间**: ${dateFormat.format(Date(clipboard.createdAt))}")
                sb.appendLine("**更新时间**: ${dateFormat.format(Date(clipboard.updatedAt))}")
                sb.appendLine("**字符数**: ${clipboard.content.length}")

                // 标签
                if (includeTags && clipboardTags.isNotEmpty()) {
                    val tagNames = clipboardTags.joinToString(", ") { "#${it.name}" }
                    sb.appendLine("**标签**: $tagNames")
                }

                sb.appendLine()

                // 内容
                sb.appendLine("<details>")
                sb.appendLine("<summary>点击查看完整内容</summary>")
                sb.appendLine()
                sb.appendLine("```")
                sb.appendLine(clipboard.content)
                sb.appendLine("```")
                sb.appendLine()
                sb.appendLine("</details>")
                sb.appendLine()
                sb.appendLine("---")
                sb.appendLine()

                index++
            }
        }

        // 标签统计
        if (includeTags && tagDefinitions.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("## 🏷️ 标签统计")
            sb.appendLine()

            tagDefinitions
                .sortedByDescending { it.useCount }
                .forEach { tag ->
                    val count = tags.count { it.tagDefinitionId == tag.id }
                    sb.appendLine("- **#${tag.name}**: 使用 ${tag.useCount} 次,关联 $count 条记录")
                }

            sb.appendLine()
        }

        // 文件结尾
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
        sb.appendLine("*本文件由及时记剪贴板管理器自动生成*")

        return sb.toString()
    }

    /**
     * 导出指定日期范围的剪贴板数据
     */
    suspend fun exportDateRange(
        outputDir: File,
        startDate: Long,
        endDate: Long
    ): File = withContext(Dispatchers.IO) {

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val allClipboards = repository.getAllClipboardsSync()
        val filtered = allClipboards.filter {
            it.createdAt in startDate..endDate
        }

        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "clipboard_range_$timestamp.md")

        // 复用导出逻辑
        val tagDefinitions = repository.getAllTagDefinitionsSync()
        val allTags = repository.getAllTagsSync()

        val markdown = buildMarkdownContent(
            filtered,
            tagDefinitions,
            allTags,
            includeTags = true,
            groupByDate = true
        )

        outputFile.writeText(markdown, Charsets.UTF_8)

        outputFile
    }

    /**
     * 导出指定标签的剪贴板数据
     */
    suspend fun exportByTag(
        outputDir: File,
        tagId: Long
    ): File = withContext(Dispatchers.IO) {

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val allClipboards = repository.getAllClipboardsSync()
        val allTags = repository.getAllTagsSync()

        // 筛选指定标签的剪贴板
        val clipboardIds = allTags
            .filter { it.tagDefinitionId == tagId }
            .map { it.clipboardId }
            .distinct()

        val filtered = allClipboards.filter { it.id in clipboardIds }

        val tag = repository.getAllTagDefinitionsSync().find { it.id == tagId }
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "clipboard_tag_${tag?.name ?: "unknown"}_$timestamp.md")

        val tagDefinitions = repository.getAllTagDefinitionsSync()

        val markdown = buildMarkdownContent(
            filtered,
            tagDefinitions,
            allTags,
            includeTags = true,
            groupByDate = false
        )

        outputFile.writeText(markdown, Charsets.UTF_8)

        outputFile
    }
}
