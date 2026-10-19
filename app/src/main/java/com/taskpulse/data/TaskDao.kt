package com.taskpulse.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DAO (Data Access Object): reúne todas las consultas contra la tabla tasks.
// Room genera la implementación de cada función a partir de la anotación @Query.
// Las funciones devuelven Flow, es decir, un flujo que la interfaz observa:
// cada vez que cambia la base de datos, Room emite el listado nuevo solo.
@Dao
interface TaskDao {

    // Tareas ordenadas por fecha: primero las pendientes por su fecha, y al
    // final las completadas. La condición completed = 0 + dueAt pone lo
    // pendiente por delante de lo terminado.
    @Query("SELECT * FROM tasks ORDER BY completed ASC, dueAt ASC")
    fun observeTasks(): Flow<List<Task>>

    // Busca una tarea por su id para abrir su detalle o editarla.
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: Long): Task?

    // Observa una tarea por su id: emite la fila actual cada vez que cambia.
    // Lo usa la pantalla de detalle para refrescarse al volver del editor.
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeById(taskId: Long): Flow<Task?>

    // Tareas con recordatorio activado y sin completar. BootReceiver usa esta
    // consulta para volver a programar las alarmas tras reiniciar el reloj.
    @Query("SELECT * FROM tasks WHERE reminderEnabled = 1 AND completed = 0")
    suspend fun getPendingReminders(): List<Task>

    // @Insert devuelve el id generado, que hace falta para programar la alarma.
    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
