package com.fleetingnotes.data.local

import android.content.Context
import com.fleetingnotes.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import java.io.File

/**
 * JSON 文件存储
 * 负责每日 JSON 文件的读写
 */
class JsonFileStorage(
    private val context: Context
) {
    private val notesDir: File
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        coerceInputValues = true
    }

    init {
        // 创建笔记目录
        notesDir = File(context.filesDir, "notes")
        if (!notesDir.exists()) {
            notesDir.mkdirs()
        }
    }

    /**
     * 追加笔记到今日文件
     */
    suspend fun appendNote(note: Note) = withContext(Dispatchers.IO) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val file = File(notesDir, "$today.json")

        val dailyNotes = if (file.exists()) {
            readDailyNotes(file)
        } else {
            DailyNotes(date = today.toString())
        }

        val updatedData = when (note) {
            is IdeaNote -> dailyNotes.data.copy(
                ideas = dailyNotes.data.ideas + (note as IdeaNote)
            )
            is InsightNote -> dailyNotes.data.copy(
                insights = dailyNotes.data.insights + (note as InsightNote)
            )
            is TodoNote -> dailyNotes.data.copy(
                todos = dailyNotes.data.todos + (note as TodoNote)
            )
            else -> dailyNotes.data
        }

        val updatedDailyNotes = dailyNotes.copy(
            data = updatedData,
            statistics = Statistics(
                ideas = updatedData.ideas.size,
                insights = updatedData.insights.size,
                todos = updatedData.todos.size
            )
        )

        writeDailyNotes(file, updatedDailyNotes)
    }

    /**
     * 读取指定日期的笔记
     */
    suspend fun getNotesByDate(date: LocalDate): DailyNotes = withContext(Dispatchers.IO) {
        val file = File(notesDir, "$date.json")
        if (file.exists()) {
            readDailyNotes(file)
        } else {
            DailyNotes(date = date.toString())
        }
    }

    /**
     * 获取所有笔记文件列表
     */
    suspend fun getAllDates(): List<LocalDate> = withContext(Dispatchers.IO) {
        notesDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?.mapNotNull {
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    null
                }
            }
            ?.sortedDescending()
            ?: emptyList()
    }

    /**
     * 搜索笔记
     */
    suspend fun searchNotes(query: String): List<Note> = withContext(Dispatchers.IO) {
        val allNotes = mutableListOf<Note>()

        getAllDates().forEach { date ->
            val dailyNotes = getNotesByDate(date)
            allNotes.addAll(dailyNotes.data.ideas)
            allNotes.addAll(dailyNotes.data.insights)
            allNotes.addAll(dailyNotes.data.todos)
        }

        allNotes.filter { note ->
            note.content.contains(query, ignoreCase = true) ||
                    (note.memo?.contains(query, ignoreCase = true) ?: false)
        }
    }

    /**
     * 删除笔记
     */
    suspend fun deleteNote(noteId: String, date: LocalDate): Boolean = withContext(Dispatchers.IO) {
        val file = File(notesDir, "$date.json")
        if (!file.exists()) return@withContext false

        val dailyNotes = readDailyNotes(file)
        val updatedData = when {
            dailyNotes.data.ideas.any { it.id == noteId } -> {
                dailyNotes.data.copy(
                    ideas = dailyNotes.data.ideas.filter { it.id != noteId }
                )
            }
            dailyNotes.data.insights.any { it.id == noteId } -> {
                dailyNotes.data.copy(
                    insights = dailyNotes.data.insights.filter { it.id != noteId }
                )
            }
            dailyNotes.data.todos.any { it.id == noteId } -> {
                dailyNotes.data.copy(
                    todos = dailyNotes.data.todos.filter { it.id != noteId }
                )
            }
            else -> return@withContext false
        }

        val updatedDailyNotes = dailyNotes.copy(
            data = updatedData,
            statistics = Statistics(
                ideas = updatedData.ideas.size,
                insights = updatedData.insights.size,
                todos = updatedData.todos.size
            )
        )

        writeDailyNotes(file, updatedDailyNotes)
        true
    }

    /**
     * 清除所有笔记数据
     */
    suspend fun clearAll(): Boolean = withContext(Dispatchers.IO) {
        try {
            notesDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.forEach { it.delete() }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 读取每日笔记文件
     */
    private fun readDailyNotes(file: File): DailyNotes {
        val jsonContent = file.readText()
        return json.decodeFromString<DailyNotes>(jsonContent)
    }

    /**
     * 写入每日笔记文件
     */
    private fun writeDailyNotes(file: File, dailyNotes: DailyNotes) {
        val jsonContent = json.encodeToString(DailyNotes.serializer(), dailyNotes)
        file.writeText(jsonContent)
    }
}
