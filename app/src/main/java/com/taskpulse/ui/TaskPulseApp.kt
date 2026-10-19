package com.taskpulse.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.taskpulse.ui.screens.SettingsScreen
import com.taskpulse.ui.screens.TaskDetailScreen
import com.taskpulse.ui.screens.TaskEditorScreen
import com.taskpulse.ui.screens.TaskListScreen

// Rutas de la app. "detail" y "edit" llevan el id de la tarea.
private object Routes {
    const val LIST = "list"
    const val NEW = "new"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{taskId}"
    const val EDIT = "edit/{taskId}"
    fun detail(taskId: Long) = "detail/$taskId"
    fun edit(taskId: Long) = "edit/$taskId"
}

// Raíz de la navegación en reloj. SwipeDismissableNavHost es el patrón
// nativo de Wear OS: se vuelve hacia atrás deslizando de derecha a izquierda.
@Composable
fun TaskPulseApp(openedTaskId: Long? = null) {
    val navController = rememberSwipeDismissableNavController()
    // La lista es SIEMPRE la raíz de la pila: aunque la app se abra desde una
    // notificación, el detalle se apila sobre ella. Así, volver desde el
    // detalle jamás deja el NavHost con la pila vacía (pantalla negra).
    // Si llega un intent con tarea (onCreate u onNewIntent: "Open on watch")
    // se navega a su detalle; popUpTo(LIST) evita apilar varios detalles si
    // llegan varias notificaciones.
    var lastOpenedTaskId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(openedTaskId) {
        val id = openedTaskId
        if (id != null && id != lastOpenedTaskId) {
            lastOpenedTaskId = id
            navController.navigate(Routes.detail(id)) {
                popUpTo(Routes.LIST) { saveState = true }
                launchSingleTop = true
            }
        }
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {

        composable(Routes.LIST) {
            val vm: TaskListViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            TaskListScreen(
                state = state,
                onNewTask = { navController.navigate(Routes.NEW) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenTask = { navController.navigate(Routes.detail(it)) },
                onToggleCompleted = vm::toggleCompleted
            )
        }

        composable(Routes.NEW) {
            val vm: TaskEditorViewModel = viewModel()
            val state by vm.state.collectAsState()
            TaskEditorScreen(
                state = state,
                onTitleChange = vm::onTitleChange,
                onDueAtChange = vm::onDueAtChange,
                onReminderToggle = vm::onReminderToggle,
                onSave = { vm.save { navController.popBackStack() } },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { entry ->
            val taskId = entry.arguments?.getLong("taskId") ?: return@composable
            val vm: TaskDetailViewModel = viewModel()
            val task by vm.task.collectAsState()
            val notFound by vm.notFound.collectAsState()
            vm.load(taskId)

            // Si la tarea no existe (notificación de una tarea ya borrada),
            // se vuelve a la lista: nada que completar ni editar aquí.
            LaunchedEffect(notFound) {
                if (notFound) {
                    val back = navController.popBackStack(Routes.LIST, inclusive = false)
                    if (!back) {
                        navController.navigate(Routes.LIST) { launchSingleTop = true }
                    }
                }
            }

            TaskDetailScreen(
                task = task,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.edit(it)) },
                onToggleCompleted = vm::toggleCompleted,
                onDelete = { vm.delete { navController.popBackStack() } }
            )
        }

        composable(
            Routes.EDIT,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { entry ->
            val taskId = entry.arguments?.getLong("taskId") ?: return@composable
            val vm: TaskEditorViewModel = viewModel()
            val state by vm.state.collectAsState()
            vm.load(taskId)
            TaskEditorScreen(
                state = state,
                isEditing = true,
                onTitleChange = vm::onTitleChange,
                onDueAtChange = vm::onDueAtChange,
                onReminderToggle = vm::onReminderToggle,
                onSave = { vm.save { navController.popBackStack() } },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel()
            val enabled by vm.remindersEnabled.collectAsState()
            SettingsScreen(
                remindersEnabled = enabled,
                onToggleReminders = vm::setRemindersEnabled,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
