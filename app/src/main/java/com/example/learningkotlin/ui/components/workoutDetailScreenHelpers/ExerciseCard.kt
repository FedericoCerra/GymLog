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
    parentRefreshTrigger: Int,
    onUpdate: () -> Unit,
    onRemove: () -> Unit,
    onStartTimer: (Int) -> Unit,
    onInfoClick: () -> Unit
) {
    var localRefreshTrigger by remember { mutableIntStateOf(0) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Forces card to recompose when any data changes
        val trigger = localRefreshTrigger + parentRefreshTrigger
        
        Column(modifier = Modifier.padding(16.dp)) {
            // Pass the primitives explicitly so Compose detects the change instantly
            key(exercise.restTimer, exercise.notes) {
                ExerciseHeader(
                    exerciseName = exercise.name,
                    restTimer = exercise.restTimer,
                    notes = exercise.notes,
                    imagePath = exercise.imagePath,
                    onDeleteExercise = { onRemove() },
                    onTimerChange = { newTime ->
                        exercise.restTimer = newTime
                        localRefreshTrigger++ 
                        onUpdate()
                    },
                    onInfoClick = onInfoClick,
                    onNotesChange = { newNotes ->
                        exercise.notes = newNotes
                        // Don't refresh trigger here to keep typing smooth
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                HeaderLabel("SET", Modifier.weight(1f))
                HeaderLabel("PREVIOUS", Modifier.weight(2f))
                HeaderLabel("KG", Modifier.weight(1.5f))
                HeaderLabel("REPS", Modifier.weight(1.5f))
                if (isWorkoutActive) {
                    Spacer(modifier = Modifier.weight(1f)) 
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val listTrigger = trigger // Read trigger to keep list in sync
                exercise.sets.forEachIndexed { index, set ->
                    key(set.id) {
                        SetRow(
                            index = index + 1,
                            set = set,
                            isWorkoutActive = isWorkoutActive,
                            onDelete = {
                                exercise.sets.remove(set)
                                localRefreshTrigger++
                                onUpdate()
                            },
                            onCheck = { isChecked ->
                                onUpdate() 
                                if (isChecked) onStartTimer(exercise.restTimer)
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val lastSet = exercise.sets.lastOrNull()
                    val newWeight = lastSet?.weight ?: 0.0
                    val newReps = lastSet?.reps ?: 0
                    val nextId = (exercise.sets.maxOfOrNull { it.id } ?: 0) + 1

                    exercise.sets.add(WorkoutSet(nextId, newWeight, newReps, false))
                    localRefreshTrigger++
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