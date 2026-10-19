package com.taskpulse.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taskpulse.data.Task
import com.taskpulse.data.TaskRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Estado del formulario de nueva/editar tarea.
data class TaskEditorUiState(
    val title: String = "",
    val description: String = "",
    val dueAt: Long = 0L,
    val reminderEnabled: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null
)

// ViewModel del formulario. Valida el título y, al guardar, delega en el
// repositorio para persistir y programar la alarma.
class TaskEditorViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(app)

    private val _state = MutableStateFlow(TaskEditorUiState(dueAt = defaultDueAt()))
    val state: StateFlow<TaskEditorUiState> = _state.asStateFlow()

    private var editingTaskId: Long? = null

    // Marca que el usuario ya tocó el formulario. Si load() termina después,
    // no debe pisar los cambios con los datos antiguos de la base de datos.
    private var dirty = false

    fun onTitleChange(value: String) {
        dirty = true
        _state.value = _state.value.copy(title = value, error = null)
    }


    fun onDueAtChange(value: Long) {
        dirty = true
        _state.value = _state.value.copy(dueAt = value)
    }

    fun onReminderToggle(enabled: Boolean) {
        dirty = true
        _state.value = _state.value.copy(reminderEnabled = enabled)
    }

    fun load(taskId: Long) {
        if (editingTaskId != null) return
        editingTaskId = taskId
        viewModelScope.launch {
            val task = repository.get(taskId)
            if (task != null && !dirty) {
                _state.value = TaskEditorUiState(
                    title = task.title,
                    description = task.description,
                    dueAt = task.dueAt,
                    reminderEnabled = task.reminderEnabled,
                )
            }
        }
    }

    // Guarda la tarea y, sólo cuando la persistencia y la programación de la
    // alarma terminan bien, navega fuera con onSaved. Si la validación falla,
    // se permanece en el formulario para que el usuario vea el error.
    fun save(onSaved: () -> Unit) {
        val current = _state.value
        // No se permite guardar sin título ni sin fecha; se informa en pantalla.
        if (current.title.isBlank()) {
            _state.value = current.copy(error = "Escribe un título para la tarea")
            return
        }
        if (current.dueAt == 0L) {
            _state.value = current.copy(error = "Elige fecha y hora")
            return
        }

        _state.value = current.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                val existingId = editingTaskId
                // NonCancellable: aunque el usuario deslice para volver nada más
                // pulsar Guardar (y la navegación cancele este viewModelScope),
                // la escritura en Room y la programación de la alarma terminan.
                withContext(NonCancellable) {
                    if (existingId == null) {
                        repository.create(
                            Task(
                                title = current.title.trim(),
                                description = current.description.trim(),
                                dueAt = current.dueAt,
                                reminderEnabled = current.reminderEnabled
                            )
                        )
                    } else {
                        repository.get(existingId)?.let { existing ->
                            repository.update(
                                existing.copy(
                                    title = current.title.trim(),
                                    description = current.description.trim(),
                                    dueAt = current.dueAt,
                                    reminderEnabled = current.reminderEnabled
                                )
                            )
                        }
                    }
                }
                _state.value = _state.value.copy(saving = false)
                // Se navega después de guardar: hacerlo antes cancelaba esta
                // corrutina (viewModelScope) y perdía el guardado o la alarma.
                onSaved()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = "No se pudo guardar la tarea"
                )
            }
        }
    }

    companion object {
        // Hora por defecto al crear una tarea: la actual + 1 h, para que lo
        // que muestra la pantalla sea siempre lo que se guarda si el usuario
        // no llega a tocar el chip de fecha.
        private fun defaultDueAt(): Long = System.currentTimeMillis() + 60 * 60 * 1000L
    }
}
