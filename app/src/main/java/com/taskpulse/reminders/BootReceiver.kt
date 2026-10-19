package com.taskpulse.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taskpulse.data.TaskDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// BootReceiver vuelve a programar las alarmas cuando el reloj se reinicia.
// Android borra las alarmas al apagarse, así que se reconstruyen desde las
// tareas guardadas en Room. Así un recordatorio futuro no se pierde.
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Con los recordatorios desactivados en Ajustes no se reconstruye nada.
        if (!ReminderPreferences.isEnabled(context)) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scheduler = ReminderScheduler(context)
                val pending = TaskDatabase.get(context).taskDao().getPendingReminders()
                pending.forEach { scheduler.schedule(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
