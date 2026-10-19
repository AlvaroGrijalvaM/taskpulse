package com.taskpulse.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.taskpulse.data.TaskDatabase
import com.taskpulse.notifications.ReminderNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ReminderReceiver recibe la alarma de Android cuando llega la hora de una
// tarea, esté abierta o no la app. Consulta la tarea en Room y muestra la
// notificación con vibración.
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        private const val TAG = "TaskPulse"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return
        Log.d(TAG, "Alarma recibida del sistema para la tarea $taskId")

        // goAsync deja el receptor vivo mientras corre la corrutina, para
        // poder leer la base de datos antes de que el sistema lo reclame.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = TaskDatabase.get(context).taskDao().getById(taskId)
                // Sólo avisa si la tarea sigue pendiente y con recordatorio,
                // y si el usuario no ha desactivado los recordatorios en
                // Ajustes: esa preferencia apaga el sistema de avisos entero.
                val active = task != null && !task.completed && task.reminderEnabled
                val prefOn = ReminderPreferences.isEnabled(context)
                if (active && prefOn) {
                    ReminderNotifier(context).show(task)
                } else {
                    Log.w(
                        TAG,
                        "Aviso omitido: existe=${task != null}, completada=${task?.completed}," +
                            " recordatorio=${task?.reminderEnabled}, ajustesActivos=$prefOn"
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
