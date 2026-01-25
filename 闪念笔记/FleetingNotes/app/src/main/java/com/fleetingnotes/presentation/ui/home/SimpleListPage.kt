package com.fleetingnotes.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleetingnotes.data.model.Note
import com.fleetingnotes.data.model.NoteType
import com.fleetingnotes.data.model.Priority
import com.fleetingnotes.presentation.viewmodel.MainViewModel
import com.fleetingnotes.presentation.ui.dialog.IdeaDialog
import com.fleetingnotes.presentation.ui.dialog.IdeaDialogState
import com.fleetingnotes.data.model.IdeaNote
import kotlinx.datetime.Clock
import timber.log.Timber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import java.util.UUID

/**
 * 简化版列表页面 - 用于快速编译
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleListPage(noteType: NoteType, title: String) {
    val viewModelFactory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel.create(
                repository = com.fleetingnotes.ServiceLocator.noteRepository
            ) as T
        }
    }

    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
    val notes = when (noteType) {
        NoteType.IDEA -> viewModel.ideaNotes.collectAsState().value
        NoteType.INSIGHT -> viewModel.insightNotes.collectAsState().value
        NoteType.TODO -> viewModel.todoNotes.collectAsState().value
    }

    LaunchedEffect(Unit) {
        viewModel.loadNotes(noteType)
    }

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (noteType == NoteType.IDEA) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无数据")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes.size) { index ->
                    NoteCard(notes[index], onDelete = { noteId ->
                        viewModel.deleteNote(noteId)
                    })
                }
            }
        }
    }

    // 显示灵感对话框
    if (showDialog && noteType == NoteType.IDEA) {
        IdeaDialog(
            onDismiss = { showDialog = false },
            onSave = { dialogState ->
                val now = Clock.System.now()
                val note = IdeaNote(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    updatedAt = now,
                    content = dialogState.content,
                    scene = dialogState.scene.takeIf { it.isNotBlank() },
                    memo = dialogState.memo.takeIf { it.isNotBlank() },
                    clipboardSources = emptyList()
                )
                viewModel.saveNote(note)
                showDialog = false
            }
        )
    }
}

@Composable
fun NoteCard(note: Note, onDelete: (String) -> Unit = {}) {
    // 根据笔记类型和时间生成随机但固定的颜色变体
    val variant = remember(note.id) {
        ((note.id.hashCode() % 3) + 3) % 3 + 1
    }

    val (backgroundColor, contentColor, badgeColor) = when (note.type) {
        NoteType.IDEA -> when (variant) {
            1 -> Triple(
                Color(0xFFFFFBEF),
                Color(0xFFF59E0B),
                Color(0xFFFDE68A)
            )
            2 -> Triple(
                Color(0xFFFFF9C3),
                Color(0xFFF59E0B),
                Color(0xFFFACC15)
            )
            else -> Triple(
                Color(0xFFFFEDD5),
                Color(0xF59E0B),
                Color(0xFFFDBA74)
            )
        }
        NoteType.INSIGHT -> when (variant) {
            1 -> Triple(
                Color(0xFFEFF6FF),
                Color(0xFF1E40AF),
                Color(0xFFBFDBFE)
            )
            2 -> Triple(
                Color(0xFFE0E7FF),
                Color(0xFF1E40AF),
                Color(0xFFA5B4FC)
            )
            else -> Triple(
                Color(0xFFF3E8FF),
                Color(0xFF1E40AF),
                Color(0xFFD8B4FE)
            )
        }
        NoteType.TODO -> when (variant) {
            1 -> Triple(
                Color(0xFFFEF2F2),
                Color(0xFF991B1B),
                Color(0xFFFECACA)
            )
            2 -> Triple(
                Color(0xFFFCE7F3),
                Color(0xFF991B1B),
                Color(0xFFF9A8D4)
            )
            else -> Triple(
                Color(0xFFFFE4E6),
                Color(0xFF991B1B),
                Color(0xFFFDA4AF)
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, badgeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // 类型图标徽章
                    val typeIcon = when (note.type) {
                        NoteType.IDEA -> "💡"
                        NoteType.INSIGHT -> "📖"
                        NoteType.TODO -> "⚡"
                    }

                    // 显示场景/来源
                    when (note) {
                        is com.fleetingnotes.data.model.IdeaNote -> {
                            note.scene?.let { scene ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "🎬 $scene",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = contentColor,
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                                    )
                                }
                            }
                        }
                        is com.fleetingnotes.data.model.InsightNote -> {
                            note.source?.let { source ->
                                val sourceIcon = when (source) {
                                    com.fleetingnotes.data.model.InsightSource.BOOK -> "📚"
                                    com.fleetingnotes.data.model.InsightSource.PODCAST -> "🎙️"
                                    com.fleetingnotes.data.model.InsightSource.WEB -> "🌐"
                                    com.fleetingnotes.data.model.InsightSource.CONVERSATION -> "💬"
                                    com.fleetingnotes.data.model.InsightSource.COURSE -> "👨‍🏫"
                                    com.fleetingnotes.data.model.InsightSource.OTHER -> "📝"
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "$sourceIcon ${source.name}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = contentColor,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        is com.fleetingnotes.data.model.TodoNote -> {
                            note.priority?.let { priority ->
                                val priorityText = when (priority) {
                                    Priority.HIGH -> "🔴 高"
                                    Priority.MEDIUM -> "🟡 中"
                                    Priority.LOW -> "🟢 低"
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = priorityText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = contentColor,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )

                    note.memo?.let { memo ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = memo,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }

                    // 时间显示
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = com.fleetingnotes.utils.DateTimeUtils.formatInstant(
                            note.createdAt,
                            "HH:mm"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = { onDelete(note.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
