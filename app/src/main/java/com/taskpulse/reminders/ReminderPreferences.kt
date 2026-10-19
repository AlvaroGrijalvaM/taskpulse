package com.taskpulse.reminders

import android.content.Context

// Preferencia global de recordatorios, compartida por Ajustes, el receptor
// de alarmas y los puntos de reprogramación (arranque de la app y reinicio).
// Está aquí (y no en Ajustes) para que receivers sin UI puedan consultarla.
object ReminderPreferences {

    private const val PREFS_NAME = "taskpulse_prefs"
    private const val KEY_REMINDERS_ENABLED = "reminders_enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REMINDERS_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REMINDERS_ENABLED, enabled)
            .apply()
    }
}
