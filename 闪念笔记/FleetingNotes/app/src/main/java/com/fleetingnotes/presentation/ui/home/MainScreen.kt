package com.fleetingnotes.presentation.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleetingnotes.R
import com.fleetingnotes.data.model.NoteType
import com.fleetingnotes.data.model.IdeaNote
import com.fleetingnotes.data.model.InsightNote
import com.fleetingnotes.data.model.TodoNote
import com.fleetingnotes.data.model.InsightSource
import com.fleetingnotes.data.model.Priority
import com.fleetingnotes.presentation.ui.dialog.IdeaDialog
import com.fleetingnotes.presentation.ui.dialog.InsightDialog
import com.fleetingnotes.presentation.ui.dialog.TodoDialog
import com.fleetingnotes.presentation.viewmodel.MainViewModel
import com.fleetingnotes.presentation.ui.settings.SettingsPage
import com.fleetingnotes.ServiceLocator
import kotlinx.datetime.Clock
import java.util.UUID

/**
 * 主页面 - 包含三个标签页和底部导航
 */
sealed class Screen(val route: String, val title: String, val iconId: Int) {
    object Ideas : Screen("ideas", "灵感", R.drawable.ic_idea)
    object Insights : Screen("insights", "启发", R.drawable.ic_insight)
    object Todos : Screen("todos", "待办", R.drawable.ic_todo)
    object Settings : Screen("settings", "设置", R.drawable.ic_settings)
}

@Composable
fun MainScreen(
    pendingDialogType: NoteType? = null,
    onDialogShown: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModelFactory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel.create(
                repository = ServiceLocator.noteRepository
            ) as T
        }
    }
    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)

    val navController = rememberNavController()
    val screens = listOf(Screen.Ideas, Screen.Insights, Screen.Todos, Screen.Settings)

    var showIdeaDialog by remember { mutableStateOf(false) }
    var showInsightDialog by remember { mutableStateOf(false) }
    var showTodoDialog by remember { mutableStateOf(false) }

    // 处理悬浮窗传入的对话框类型
    LaunchedEffect(pendingDialogType) {
        when (pendingDialogType) {
            NoteType.IDEA -> {
                showIdeaDialog = true
                onDialogShown()
            }
            NoteType.INSIGHT -> {
                showInsightDialog = true
                onDialogShown()
            }
            NoteType.TODO -> {
                showTodoDialog = true
                onDialogShown()
            }
            null -> {}
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = screen.iconId),
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Ideas.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Ideas.route) {
                SimpleListPage(NoteType.IDEA, "💡 灵感")
            }
            composable(Screen.Insights.route) {
                SimpleListPage(NoteType.INSIGHT, "📖 启发")
            }
            composable(Screen.Todos.route) {
                SimpleListPage(NoteType.TODO, "⚡ 待办")
            }
            composable(Screen.Settings.route) {
                SettingsPage()
            }
        }
    }

    // 对话框
    if (showIdeaDialog) {
        IdeaDialog(
            onDismiss = { showIdeaDialog = false },
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
                showIdeaDialog = false
            }
        )
    }

    if (showInsightDialog) {
        InsightDialog(
            onDismiss = { showInsightDialog = false },
            onSave = { dialogState ->
                val now = Clock.System.now()
                val note = InsightNote(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    updatedAt = now,
                    source = dialogState.source,
                    sourceDetail = dialogState.sourceDetail.takeIf { it.isNotBlank() },
                    keyInsight = dialogState.keyInsight,
                    content = dialogState.content,
                    memo = dialogState.memo.takeIf { it.isNotBlank() },
                    keywords = dialogState.keywords.takeIf { it.isNotBlank() }?.split(" ")?.filter { it.isNotEmpty() } ?: emptyList()
                )
                viewModel.saveNote(note)
                showInsightDialog = false
            }
        )
    }

    if (showTodoDialog) {
        TodoDialog(
            onDismiss = { showTodoDialog = false },
            onSave = { dialogState ->
                val now = Clock.System.now()
                val note = TodoNote(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    updatedAt = now,
                    content = dialogState.content,
                    priority = dialogState.priority,
                    dueDate = dialogState.dueDate,
                    dueTime = dialogState.dueTime,
                    memo = dialogState.memo.takeIf { it.isNotBlank() }
                )
                viewModel.saveNote(note)
                showTodoDialog = false
            }
        )
    }
}
