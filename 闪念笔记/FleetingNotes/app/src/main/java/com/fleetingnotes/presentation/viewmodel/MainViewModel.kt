package com.fleetingnotes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleetingnotes.data.model.Note
import com.fleetingnotes.data.model.NoteType
import com.fleetingnotes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 主页面 ViewModel - 管理列表页面的状态
 */
class MainViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 灵感笔记列表
    private val _ideaNotes = MutableStateFlow<List<Note>>(emptyList())
    val ideaNotes: StateFlow<List<Note>> = _ideaNotes.asStateFlow()

    // 启发笔记列表
    private val _insightNotes = MutableStateFlow<List<Note>>(emptyList())
    val insightNotes: StateFlow<List<Note>> = _insightNotes.asStateFlow()

    // 待办笔记列表
    private val _todoNotes = MutableStateFlow<List<Note>>(emptyList())
    val todoNotes: StateFlow<List<Note>> = _todoNotes.asStateFlow()

    init {
        Timber.d("MainViewModel initialized")
    }

    /**
     * 加载指定类型的笔记
     */
    fun loadNotes(type: NoteType) {
        viewModelScope.launch {
            try {
                Timber.d("Loading notes for type: $type")
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val todayNotes = repository.getNotesByDate(today)
                val filteredNotes = when (type) {
                    NoteType.IDEA -> todayNotes.filter { it.type == NoteType.IDEA }
                    NoteType.INSIGHT -> todayNotes.filter { it.type == NoteType.INSIGHT }
                    NoteType.TODO -> todayNotes.filter { it.type == NoteType.TODO }
                }

                when (type) {
                    NoteType.IDEA -> _ideaNotes.value = filteredNotes
                    NoteType.INSIGHT -> _insightNotes.value = filteredNotes
                    NoteType.TODO -> _todoNotes.value = filteredNotes
                }

                Timber.d("Loaded ${filteredNotes.size} notes for type: $type")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load notes for type: $type")
            }
        }
    }

    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        Timber.d("Search query updated: $query")
        // TODO: 实现搜索过滤
    }

    /**
     * 删除笔记
     */
    fun deleteNote(noteId: String, date: kotlinx.datetime.LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) {
        viewModelScope.launch {
            try {
                val success = repository.deleteNote(noteId, date)
                if (success) {
                    Timber.d("Note deleted: $noteId")
                    // 重新加载所有列表
                    loadNotes(NoteType.IDEA)
                    loadNotes(NoteType.INSIGHT)
                    loadNotes(NoteType.TODO)
                } else {
                    Timber.w("Failed to delete note: $noteId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error deleting note: $noteId")
            }
        }
    }

    /**
     * 保存笔记
     */
    fun saveNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.saveNote(note)
                Timber.d("Note saved: ${note.id}")

                // 重新加载对应类型的列表
                loadNotes(note.type)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save note: ${note.id}")
            }
        }
    }

    companion object {
        /**
         * Factory for creating MainViewModel instances
         */
        fun create(repository: NoteRepository) = MainViewModel(repository)
    }
}
