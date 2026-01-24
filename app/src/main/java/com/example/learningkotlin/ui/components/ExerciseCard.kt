package com.example.learningkotlin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.WorkoutSet

@Composable
fun ExerciseCard(exercise: Exercise) {
    var refreshTrigger by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = exercise.name,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Table Header
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            HeaderLabel("SET", Modifier.weight(1f))
            HeaderLabel("PREVIOUS", Modifier.weight(2f))
            HeaderLabel("KG", Modifier.weight(1.5f))
            HeaderLabel("REPS", Modifier.weight(1.5f))
            Spacer(modifier = Modifier.weight(1f))
        }

        // 3. The Sets Loop (Using the external SetRow component)
        key(refreshTrigger) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                exercise.sets.forEachIndexed { index, set ->
                    SetRow(
                        index = index + 1,
                        set = set,
                        onDelete = {
                            exercise.sets.remove(set)
                            refreshTrigger++
                        }
                    )
                }
            }
        }

        // 4. Add Set Button
        Button(
            onClick = {
                val lastSet = exercise.sets.lastOrNull()
                val newWeight = lastSet?.weight ?: 0.0
                val newReps = lastSet?.reps ?: 0
                val nextId = (exercise.sets.maxOfOrNull { it.id } ?: 0) + 1

                exercise.sets.add(
                    WorkoutSet(nextId, newWeight, newReps, false)
                )
                refreshTrigger++
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("+ Add set")
        }
    }
}

// A tiny helper, can stay here since it's only used for the card header
@Composable
private fun HeaderLabel(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}