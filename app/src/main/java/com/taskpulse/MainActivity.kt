package com.taskpulse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.taskpulse.notifications.ReminderNotifier
import com.taskpulse.ui.TaskPulseApp
import com.taskpulse.ui.theme.TaskPulseTheme

// MainActivity: punto de entrada de la app. Además de alojar la interfaz
// Compose, solicita el permiso de notificaciones en Android 13+, necesario
// para que el recordatorio pueda mostrarse.
class MainActivity : ComponentActivity() {

    // id de la tarea que abrió la app desde una notificación (si la hubo).
    private var openedTaskId by mutableStateOf<Long?>(null)

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        handleIntent(intent)

        setContent {
            TaskPulseTheme {
                TaskPulseApp(openedTaskId = openedTaskId)
            }
        }
    }

    // Algunas rutas de lanzamiento (por ejemplo, "Open on watch" desde el
    // móvil pareado) reutilizan la actividad viva y entregan el extra aquí
    // en lugar de en onCreate: sin este método la navegación se ignoraría.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    // Lee el id de tarea de la notificación y lo expone a la interfaz.
    // openedTaskId sólo se actualiza si el intent lo trae (un lanzamiento
    // normal no debe sacar al usuario de la pantalla en la que está).
    private fun handleIntent(intent: Intent) {
        val taskId = intent.getLongExtra(ReminderNotifier.EXTRA_OPEN_TASK_ID, -1L)
            .takeIf { it != -1L }
        Log.d(TAG, "Intent recibido: taskId=$taskId action=${intent.action}")
        if (taskId != null) {
            openedTaskId = taskId
        }
    }

    // En Android 13+ POST_NOTIFICATIONS debe concederse en tiempo de ejecución.
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private companion object {
        const val TAG = "TaskPulse"
    }
}
