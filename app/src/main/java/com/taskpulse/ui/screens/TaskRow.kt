package com.taskpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.taskpulse.data.Task
import com.taskpulse.util.DateTimeFormat

// Una tarea está vencida si su hora ya pasó y sigue pendiente.
fun isOverdue(task: Task): Boolean =
    !task.completed && task.dueAt < System.currentTimeMillis()

// Fila de una tarea dentro de la lista. Muestra título, hora y, si está
// vencida, un aviso en color. Al tocarla se abre su detalle.
@Composable
fun TaskRow(task: Task, onClick: () -> Unit) {
    Chip(
        label = {
            Text(
                task.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(DateTimeFormat.time(task.dueAt))
                if (isOverdue(task)) {
                    Text(
                        "  · vencida",
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        onClick = onClick,
        colors = ChipDefaults.secondaryChipColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

// Estado de carga mientras Room entrega el primer listado.
@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cargando…", style = MaterialTheme.typography.body2)
    }
}
