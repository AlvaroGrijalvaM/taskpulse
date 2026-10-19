package com.taskpulse.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taskpulse.data.Task
import com.taskpulse.data.TaskRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel del detalle de una tarea: la observa en vivo desde Room y gestiona
// sus acciones (completar, editar recordatorio, eliminar). Al observar el flow,
// la pantalla se actualiza sola al volver del editor con cambios.
class TaskDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(app)

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    // true cuando la primera lectura confirma que la tarea no existe
    // (p. ej. una notificación de una tarea ya borrada). La pantalla de
    // detalle usa esto para volver a la lista en lugar de dejar un
    // callejón sin salida.
    private val _notFound = MutableStateFlow(false)
    val notFound: StateFlow<Boolean> = _notFound.asStateFlow()

    private var observingId: Long? = null
    private var observeJob: Job? = null

    // Distingue un "no existe" real del null final que emite la observación
    // al borrar la tarea desde el propio detalle (esa navegación ya la
    // maneja el usuario con el popBackStack de delete).
    private var userDeleted = false

    fun load(taskId: Long) {
        if (observingId == taskId) return
        observingId = taskId
        // Si se cambia de tarea se cancela la observación anterior.
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observe(taskId).collect { task ->
                _task.value = task
                if (task == null && !userDeleted) {
                    _notFound.value = true
                }
            }
        }
    }

    fun toggleCompleted() {
        val current = _task.value ?: return
        // No hace falta refrescar a mano: el flow notifica el cambio.
        viewModelScope.launch { repository.setCompleted(current, !current.completed) }
    }

    // Borra la tarea y, sólo cuando la persistencia termina, navega fuera
    // mediante onDeleted (evita cancelar la operación a medias). NonCancellable
    // protege la escritura si el usuario ya deslizó para volver.
    fun delete(onDeleted: () -> Unit) {
        val current = _task.value ?: return
        userDeleted = true
        viewModelScope.launch {
            withContext(NonCancellable) { repository.delete(current) }
            onDeleted()
        }
    }
}
