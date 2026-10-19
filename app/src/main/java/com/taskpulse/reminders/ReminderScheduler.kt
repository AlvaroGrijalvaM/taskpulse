package com.taskpulse.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.taskpulse.data.Task

// ReminderScheduler programa y cancela las alarmas del sistema para cada tarea.
// Android garantiza que, aunque la app esté cerrada, dispara el PendingIntent
// a la hora indicada. Un id de tarea se traduce siempre en el mismo PendingIntent,
// así que programar dos veces no duplica: reemplaza la alarma anterior.
class ReminderScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(task: Task) {
        // Si la hora ya pasó, no se programa nada: la tarea se mostrará como
        // vencida en la lista, sin desaparecer de forma silenciosa.
        if (task.dueAt <= System.currentTimeMillis()) return

        val pending = pendingIntentFor(task.id)
        // setExactAndAllowWhileIdle: alarma exacta que también se dispara en
        // reposo. Si el sistema ha denegado las alarmas exactas (Android 12+),
        // se recurre a una alarma inexacta: el aviso puede llegar algo tarde,
        // pero no se pierde. Ajustes informa del estado del permiso.
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, task.dueAt, pending
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, task.dueAt, pending
            )
        }
    }

    fun cancel(taskId: Long) {
        alarmManager.cancel(pendingIntentFor(taskId))
    }

    // Construye un PendingIntent con flags que lo hacen estable e inmutable.
    private fun pendingIntentFor(taskId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
