package com.taskpulse.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taskpulse.data.Task
import com.taskpulse.data.TaskRepository
import com.taskpulse.reminders.ReminderPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estado que ve la pantalla de lista, ya calculado a partir de Room.
data class TaskListUiState(
    val pending: List<Task> = emptyList(),
    val completed: List<Task> = emptyList(),
    val loading: Boolean = true
)

// ViewModel de la lista de tareas. Lee Room y expone un Flow que la interfaz
// observa: cada alta, edición o borrado se refleja solo en pantalla.
class TaskListViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(app)

    init {
        // Red de seguridad: al mostrarse la lista se reprograman todas las
        // alarmas pendientes. Es idempotente (mismo PendingIntent reemplaza),
        // no duplica avisos y recupera cualquier alarma perdida por una
        // cancelación, un force-stop o un guardado anterior defectuoso.
        viewModelScope.launch {
            if (ReminderPreferences.isEnabled(app)) {
                repository.getPendingReminders().forEach { repository.scheduleReminder(it) }
            }
        }
    }

    val uiState = repository.tasks
        .map { tasks ->
            TaskListUiState(
                pending = tasks.filter { !it.completed },
                completed = tasks.filter { it.completed },
                loading = false
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TaskListUiState()
        )

    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            repository.setCompleted(task, !task.completed)
        }
    }

}
