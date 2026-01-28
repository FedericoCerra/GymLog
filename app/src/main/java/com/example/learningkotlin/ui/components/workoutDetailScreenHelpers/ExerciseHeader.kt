package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Exercise

@Composable
fun ExerciseHeader(
    exercise: Exercise,
    onDeleteExercise: () -> Unit,
    onTimerChange: (Int) -> Unit,
    onInfoClick: () -> Unit // <--- Added this
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            // Tapping the name now also opens info
            Text(
                text = exercise.name,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.clickable { onInfoClick() }
            )

            RestTimerChip(
                currentSeconds = exercise.restTimer,
                onTimeSelected = onTimerChange
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Dedicated Info Button
            IconButton(onClick = onInfoClick) {
                Icon(Icons.Default.Info, "Info", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Remove Exercise", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) },
                        onClick = { onDeleteExercise(); showMenu = false }
                    )
                }
            }
        }
    }
}