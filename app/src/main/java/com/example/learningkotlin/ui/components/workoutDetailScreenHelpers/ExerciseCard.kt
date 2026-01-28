package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.WorkoutSet

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isWorkoutActive: Boolean,
    parentRefreshTrigger: Int, // <--- Watches parent state for stats/logic
    onUpdate: () -> Unit,
    onRemove: () -> Unit,
    onStartTimer: (Int) -> Unit
) {
    // Local trigger for adding/removing sets instantly without lag
    var localSetsTrigger by remember { mutableIntStateOf(0) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. HEADER
            ExerciseHeader(
                exercise = exercise,
                onDeleteExercise = { onRemove() },
                onTimerChange = { newTime ->
                    exercise.restTimer = newTime
                    localSetsTrigger++
                    onUpdate()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. COLUMN LABELS
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                HeaderLabel("SET", Modifier.weight(1f))
                HeaderLabel("PREVIOUS", Modifier.weight(2f))
                HeaderLabel("KG", Modifier.weight(1.5f))
                HeaderLabel("REPS", Modifier.weight(1.5f))
                if (isWorkoutActive) {
                    Spacer(modifier = Modifier.weight(1f)) 
                }
            }

            // 3. SETS LIST
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // We combine triggers to ensure we redraw when sets are added OR parent is refreshed
                val combinedTrigger = localSetsTrigger + parentRefreshTrigger
                
                exercise.sets.forEachIndexed { index, set ->
                    // Use a stable key for each set row to prevent flickering/lag
                    key(set.id) {
                        SetRow(
                            index = index + 1,
                            set = set,
                            isWorkoutActive = isWorkoutActive,
                            onDelete = {
                                exercise.sets.remove(set)
                                localSetsTrigger++
                                onUpdate()
                            },
                            onCheck = { isChecked ->
                                onUpdate() // Updates parent stats
                                if (isChecked) onStartTimer(exercise.restTimer)
                            }
                        )
                    }
                }
            }

            // 4. ADD BUTTON
            Button(
                onClick = {
                    val lastSet = exercise.sets.lastOrNull()
                    val newWeight = lastSet?.weight ?: 0.0
                    val newReps = lastSet?.reps ?: 0
                    val nextId = (exercise.sets.maxOfOrNull { it.id } ?: 0) + 1

                    exercise.sets.add(WorkoutSet(nextId, newWeight, newReps, false))
                    localSetsTrigger++ // Instant UI update for the new set
                    onUpdate()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Add Set")
            }
        }
    }
}

@Composable
private fun HeaderLabel(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}