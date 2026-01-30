package com.jishi.clipboard.utils

/**
 * 标签解析工具
 * 从内容中提取或移除 #标签 格式的文本
 */
object TagParser {

    // 预编译正则，匹配 #标签 格式：# 开头，后跟中英文数字下划线
    private val TAG_PATTERN = Regex(
        """#(?=[\u4e00-\u9fa5a-zA-Z_])[\u4e00-\u9fa5a-zA-Z0-9_]+"""
    )

    /**
     * 从内容中提取标签名称（不含 #）
     * @param content 原始内容
     * @return 标签名称列表
     */
    fun extractTags(content: String): List<String> {
        return TAG_PATTERN.findAll(content)
            .map { it.value.substring(1) }  // 移除 # 前缀
            .distinct()
            .toList()
    }

    /**
     * 从内容中移除标签文本
     * @param content 原始内容
     * @return 移除标签后的内容
     */
    fun removeTags(content: String): String {
        return TAG_PATTERN.replace(content, "")
            .replace(Regex(" +\n"), "\n")    // 移除行尾多余空格
            .replace(Regex("\\s+"), " ")     // 处理多余空格
            .trim()
    }

    /**
     * 同时获取不含标签的内容和标签列表
     * @param content 原始内容
     * @return 解析结果
     */
    fun parse(content: String): ParsedContent {
        return ParsedContent(
            contentWithoutTags = removeTags(content),
            tags = extractTags(content)
        )
    }

    /**
     * 解析结果数据类
     */
    data class ParsedContent(
        val contentWithoutTags: String,
        val tags: List<String>
    )
}
