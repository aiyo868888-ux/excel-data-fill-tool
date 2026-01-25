package com.fleetingnotes.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.datetime.Instant

/**
 * 笔记类型枚举
 */
@Serializable
enum class NoteType {
    IDEA,       // 灵感
    INSIGHT,    // 启发
    TODO        // 待办
}

/**
 * 启发来源枚举
 */
@Serializable
enum class InsightSource {
    BOOK,        // 书籍
    PODCAST,     // 播客
    WEB,         // 网络
    CONVERSATION,// 对话
    COURSE,      // 课程
    OTHER        // 其他
}

/**
 * 优先级枚举
 */
@Serializable
enum class Priority {
    HIGH,        // 高
    MEDIUM,      // 中
    LOW          // 低
}

/**
 * 重复频率枚举
 */
@Serializable
enum class RepeatFrequency {
    DAILY,       // 每天
    WEEKLY,      // 每周
    MONTHLY,     // 每月
    CUSTOM       // 自定义
}

/**
 * InsightSource 扩展属性
 */
val InsightSource.label: String
    get() = when (this) {
        InsightSource.BOOK -> "书籍"
        InsightSource.PODCAST -> "播客"
        InsightSource.WEB -> "网络"
        InsightSource.CONVERSATION -> "对话"
        InsightSource.COURSE -> "课程"
        InsightSource.OTHER -> "其他"
    }

val InsightSource.icon: String?
    get() = when (this) {
        InsightSource.BOOK -> "📚"
        InsightSource.PODCAST -> "🎧"
        InsightSource.WEB -> "🌐"
        InsightSource.CONVERSATION -> "💬"
        InsightSource.COURSE -> "🎓"
        InsightSource.OTHER -> "📌"
    }

/**
 * 剪切板来源记录
 */
@Serializable
data class ClipboardSource(
    val text: String,
    val timestamp: Instant,
    val app: String? = null
)

/**
 * 重复规则
 */
@Serializable
data class RepeatRule(
    val frequency: RepeatFrequency,
    val interval: Int = 1
)

/**
 * 笔记基类
 */
@Serializable
sealed class Note {
    abstract val id: String
    abstract val type: NoteType
    abstract val content: String
    abstract val memo: String?
    abstract val clipboardSources: List<ClipboardSource>
    abstract val createdAt: Instant
    abstract val updatedAt: Instant
}

/**
 * 灵感笔记
 */
@Serializable
data class IdeaNote(
    override val id: String = generateId(),
    override val type: NoteType = NoteType.IDEA,
    override val content: String,
    val scene: String? = null,                    // 场景
    override val memo: String? = null,
    override val clipboardSources: List<ClipboardSource> = emptyList(),
    @Serializable(with = InstantSerializer::class)
    override val createdAt: Instant = kotlinx.datetime.Clock.System.now(),
    @Serializable(with = InstantSerializer::class)
    override val updatedAt: Instant = kotlinx.datetime.Clock.System.now()
) : Note()

/**
 * 启发笔记
 */
@Serializable
data class InsightNote(
    override val id: String = generateId(),
    override val type: NoteType = NoteType.INSIGHT,
    val source: InsightSource? = null,            // 来源类型
    val sourceDetail: String? = null,             // 来源详情
    override val content: String,
    val keyInsight: String? = null,               // 关键启发
    val keywords: List<String> = emptyList(),       // 关键词
    override val memo: String? = null,
    override val clipboardSources: List<ClipboardSource> = emptyList(),
    @Serializable(with = InstantSerializer::class)
    override val createdAt: Instant = kotlinx.datetime.Clock.System.now(),
    @Serializable(with = InstantSerializer::class)
    override val updatedAt: Instant = kotlinx.datetime.Clock.System.now()
) : Note()

/**
 * 待办笔记
 */
@Serializable
data class TodoNote(
    override val id: String = generateId(),
    override val type: NoteType = NoteType.TODO,
    override val content: String,
    val dueDate: kotlinx.datetime.LocalDate? = null,  // 截止日期
    val dueTime: kotlinx.datetime.LocalTime? = null,  // 截止时间
    val reminder: Boolean = false,               // 是否提醒
    val priority: Priority = Priority.MEDIUM,    // 优先级
    val repeat: RepeatRule? = null,              // 重复规则
    val completed: Boolean = false,             // 是否完成
    @Serializable(with = InstantSerializer::class)
    val completedAt: Instant? = null,            // 完成时间
    override val memo: String? = null,
    override val clipboardSources: List<ClipboardSource> = emptyList(),
    @Serializable(with = InstantSerializer::class)
    override val createdAt: Instant = kotlinx.datetime.Clock.System.now(),
    @Serializable(with = InstantSerializer::class)
    override val updatedAt: Instant = kotlinx.datetime.Clock.System.now()
) : Note()

/**
 * 每日数据容器
 */
@Serializable
data class DailyNotes(
    val date: String,                             // YYYY-MM-DD
    val version: String = "1.0",
    val statistics: Statistics = Statistics(),
    val data: NoteData = NoteData()
)

/**
 * 统计信息
 */
@Serializable
data class Statistics(
    val ideas: Int = 0,
    val insights: Int = 0,
    val todos: Int = 0
)

/**
 * 分类数据
 */
@Serializable
data class NoteData(
    val ideas: List<IdeaNote> = emptyList(),
    val insights: List<InsightNote> = emptyList(),
    val todos: List<TodoNote> = emptyList()
)

/**
 * Instant 序列化器
 */
@Serializer(Instant::class)
object InstantSerializer : kotlinx.serialization.KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

/**
 * 生成唯一 ID
 */
fun generateId(): String {
    return java.util.UUID.randomUUID().toString()
}
