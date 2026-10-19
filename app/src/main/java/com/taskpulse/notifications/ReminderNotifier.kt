package com.taskpulse.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.taskpulse.MainActivity
import com.taskpulse.R
import com.taskpulse.data.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ReminderNotifier crea el canal y muestra la notificación del recordatorio.
// En Wear OS la notificación aparece en la pantalla del reloj y hace vibrar
// el dispositivo según la configuración del sistema.
class ReminderNotifier(private val context: Context) {

    fun show(task: Task) {
        // En Android 13+ hacen falta permisos de notificación en tiempo de
        // ejecución; si no están concedidos, no se lanza la notificación.
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            Log.w(TAG, "Aviso omitido: POST_NOTIFICATIONS no concedido (task=${task.id})")
            return
        }

        createChannel()

        // Al tocar la notificación se abre el detalle de la tarea.
        val contentIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_TASK_ID, task.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(task.dueAt))
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.title)
            .setContentText(context.getString(R.string.reminder_body, time))
            .setStyle(NotificationCompat.BigTextStyle().bigText(task.description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        NotificationManagerCompat.from(context).notify(task.id.toInt(), notification)
        Log.d(TAG, "Aviso publicado: task=${task.id} \"${task.title}\" a las ${System.currentTimeMillis() - task.dueAt} ms de su hora")
    }

    // Descarta la notificación de una tarea (al borrarla o completarla),
    // para que no quede un aviso que apunte a algo que ya no existe.
    fun cancel(taskId: Long) {
        NotificationManagerCompat.from(context).cancel(taskId.toInt())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        // Android NO permite actualizar un canal ya creado: createNotificationChannel
        // ignora cambios de sonido o vibración si el id existe. Se elimina y se
        // recrea para que la configuración actual siempre surta efecto.
        manager.deleteNotificationChannel(CHANNEL_ID)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_description)
            // En Android O+ el canal manda sobre la notificación: sin patrón
            // explícito algunos dispositivos (y emuladores) no vibran.
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            // Sonido explícito con uso de notificación; el constructor del canal
            // ya trae el sonido por defecto, pero fijarlo evita herencias raras.
            setSound(
                Settings.System.DEFAULT_NOTIFICATION_URI,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "TaskPulse"
        const val CHANNEL_ID = "recordatorios"
        const val EXTRA_OPEN_TASK_ID = "open_task_id"
    }
}
