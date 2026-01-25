package com.fleetingnotes.data.repository

import com.fleetingnotes.data.local.JsonFileStorage
import com.fleetingnotes.data.model.DailyNotes
import com.fleetingnotes.data.model.Note
import com.fleetingnotes.domain.repository.NoteRepository
import kotlinx.datetime.LocalDate

/**
 * 笔记仓储实现
 */
class NoteRepositoryImpl(
    private val jsonFileStorage: JsonFileStorage
) : NoteRepository {

    override suspend fun saveNote(note: Note) {
        jsonFileStorage.appendNote(note)
    }

    override suspend fun getNotesByDate(date: LocalDate): List<Note> {
        val dailyNotes = jsonFileStorage.getNotesByDate(date)
        return listOf(
            dailyNotes.data.ideas,
            dailyNotes.data.insights,
            dailyNotes.data.todos
        ).flatten()
    }

    override suspend fun searchNotes(query: String): List<Note> {
        return jsonFileStorage.searchNotes(query)
    }

    override suspend fun deleteNote(noteId: String, date: LocalDate): Boolean {
        return jsonFileStorage.deleteNote(noteId, date)
    }

    override suspend fun getAllDates(): List<LocalDate> {
        return jsonFileStorage.getAllDates()
    }

    override suspend fun clearAll(): Boolean {
        return jsonFileStorage.clearAll()
    }
}
