package com.taskpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import com.taskpulse.util.DateTimeFormat
import java.util.Locale
import java.util.Calendar

// Selectores manuales de fecha y hora del editor de tareas. Cada paso ocupa
// la pantalla completa y se apoya en el componente Picker de Wear OS: una
// rueda con imán al centro que además entiende la corona rotativa del reloj.

// Número de días ofrecidos en la rueda de fecha (un año desde el día inicial).
private const val DAY_OPTIONS = 365
private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

// Inicio del día (medianoche local) que contiene a epochMillis.
internal fun startOfDay(epochMillis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

// Inicio del día que hay days días después del día de startOfDay.
private fun dayOffsetFrom(startOfDay: Long, days: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = startOfDay
        add(Calendar.DAY_OF_YEAR, days)
    }.timeInMillis

// Paso 1: elige el día deslizando la rueda. Devuelve el inicio del día
// elegido mediante onDaySelected; onCancel vuelve al formulario.
@Composable
fun DateSelectionStep(
    initialDayStart: Long,
    earliestDayStart: Long,
    onDaySelected: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val days = remember(earliestDayStart) {
        LongArray(DAY_OPTIONS) { dayOffsetFrom(earliestDayStart, it) }
    }
    // Índice del día actual dentro de la rueda. Se suma medio día antes de
    // dividir para que los cambios de hora de verano no lo desalineen.
    val initialIndex =
        (((initialDayStart - earliestDayStart) + DAY_MILLIS / 2) / DAY_MILLIS)
            .toInt()
            .coerceIn(0, DAY_OPTIONS - 1)
    val pickerState = rememberPickerState(
        initialNumberOfOptions = DAY_OPTIONS,
        initiallySelectedOption = initialIndex,
        repeatItems = false
    )

    StepScaffold(
        title = "Fecha",
        cancelLabel = "Cancelar",
        onCancel = onCancel,
        confirmLabel = "Siguiente",
        onConfirm = { onDaySelected(days[pickerState.selectedOption]) }
    ) { wheelModifier ->
        Picker(
            state = pickerState,
            contentDescription = "Fecha",
            modifier = wheelModifier
        ) { index ->
            OptionText(
                text = DateTimeFormat.weekdayDay(days[index]),
                selected = index == selectedOption
            )
        }
    }
}


// Paso 2: elige hora (0-23) y minuto (0-59) con dos ruedas lado a lado.
@Composable
fun TimeSelectionStep(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onBack: () -> Unit
) {
    val hourState = rememberPickerState(
        initialNumberOfOptions = 24,
        initiallySelectedOption = initialHour.coerceIn(0, 23),
        repeatItems = false
    )
    val minuteState = rememberPickerState(
        initialNumberOfOptions = 60,
        initiallySelectedOption = initialMinute.coerceIn(0, 59),
        repeatItems = false
    )

    StepScaffold(
        title = "Hora",
        cancelLabel = "Atrás",
        onCancel = onBack,
        confirmLabel = "Listo",
        onConfirm = { onConfirm(hourState.selectedOption, minuteState.selectedOption) }
    ) { wheelModifier ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = wheelModifier
        ) {
            Picker(
                state = hourState,
                contentDescription = "Hora",
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) { index ->
                OptionText(
                    text = String.format(Locale.getDefault(), "%02d", index),
                    selected = index == selectedOption
                )
            }
            Picker(
                state = minuteState,
                contentDescription = "Minuto",
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) { index ->
                OptionText(
                    text = String.format(Locale.getDefault(), "%02d", index),
                    selected = index == selectedOption
                )
            }
        }
    }
}

// Estructura común de los pasos: título arriba, la rueda ocupa el centro
// de la pantalla redonda y abajo los chips de navegación.
@Composable
private fun StepScaffold(
    title: String,
    cancelLabel: String,
    onCancel: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.caption3,
            color = MaterialTheme.colors.onSurfaceVariant
        )
        content(Modifier.weight(1f).fillMaxWidth())
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Chip(
                label = { Text(cancelLabel) },
                onClick = onCancel,
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.weight(1f)
            )
            Chip(
                label = { Text(confirmLabel) },
                onClick = onConfirm,
                colors = ChipDefaults.primaryChipColors(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Texto de una opción de la rueda: el valor centrado se resalta en color
// de marca y con tipografía mayor.
@Composable
private fun OptionText(text: String, selected: Boolean) {
    Text(
        text = text,
        style = if (selected) MaterialTheme.typography.title3
        else MaterialTheme.typography.body1,
        color = if (selected) MaterialTheme.colors.primary
        else MaterialTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
