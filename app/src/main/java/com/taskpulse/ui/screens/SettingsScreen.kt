package com.taskpulse.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults

// Pantalla de ajustes: interruptor global de recordatorios y estado de los
// permisos que necesita el sistema de alertas. No promete avisos si falta algo.
@Composable
fun SettingsScreen(
    remindersEnabled: Boolean,
    onToggleReminders: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val canNotify = hasNotificationPermission(context)
    val canExactAlarms = hasExactAlarmPermission(context)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text("Ajustes", style = MaterialTheme.typography.title2)
        }
        item {
            ToggleChip(
                checked = remindersEnabled,
                onCheckedChange = onToggleReminders,
                label = { Text("Recordatorios") },
                toggleControl = {
                    Icon(
                        imageVector = ToggleChipDefaults.switchIcon(remindersEnabled),
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            StatusRow(
                title = "Notificaciones",
                ok = canNotify,
                detail = if (canNotify) "Activadas" else "Sin permiso"
            )
        }
        item {
            StatusRow(
                title = "Alarmas exactas",
                ok = canExactAlarms,
                detail = if (canExactAlarms) "Permitidas" else "Sin permiso"
            )
        }
        item {
            Text(
                "Si falta algún permiso, activa los recordatorios en los ajustes del reloj.",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
        item {
            Chip(
                label = { Text("Volver") },
                onClick = onBack,
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusRow(title: String, ok: Boolean, detail: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text(title, style = MaterialTheme.typography.body1)
        Text(
            detail,
            style = MaterialTheme.typography.body2,
            color = if (ok) MaterialTheme.colors.primary else MaterialTheme.colors.error
        )
    }
}

// POST_NOTIFICATIONS se pide en tiempo de ejecución en Android 13+.
private fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

// En Android 12+ el usuario puede denegar las alarmas exactas por app.
private fun hasExactAlarmPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return alarmManager.canScheduleExactAlarms()
}
