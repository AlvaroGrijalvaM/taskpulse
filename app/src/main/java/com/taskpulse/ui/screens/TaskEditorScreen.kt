package com.taskpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.taskpulse.ui.TaskEditorUiState
import com.taskpulse.util.DateTimeFormat
import java.util.Calendar

// Pasos del flujo del editor: formulario, selector de fecha y selector
// de hora. El chip de fecha/hora abre los selectores en pantalla completa.
private const val STEP_FORM = 0
private const val STEP_DATE = 1
private const val STEP_TIME = 2

// Pantalla del formulario de nueva/editar tarea: título, fecha, hora,
// interruptor de recordatorio y botón de guardar.
@Composable
fun TaskEditorScreen(
    state: TaskEditorUiState,
    isEditing: Boolean = false,
    onTitleChange: (String) -> Unit,
    onDueAtChange: (Long) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    // El ViewModel ya arranca con una hora por defecto (+1 h), así que aquí
    // sólo se muestra la hora del estado, sea la precargada o la elegida.
    val baseDueAt = state.dueAt

    // Paso actual del flujo y día elegido en el paso de fecha (inicio del
    // día en epoch). rememberSaveable los conserva si el sistema recicla
    // la actividad.
    var pickStep by rememberSaveable { mutableIntStateOf(STEP_FORM) }
    var pickedDayStart by rememberSaveable { mutableLongStateOf(0L) }

    when (pickStep) {
        STEP_DATE -> {
            // La rueda ofrece un año de días; como límite inferior se toma
            // hoy, o el día de la propia tarea si es anterior (tareas vencidas).
            val todayStart = startOfDay(System.currentTimeMillis())
            val dueDayStart = startOfDay(baseDueAt)
            DateSelectionStep(
                initialDayStart = dueDayStart,
                earliestDayStart = minOf(todayStart, dueDayStart),
                onDaySelected = { dayStart ->
                    pickedDayStart = dayStart
                    pickStep = STEP_TIME
                },
                onCancel = { pickStep = STEP_FORM }
            )
        }

        STEP_TIME -> {
            val dueCalendar = Calendar.getInstance().apply { timeInMillis = baseDueAt }
            val dayStart = if (pickedDayStart != 0L) pickedDayStart else startOfDay(baseDueAt)
            TimeSelectionStep(
                initialHour = dueCalendar.get(Calendar.HOUR_OF_DAY),
                initialMinute = dueCalendar.get(Calendar.MINUTE),
                onConfirm = { hour, minute ->
                    onDueAtChange(atTime(dayStart, hour, minute))
                    pickStep = STEP_FORM
                },
                onBack = { pickStep = STEP_DATE }
            )
        }


        else -> ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    if (isEditing) "Editar tarea" else "Nueva tarea",
                    style = MaterialTheme.typography.title2,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                TitleField(value = state.title, onChange = onTitleChange)
            }

            item {
                // Al tocar el chip se abre la selección manual de fecha y hora.
                Chip(
                    label = { Text(DateTimeFormat.day(baseDueAt)) },
                    secondaryLabel = { Text(DateTimeFormat.time(baseDueAt)) },
                    onClick = { pickStep = STEP_DATE },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ToggleChip(
                    checked = state.reminderEnabled,
                    onCheckedChange = onReminderToggle,
                    label = { Text("Recordatorio") },
                    toggleControl = {
                        Icon(
                            imageVector = ToggleChipDefaults.switchIcon(state.reminderEnabled),
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            state.error?.let { message ->
                item {
                    Text(
                        message,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            item {
                Chip(
                    label = { Text(if (state.saving) "Guardando…" else "Guardar") },
                    onClick = onSave,
                    enabled = !state.saving,
                    colors = ChipDefaults.primaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    label = { Text("Cancelar") },
                    onClick = onBack,
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Combina el inicio del día elegido con la hora y el minuto seleccionados.
private fun atTime(dayStart: Long, hour: Int, minute: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = dayStart
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

// Campo de texto para el título. Wear OS no trae teclado físico, así que se
// apoya en el teclado por voz del sistema al tocar el campo.
@Composable
private fun TitleField(value: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text(
            "Título",
            style = MaterialTheme.typography.caption3,
            color = MaterialTheme.colors.onSurfaceVariant
        )
        BasicTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = MaterialTheme.colors.onSurface,
                fontSize = MaterialTheme.typography.body1.fontSize
            )
        )
    }
}
