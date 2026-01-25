package com.fleetingnotes.domain.repository

import com.fleetingnotes.data.model.Note
import kotlinx.datetime.LocalDate

/**
 * 笔记仓储接口
 */
interface NoteRepository {
    /**
     * 保存笔记
     */
    suspend fun saveNote(note: Note)

    /**
     * 获取指定日期的笔记
     */
    suspend fun getNotesByDate(date: LocalDate): List<Note>

    /**
     * 搜索笔记
     */
    suspend fun searchNotes(query: String): List<Note>

    /**
     * 删除笔记
     */
    suspend fun deleteNote(noteId: String, date: LocalDate): Boolean

    /**
     * 获取所有有笔记的日期
     */
    suspend fun getAllDates(): List<LocalDate>

    /**
     * 清除所有笔记数据
     */
    suspend fun clearAll(): Boolean
}
