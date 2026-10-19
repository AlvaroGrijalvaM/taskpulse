package com.taskpulse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tarea: una actividad con título, fecha/hora y estado de recordatorio.
// Room convierte esta clase en una tabla de SQLite llamada "tasks".
// Los comentarios con @ son anotaciones que lee Room para generar el
// esquema de la base de datos y el código de acceso a las filas.
@Entity(tableName = "tasks")
data class Task(
    // Clave primaria autogenerada: identifica a cada tarea de forma única.
    // Room la usa después para programar o cancelar su alarma.
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    // Fecha y hora programadas, en milisegundos desde 1970 (epoch).
    // Guardar un instante numérico facilita ordenar y comparar tareas.
    val dueAt: Long,
    val completed: Boolean = false,
    // Indica si debe programarse una alarma para esta tarea.
    val reminderEnabled: Boolean = true
)

