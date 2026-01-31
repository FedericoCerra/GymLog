package com.example.learningkotlin.ui.components.workoutDetailScreenHelpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.Exercise
import com.example.learningkotlin.model.SetType
import com.example.learningkotlin.model.WorkoutSet
import com.example.learningkotlin.viewmodel.ExercisePersonalBests
import kotlin.math.roundToLong

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isWorkoutActive: Boolean,
    onUpdate: () -> Unit,
    onRemove: () -> Unit,
    onReplace: () -> Unit,
    onStartTimer: (Int) -> Unit,
    onInfoClick: () -> Unit,
    previousSets: List<WorkoutSet> = emptyList(),
    personalBests: ExercisePersonalBests = ExercisePersonalBests()
) {
    var localRefreshTrigger by remember { mutableIntStateOf(0) }
    var showWarmupConfig by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(modifier = Modifier.padding(16.dp)) {
            key(exercise.restTimer, exercise.notes) {
                ExerciseHeader(
                    exerciseName = exercise.name,
                    restTimer = exercise.restTimer,
                    notes = exercise.notes,
                    imagePath = exercise.imagePath,
                    onDeleteExercise = { onRemove() },
                    onReplaceExercise = { onReplace() },
                    onTimerChange = { newTime ->
                        exercise.restTimer = newTime
                        localRefreshTrigger++ 
                        onUpdate()
                    },
                    onInfoClick = onInfoClick,
                    onNotesChange = { newNotes ->
                        exercise.notes = newNotes
                    },
                    onClearSets = {
                        exercise.sets.clear()
                        localRefreshTrigger++
                        onUpdate()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // WARMUP GENERATOR IN-CARD UI (Next-Gen feel)
            AnimatedVisibility(
                visible = showWarmupConfig,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                WarmupConfigPanel(
                    initialWeight = exercise.sets.firstOrNull { it.type == SetType.NORMAL }?.weight ?: 0.0,
                    initialReps = exercise.sets.firstOrNull { it.type == SetType.NORMAL }?.reps ?: 10,
                    onDismiss = { showWarmupConfig = false },
                    onGenerate = { warmupSets, targetWeight, targetReps ->
                        val nextIdStart = (exercise.sets.maxOfOrNull { it.id } ?: 0) + 1
                        val newWarmupSets = warmupSets.mapIndexed { i, s -> 
                            WorkoutSet(nextIdStart + i, s.weight, s.reps, false, SetType.WARMUP)
                        }
                        
                        // 1. Add warmups at the beginning
                        exercise.sets.addAll(0, newWarmupSets)
                        
                        // 2. Check if a normal set with target weight already exists
                        val hasTargetSet = exercise.sets.any { it.type == SetType.NORMAL && it.weight == targetWeight }
                        if (!hasTargetSet) {
                            // If we have a single "empty" or default normal set, update it. Otherwise add new.
                            val defaultSet = exercise.sets.find { it.type == SetType.NORMAL && (it.weight == 0.0 || it.weight == previousSets.firstOrNull()?.weight) }
                            if (defaultSet != null) {
                                defaultSet.weight = targetWeight
                                defaultSet.reps = targetReps
                            } else {
                                val finalId = (exercise.sets.maxOfOrNull { it.id } ?: 0) + 1
                                exercise.sets.add(WorkoutSet(finalId, targetWeight, targetReps, false, SetType.NORMAL))
                            }
                        }
                        
                        localRefreshTrigger++
                        onUpdate()
                        showWarmupConfig = false
                    }
                )
            }

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
                exercise.sets.forEachIndexed { index, set ->
                    val prevSet = previousSets.getOrNull(index)
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
                            },
                            onValueChange = { onUpdate() },
                            previousWeight = prevSet?.weight,
                            previousReps = prevSet?.reps,
                            personalBests = personalBests
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(42.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("+ Add Set", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                if (exercise.sets.none { it.type == SetType.WARMUP }) {
                    Button(
                        onClick = { showWarmupConfig = !showWarmupConfig },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = Color(0xFFFFB300)
                        ),
                        modifier = Modifier
                            .width(56.dp)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "W",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WarmupConfigPanel(
    initialWeight: Double,
    initialReps: Int,
    onDismiss: () -> Unit,
    onGenerate: (List<WorkoutSet>, Double, Int) -> Unit
) {
    var weightText by remember { mutableStateOf(if (initialWeight > 0) initialWeight.toString().removeSuffix(".0") else "") }
    var repsText by remember { mutableStateOf(initialReps.toString()) }

    Column(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Warmup Generator", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Enter your target set", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Weight (kg)", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = repsText,
                onValueChange = { repsText = it },
                label = { Text("Reps", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {
                val w = weightText.toDoubleOrNull() ?: 0.0
                val r = repsText.toIntOrNull() ?: 0
                if (w > 0 && r > 0) {
                    val warmups = mutableListOf<WorkoutSet>()
                    
                    // Always Empty Bar
                    warmups.add(WorkoutSet(0, 20.0, 10))
                    
                    // Step 1: 50% for 5 reps (if target > 40kg)
                    if (w > 40) warmups.add(WorkoutSet(0, roundTo25(w * 0.5), 5))
                    
                    // Step 2: 70% for 3 reps (if target > 60kg)
                    if (w > 60) warmups.add(WorkoutSet(0, roundTo25(w * 0.7), 3))
                    
                    // Step 3: Feeder 90%
                    warmups.add(WorkoutSet(0, roundTo25(w * 0.9), 1))
                    
                    // Strength specific: 95% if reps < 3
                    if (r < 3) {
                        warmups.add(WorkoutSet(0, roundTo25(w * 0.95), 1))
                    }
                    
                    onGenerate(warmups.distinctBy { it.weight }.sortedBy { it.weight }, w, r)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Auto-Generate Sets")
        }
    }
}

private fun roundTo25(weight: Double): Double {
    return (weight / 2.5).roundToLong() * 2.5
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
