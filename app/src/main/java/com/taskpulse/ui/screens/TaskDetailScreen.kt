package com.taskpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.taskpulse.data.Task
import com.taskpulse.util.DateTimeFormat

// Pantalla de detalle de una tarea: muestra sus datos y ofrece completar,
// editar y eliminar. Al eliminar se vuelve a la lista.
@Composable
fun TaskDetailScreen(
    task: Task?,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    if (task == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tarea no encontrada", style = MaterialTheme.typography.body2)
            // Refuerzo del retorno automático a la lista: si ésta pantalla
            // llegara a quedarse, siempre hay una salida visible.
            Chip(
                label = { Text("Volver") },
                onClick = onBack,
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }
        return
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text(
                task.title,
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        item {
            Text(
                DateTimeFormat.full(task.dueAt),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        if (task.description.isNotBlank()) {
            item {
                Text(
                    task.description,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
        item {
            Chip(
                label = { Text(if (task.completed) "Reabrir" else "Completar") },
                onClick = onToggleCompleted,
                colors = ChipDefaults.primaryChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Editar") },
                onClick = { onEdit(task.id) },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Chip(
                label = { Text("Eliminar") },
                onClick = onDelete,
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth()
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
