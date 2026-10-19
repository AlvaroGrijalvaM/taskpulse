package com.taskpulse.data

import android.content.Context
import com.taskpulse.notifications.ReminderNotifier
import com.taskpulse.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow

// TaskRepository: único punto de acceso a las tareas para el resto de la app.
// Agrupa dos responsabilidades que siempre deben ir juntas: guardar en Room y
// mantener la alarma del sistema en sincronía con el estado de la tarea.
class TaskRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dao = TaskDatabase.get(appContext).taskDao()
    private val scheduler = ReminderScheduler(appContext)

    val tasks: Flow<List<Task>> = dao.observeTasks()

    suspend fun get(taskId: Long): Task? = dao.getById(taskId)

    // Observación reactiva de una tarea: el detalle la ve actualizarse sola
    // cuando se edita o completa desde otras pantallas.
    fun observe(taskId: Long): Flow<Task?> = dao.observeById(taskId)

    // Inserta la tarea y, si tiene recordatorio activo, programa su alarma.
    // Devuelve el id generado para poder navegar al detalle.
    suspend fun create(task: Task): Long {
        val id = dao.insert(task)
        val saved = task.copy(id = id)
        if (saved.reminderEnabled && !saved.completed) {
            scheduler.schedule(saved)
        }
        return id
    }

    // Al editar, se cancela primero la alarma anterior para no duplicarla y
    // después se vuelve a programar si sigue correspondiendo.
    suspend fun update(task: Task) {
        scheduler.cancel(task.id)
        dao.update(task)
        if (task.reminderEnabled && !task.completed) {
            scheduler.schedule(task)
        }
    }

    suspend fun delete(task: Task) {
        scheduler.cancel(task.id)
        dao.delete(task)
        // La notificación (si sigue en el sistema) apuntaría a una tarea
        // inexistente: se descarta junto con la tarea.
        ReminderNotifier(appContext).cancel(task.id)
    }

    // Completar una tarea cancela su recordatorio pendiente y descarta
    // cualquier notificación suya que aún se esté mostrando.
    suspend fun setCompleted(task: Task, completed: Boolean) {
        scheduler.cancel(task.id)
        dao.update(task.copy(completed = completed))
        if (completed) {
            ReminderNotifier(appContext).cancel(task.id)
        }
    }

    // Tareas con recordatorio activado y aún sin completar. Lo usa Ajustes
    // para reprogramar todo al volver a activar los recordatorios.
    suspend fun getPendingReminders(): List<Task> = dao.getPendingReminders()

    // Programa la alarma de una tarea si le corresponde. Expuesto para que
    // Ajustes pueda reprogramar sin alterar el resto de la tarea.
    fun scheduleReminder(task: Task) {
        if (task.reminderEnabled && !task.completed) {
            scheduler.schedule(task)
        }
    }

    // Cancela las alarmas de todas las tareas pendientes sin tocar la base
    // de datos. Lo usa Ajustes al desactivar el interruptor de recordatorios:
    // las tareas siguen guardadas, pero no avisa hasta reactivarlo.
    suspend fun cancelAllReminderAlarms() {
        dao.getPendingReminders().forEach { scheduler.cancel(it.id) }
    }
}
