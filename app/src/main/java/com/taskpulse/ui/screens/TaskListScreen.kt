package com.taskpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.taskpulse.data.Task
import com.taskpulse.ui.TaskListUiState

// Pantalla principal: lista de tareas pendientes y completadas, con acceso a
// crear una tarea nueva y a Ajustes. Pensada para la pantalla circular.
@Composable
fun TaskListScreen(
    state: TaskListUiState,
    onNewTask: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onToggleCompleted: (Task) -> Unit
) {
    val listState = rememberScalingLazyListState()
    val isEmpty = state.pending.isEmpty() && state.completed.isEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.loading -> LoadingState()
            else -> {
                // La lista se muestra siempre, incluso vacía, para que los
                // chips «Nueva tarea» y «Ajustes» estén siempre al alcance:
                // en el primer arranque no hay ninguna tarea y sin estos
                // chips la pantalla sería un callejón sin salida.
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item { Text("TaskPulse", style = MaterialTheme.typography.title2) }

                    if (isEmpty) {
                        item {
                            Text(
                                "Sin tareas todavía",
                                style = MaterialTheme.typography.title3,
                                textAlign = TextAlign.Center
                            )
                        }
                        item {
                            Text(
                                "Pulsa «Nueva tarea» para empezar",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        item {
                            Text(
                                "${state.pending.size} pendientes",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        state.pending.forEach { task ->
                            item { TaskRow(task = task, onClick = { onOpenTask(task.id) }) }
                        }

                        if (state.completed.isNotEmpty()) {
                            item {
                                Text(
                                    "Completadas",
                                    style = MaterialTheme.typography.caption3,
                                    color = MaterialTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            state.completed.forEach { task ->
                                item {
                                    TaskRow(
                                        task = task,
                                        onClick = { onToggleCompleted(task) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Chip(
                            label = { Text("Nueva tarea") },
                            onClick = onNewTask,
                            colors = ChipDefaults.primaryChipColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Chip(
                            label = { Text("Ajustes") },
                            onClick = onOpenSettings,
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
