package com.jishi.clipboard.utils

import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.util.ImageUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Markdown 导出工具
 * 将笔记数据转换为 Markdown 格式
 */
object MarkdownExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * 导出所有笔记为 Markdown 格式
     * @param inspirations 灵感列表
     * @param insights 启发列表
     * @param todos 待办列表
     * @param tagsMap 标签映射 (entityId -> tags)
     * @param imageBaseDir 图片文件的基础目录(用于相对路径)
     */
    fun exportToMarkdown(
        inspirations: List<ClipboardEntity>,
        insights: List<ClipboardEntity>,
        todos: List<ClipboardEntity>,
        tagsMap: Map<Long, List<String>> = emptyMap(),
        imageBaseDir: String? = null
    ): String {
        val sb = StringBuilder()

        // 标题
        sb.appendLine("# 及时记 - 笔记导出")
        sb.appendLine()
        sb.appendLine("**导出时间**: ${dateFormat.format(Date())}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        // 灵感
        if (inspirations.isNotEmpty()) {
            sb.appendLine("## 📝 灵感")
            sb.appendLine()
            inspirations.forEach { entity ->
                sb.appendLine(exportEntity(entity, "灵感", tagsMap[entity.id], imageBaseDir))
                sb.appendLine()
            }
        }

        // 启发
        if (insights.isNotEmpty()) {
            sb.appendLine("## 💡 启发")
            sb.appendLine()
            insights.forEach { entity ->
                sb.appendLine(exportEntity(entity, "启发", tagsMap[entity.id], imageBaseDir))
                sb.appendLine()
            }
        }

        // 待办
        if (todos.isNotEmpty()) {
            sb.appendLine("## ✅ 待办")
            sb.appendLine()
            todos.forEach { entity ->
                sb.appendLine(exportEntity(entity, "待办", tagsMap[entity.id], imageBaseDir))
                sb.appendLine()
            }
        }

        return sb.toString()
    }

    /**
     * 导出单个笔记
     * @param entity 笔记实体
     * @param type 类型标签
     * @param tags 标签列表
     * @param imageBaseDir 图片基础目录(用于计算相对路径)
     */
    private fun exportEntity(
        entity: ClipboardEntity,
        type: String,
        tags: List<String>?,
        imageBaseDir: String?
    ): String {
        val sb = StringBuilder()

        // 时间戳和类型
        val time = dateFormat.format(Date(entity.createdAt))
        sb.appendLine("### 📅 $time")

        // 显示标签
        if (!tags.isNullOrEmpty()) {
            val tagStr = tags.joinToString(" ") { "#${it.trim()}" }
            sb.appendLine()
            sb.appendLine("**标签**: $tagStr")
        }

        sb.appendLine()

        // 处理内容中的图片
        val contentWithImages = processImagesInContent(entity.content, entity.images ?: "", imageBaseDir)
        sb.appendLine(contentWithImages)

        // 分隔线
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        return sb.toString()
    }

    /**
     * 处理内容中的图片
     * @param content 原始内容
     * @param imagesJson 图片JSON数据
     * @param exportImagesDir 导出图片的目标目录
     */
    private fun processImagesInContent(content: String, imagesJson: String, exportImagesDir: String?): String {
        val sb = StringBuilder()
        val images = ImageUtils.parseImagesFromJson(imagesJson)

        if (images.isEmpty()) {
            // 没有图片,直接返回内容
            return content
        }

        // 添加内容文本
        sb.appendLine(content)
        sb.appendLine()

        // 添加图片
        sb.appendLine("**图片**:")
        sb.appendLine()

        images.forEachIndexed { index, imagePath ->
            val sourceFile = File(imagePath)

            if (sourceFile.exists()) {
                try {
                    if (exportImagesDir != null) {
                        // 复制图片到导出目录
                        val timestamp = System.currentTimeMillis()
                        val targetFile = File(exportImagesDir, "image_${timestamp}_$index.jpg")

                        // 使用流复制文件
                        FileInputStream(sourceFile).use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }

                        // 使用相对路径引用图片
                        sb.appendLine("![图片${index + 1}](images/${targetFile.name})")
                    } else {
                        // 没有指定导出目录,使用原始路径
                        sb.appendLine("![图片${index + 1}]($imagePath)")
                    }
                } catch (e: Exception) {
                    // 复制失败,显示占位符
                    sb.appendLine("*图片${index + 1}: 无法复制图片 (${e.message})*")
                }
            } else {
                // 图片不存在,显示占位符
                sb.appendLine("*图片${index + 1}: 文件不存在 ($imagePath)*")
            }

            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * 导出指定类型的笔记
     * @param entities 笔记列表
     * @param type 类型名称
     * @param tagsMap 标签映射
     * @param imageBaseDir 图片基础目录
     */
    fun exportByType(
        entities: List<ClipboardEntity>,
        type: String,
        tagsMap: Map<Long, List<String>> = emptyMap(),
        imageBaseDir: String? = null
    ): String {
        val sb = StringBuilder()

        sb.appendLine("# 及时记 - ${type}导出")
        sb.appendLine()
        sb.appendLine("**导出时间**: ${dateFormat.format(Date())}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        entities.forEach { entity ->
            sb.appendLine(exportEntity(entity, type, tagsMap[entity.id], imageBaseDir))
            sb.appendLine()
        }

        return sb.toString()
    }
}
