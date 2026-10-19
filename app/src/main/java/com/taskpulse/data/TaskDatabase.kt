package com.taskpulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Base de datos de TaskPulse. Room genera por debajo un archivo SQLite dentro
// del reloj, de modo que las tareas sobreviven al cerrar o reiniciar la app.
// @Database declara qué entidades (tablas) forman parte de la base de datos.
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        // Instancia única: toda la app (y los receivers) usan la misma base de
        // datos. @Volatile garantiza que los cambios se vean entre hilos.
        @Volatile
        private var instance: TaskDatabase? = null

        fun get(context: Context): TaskDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): TaskDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                TaskDatabase::class.java,
                "taskpulse.db"
            ).build()
    }
}
