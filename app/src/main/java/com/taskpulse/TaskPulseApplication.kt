package com.taskpulse

import android.app.Application
import com.taskpulse.data.TaskDatabase
import com.taskpulse.reminders.ReminderPreferences
import com.taskpulse.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Application: se ejecuta una sola vez, antes que cualquier pantalla o receiver.
// Aquí se crea el canal de notificaciones y se reprograman los recordatorios
// por si la app arranca tras un reinicio del reloj.
class TaskPulseApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        rescheduleReminders()
    }

    // Vuelve a registrar las alarmas pendientes desde Room. Es un refuerzo
    // adicional al BootReceiver, para cubrir el caso de arranques en frío.
    private fun rescheduleReminders() {
        appScope.launch {
            // Si los recordatorios están desactivados en Ajustes, no se
            // reprograma nada hasta que el usuario los vuelva a activar.
            if (!ReminderPreferences.isEnabled(this@TaskPulseApplication)) return@launch
            val scheduler = ReminderScheduler(this@TaskPulseApplication)
            val pending = TaskDatabase.get(this@TaskPulseApplication)
                .taskDao().getPendingReminders()
            pending.forEach { scheduler.schedule(it) }
        }
    }
}
