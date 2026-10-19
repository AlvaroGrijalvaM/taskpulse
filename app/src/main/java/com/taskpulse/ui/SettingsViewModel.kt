package com.taskpulse.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taskpulse.data.TaskRepository
import com.taskpulse.reminders.ReminderPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado del ajuste global de recordatorios. Se guarda en preferencias
// privadas de la app para sobrevivir a los reinicios.
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(app)

    private val _remindersEnabled = MutableStateFlow(ReminderPreferences.isEnabled(app))
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    // Al apagar el interruptor se cancelan las alarmas ya programadas (las
    // tareas se quedan guardadas). Al reactivarlo se vuelven a programar todas
    // las tareas pendientes que tengan el recordatorio activado.
    fun setRemindersEnabled(enabled: Boolean) {
        ReminderPreferences.setEnabled(getApplication(), enabled)
        _remindersEnabled.value = enabled
        if (enabled) rescheduleAll() else cancelAll()
    }

    private fun rescheduleAll() {
        viewModelScope.launch {
            val pending = repository.getPendingReminders()
            pending.forEach { repository.scheduleReminder(it) }
        }
    }

    private fun cancelAll() {
        viewModelScope.launch { repository.cancelAllReminderAlarms() }
    }
}
